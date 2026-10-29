package com.skylane.booking.service;

import com.skylane.booking.domain.Entities.Fare;
import com.skylane.booking.domain.Entities.InventoryBucket;
import com.skylane.booking.repo.Repositories.FareRepository;
import com.skylane.booking.repo.Repositories.InventoryBucketRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Fare service. Recomputes the fare live on every call by reading the same
 * inventory and fare rows the booking write path uses.
 */
@Service
public class FareService {

    private final FareRepository fares;
    private final InventoryBucketRepository inventory;

    public FareService(FareRepository fares, InventoryBucketRepository inventory) {
        this.fares = fares;
        this.inventory = inventory;
    }

    public BigDecimal quote(String flightId, String cabin) {
        Fare fare = fares.findByFlightIdAndCabin(flightId, cabin)
                .orElseThrow(() -> new IllegalArgumentException(
                        "no fare for " + flightId + "/" + cabin));

        // Live read of the inventory row to derive the current load factor.
        InventoryBucket bucket = inventory.findByFlightIdAndCabin(flightId, cabin)
                .orElseThrow(() -> new IllegalArgumentException(
                        "no inventory for " + flightId + "/" + cabin));

        double loadFactor = bucket.getCapacity() == 0
                ? 0.0
                : (double) (bucket.getSold() + bucket.getHeld()) / bucket.getCapacity();

        BigDecimal dynamicMultiplier = BigDecimal.ONE
                .add(fare.getLoadFactorMultiplier()
                        .multiply(BigDecimal.valueOf(loadFactor)));

        return fare.getBasePrice()
                .multiply(dynamicMultiplier)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
