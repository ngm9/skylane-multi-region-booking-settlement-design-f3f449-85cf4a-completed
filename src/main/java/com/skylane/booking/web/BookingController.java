package com.skylane.booking.web;

import com.skylane.booking.service.BookingService;
import com.skylane.booking.service.BookingService.BookingResult;
import com.skylane.booking.service.SearchService;
import com.skylane.booking.service.SearchService.SearchResult;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST edge routed by the gateway: search/quote traffic and the booking write
 * path both terminate here and hit the same Booking DB.
 */
@RestController
@RequestMapping("/api")
public class BookingController {

    private final SearchService searchService;
    private final BookingService bookingService;

    public BookingController(SearchService searchService, BookingService bookingService) {
        this.searchService = searchService;
        this.bookingService = bookingService;
    }

    @GetMapping("/search/{flightId}")
    public List<SearchResult> search(@PathVariable String flightId) {
        return searchService.search(flightId);
    }

    @PostMapping("/bookings")
    public ResponseEntity<BookingResult> book(@RequestBody BookRequest req) {
        BookingResult result = bookingService.book(
                req.flightId(), req.cabin(), req.passengerRef(), req.paymentToken());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/health")
    public String health() {
        return "ok";
    }

    public record BookRequest(
            @NotBlank String flightId,
            @NotBlank String cabin,
            @NotBlank String passengerRef,
            @NotBlank String paymentToken) {}
}
