package com.skylane.booking.service;

import com.skylane.booking.domain.Entities.*;
import com.skylane.booking.repo.Repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Booking service. Runs the full hold -> price -> pay -> ticket sequence inline
 * inside a single request/transaction. The cabin bucket row is locked for the
 * whole duration, and the external payment gateway is called synchronously
 * mid-transaction.
 */
@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final InventoryBucketRepository inventory;
    private final FareRepository fares;
    private final BookingRepository bookings;
    private final TicketRepository tickets;
    private final FareService fareService;
    private final RestTemplate restTemplate;

    @Value("${skylane.payment.gateway-url}")
    private String paymentGatewayUrl;

    public BookingService(InventoryBucketRepository inventory,
                          FareRepository fares,
                          BookingRepository bookings,
                          TicketRepository tickets,
                          FareService fareService,
                          RestTemplate restTemplate) {
        this.inventory = inventory;
        this.fares = fares;
        this.bookings = bookings;
        this.tickets = tickets;
        this.fareService = fareService;
        this.restTemplate = restTemplate;
    }

    @Transactional
    public BookingResult book(String flightId, String cabin, String passengerRef, String paymentToken) {
        // Step 1: hold. Lock the cabin bucket row for the whole transaction.
        InventoryBucket bucket = inventory.lockByFlightAndCabin(flightId, cabin)
                .orElseThrow(() -> new IllegalArgumentException(
                        "no inventory for " + flightId + "/" + cabin));

        if (bucket.available() <= 0) {
            throw new IllegalStateException("sold out: " + flightId + "/" + cabin);
        }
        bucket.setHeld(bucket.getHeld() + 1);
        bucket.setUpdatedAt(Instant.now());
        inventory.save(bucket);

        // Step 2: price.
        BigDecimal price = fareService.quote(flightId, cabin);

        Booking booking = new Booking();
        booking.setFlightId(flightId);
        booking.setCabin(cabin);
        booking.setPassengerRef(passengerRef);
        booking.setStatus("HELD");
        booking.setCreatedAt(Instant.now());
        booking = bookings.save(booking);

        // Step 3: pay. Synchronous call to the external payment gateway while the
        // inventory row lock is still held.
        String authCode = charge(paymentToken, price, booking.getId());
        booking.setStatus("PAID");
        booking.setAmountPaid(price);
        bookings.save(booking);

        // Step 4: ticket. Convert the hold into a sold seat and issue the ticket.
        bucket.setHeld(bucket.getHeld() - 1);
        bucket.setSold(bucket.getSold() + 1);
        bucket.setUpdatedAt(Instant.now());
        inventory.save(bucket);

        Ticket ticket = new Ticket();
        ticket.setBookingId(booking.getId());
        ticket.setTicketNumber("TK-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());
        ticket.setIssuedAt(Instant.now());
        tickets.save(ticket);

        booking.setStatus("TICKETED");
        bookings.save(booking);

        log.info("booking ticketed id={} flight={}/{} auth={} amount={}",
                booking.getId(), flightId, cabin, authCode, price);

        return new BookingResult(booking.getId(), ticket.getTicketNumber(), "TICKETED", price);
    }

    private String charge(String paymentToken, BigDecimal amount, Long bookingId) {
        Map<String, Object> req = Map.of(
                "token", paymentToken,
                "amount", amount,
                "currency", "USD",
                "reference", "BK-" + bookingId);
        @SuppressWarnings("unchecked")
        Map<String, Object> resp = restTemplate.postForObject(paymentGatewayUrl, req, Map.class);
        if (resp == null || !"approved".equals(resp.get("status"))) {
            throw new IllegalStateException("payment declined for booking " + bookingId);
        }
        return String.valueOf(resp.get("authCode"));
    }

    public record BookingResult(Long bookingId, String ticketNumber, String status, BigDecimal amount) {}
}
