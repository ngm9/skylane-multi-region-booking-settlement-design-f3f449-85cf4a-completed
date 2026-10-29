package com.skylane.booking.service;

import com.skylane.booking.domain.Entities.InventoryBucket;
import com.skylane.booking.repo.Repositories.InventoryBucketRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Search/Quote service. Every search reads the live inventory rows and asks
 * the Fare service to recompute the fare against those same rows.
 */
@Service
public class SearchService {

    private final InventoryBucketRepository inventory;
    private final FareService fareService;

    public SearchService(InventoryBucketRepository inventory, FareService fareService) {
        this.inventory = inventory;
        this.fareService = fareService;
    }

    public List<SearchResult> search(String flightId) {
        List<SearchResult> results = new ArrayList<>();
        List<InventoryBucket> buckets = inventory.findByFlightId(flightId);
        for (InventoryBucket b : buckets) {
            BigDecimal price = fareService.quote(flightId, b.getCabin());
            results.add(new SearchResult(flightId, b.getCabin(), b.available(), price));
        }
        return results;
    }

    public record SearchResult(String flightId, String cabin, int available, BigDecimal price) {}
}
