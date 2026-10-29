package com.skylane.booking.repo;

import com.skylane.booking.domain.Entities.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public class Repositories {

    public interface InventoryBucketRepository extends JpaRepository<InventoryBucket, Long> {

        Optional<InventoryBucket> findByFlightIdAndCabin(String flightId, String cabin);

        List<InventoryBucket> findByFlightId(String flightId);

        /**
         * Pessimistic write lock on the cabin bucket row. Held for the entire
         * hold-price-pay-ticket transaction in BookingService.
         */
        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("select b from InventoryBucket b where b.flightId = :flightId and b.cabin = :cabin")
        Optional<InventoryBucket> lockByFlightAndCabin(@Param("flightId") String flightId,
                                                       @Param("cabin") String cabin);
    }

    public interface FareRepository extends JpaRepository<Fare, Long> {
        Optional<Fare> findByFlightIdAndCabin(String flightId, String cabin);
        List<Fare> findByFlightId(String flightId);
    }

    public interface BookingRepository extends JpaRepository<Booking, Long> {
        List<Booking> findByPassengerRef(String passengerRef);
    }

    public interface TicketRepository extends JpaRepository<Ticket, Long> {
        Optional<Ticket> findByBookingId(Long bookingId);
    }
}
