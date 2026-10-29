package com.skylane.booking.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Booking DB schema: single PostgreSQL instance, one region.
 * inventory_buckets holds one row per flight cabin bucket.
 */
public class Entities {

    @Entity
    @Table(name = "inventory_buckets",
            uniqueConstraints = @UniqueConstraint(columnNames = {"flight_id", "cabin"}))
    public static class InventoryBucket {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "flight_id", nullable = false)
        private String flightId;

        @Column(nullable = false)
        private String cabin;

        @Column(nullable = false)
        private int capacity;

        @Column(nullable = false)
        private int sold;

        @Column(name = "held", nullable = false)
        private int held;

        @Column(name = "updated_at")
        private Instant updatedAt;

        public Long getId() { return id; }
        public String getFlightId() { return flightId; }
        public void setFlightId(String v) { this.flightId = v; }
        public String getCabin() { return cabin; }
        public void setCabin(String v) { this.cabin = v; }
        public int getCapacity() { return capacity; }
        public void setCapacity(int v) { this.capacity = v; }
        public int getSold() { return sold; }
        public void setSold(int v) { this.sold = v; }
        public int getHeld() { return held; }
        public void setHeld(int v) { this.held = v; }
        public Instant getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(Instant v) { this.updatedAt = v; }

        public int available() { return capacity - sold - held; }
    }

    @Entity
    @Table(name = "fares")
    public static class Fare {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "flight_id", nullable = false)
        private String flightId;

        @Column(nullable = false)
        private String cabin;

        @Column(name = "base_price", nullable = false)
        private BigDecimal basePrice;

        @Column(name = "load_factor_multiplier", nullable = false)
        private BigDecimal loadFactorMultiplier;

        public Long getId() { return id; }
        public String getFlightId() { return flightId; }
        public void setFlightId(String v) { this.flightId = v; }
        public String getCabin() { return cabin; }
        public void setCabin(String v) { this.cabin = v; }
        public BigDecimal getBasePrice() { return basePrice; }
        public void setBasePrice(BigDecimal v) { this.basePrice = v; }
        public BigDecimal getLoadFactorMultiplier() { return loadFactorMultiplier; }
        public void setLoadFactorMultiplier(BigDecimal v) { this.loadFactorMultiplier = v; }
    }

    @Entity
    @Table(name = "bookings")
    public static class Booking {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "flight_id", nullable = false)
        private String flightId;

        @Column(nullable = false)
        private String cabin;

        @Column(name = "passenger_ref", nullable = false)
        private String passengerRef;

        @Column(nullable = false)
        private String status;

        @Column(name = "amount_paid")
        private BigDecimal amountPaid;

        @Column(name = "created_at")
        private Instant createdAt;

        public Long getId() { return id; }
        public String getFlightId() { return flightId; }
        public void setFlightId(String v) { this.flightId = v; }
        public String getCabin() { return cabin; }
        public void setCabin(String v) { this.cabin = v; }
        public String getPassengerRef() { return passengerRef; }
        public void setPassengerRef(String v) { this.passengerRef = v; }
        public String getStatus() { return status; }
        public void setStatus(String v) { this.status = v; }
        public BigDecimal getAmountPaid() { return amountPaid; }
        public void setAmountPaid(BigDecimal v) { this.amountPaid = v; }
        public Instant getCreatedAt() { return createdAt; }
        public void setCreatedAt(Instant v) { this.createdAt = v; }
    }

    @Entity
    @Table(name = "tickets")
    public static class Ticket {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "booking_id", nullable = false)
        private Long bookingId;

        @Column(name = "ticket_number", nullable = false, unique = true)
        private String ticketNumber;

        @Column(name = "issued_at")
        private Instant issuedAt;

        public Long getId() { return id; }
        public Long getBookingId() { return bookingId; }
        public void setBookingId(Long v) { this.bookingId = v; }
        public String getTicketNumber() { return ticketNumber; }
        public void setTicketNumber(String v) { this.ticketNumber = v; }
        public Instant getIssuedAt() { return issuedAt; }
        public void setIssuedAt(Instant v) { this.issuedAt = v; }
    }
}
