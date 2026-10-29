package com.skylane.booking.batch;

import com.skylane.booking.domain.Entities.InventoryBucket;
import com.skylane.booking.repo.Repositories.InventoryBucketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

/**
 * Nightly file-diff synchronisation with the two GDS partners. This cron job
 * is the only mechanism keeping own-channel inventory and partner-side sales in
 * sync. It runs once in the 02:00-04:00 window: it exports own-channel state to
 * a file for the partners and imports the partner-side sold counts back in.
 */
@Component
public class GdsNightlyDiffJob {

    private static final Logger log = LoggerFactory.getLogger(GdsNightlyDiffJob.class);

    private final InventoryBucketRepository inventory;

    @Value("${skylane.gds.export-dir}")
    private String exportDir;

    public GdsNightlyDiffJob(InventoryBucketRepository inventory) {
        this.inventory = inventory;
    }

    @Scheduled(cron = "${skylane.gds.cron}")
    @Transactional
    public void runNightlyDiff() {
        log.info("gds-nightly-diff starting export/import window");
        try {
            exportOwnChannel();
            importPartnerChanges();
        } catch (IOException e) {
            log.error("gds-nightly-diff failed: {}", e.getMessage());
        }
        log.info("gds-nightly-diff complete");
    }

    private void exportOwnChannel() throws IOException {
        List<InventoryBucket> all = inventory.findAll();
        StringBuilder sb = new StringBuilder();
        for (InventoryBucket b : all) {
            sb.append(b.getFlightId()).append(',')
              .append(b.getCabin()).append(',')
              .append(b.getCapacity()).append(',')
              .append(b.getSold()).append('\n');
        }
        Path out = Path.of(exportDir, "own-channel-export.csv");
        Files.createDirectories(out.getParent());
        Files.writeString(out, sb.toString());
        log.info("gds-nightly-diff exported {} buckets to {}", all.size(), out);
    }

    private void importPartnerChanges() throws IOException {
        Path in = Path.of(exportDir, "partner-diff-import.csv");
        if (!Files.exists(in)) {
            log.info("gds-nightly-diff no partner diff file present");
            return;
        }
        for (String line : Files.readAllLines(in)) {
            if (line.isBlank()) continue;
            String[] parts = line.split(",");
            String flightId = parts[0];
            String cabin = parts[1];
            int partnerSold = Integer.parseInt(parts[3]);
            inventory.findByFlightIdAndCabin(flightId, cabin).ifPresent(b -> {
                int merged = b.getSold() + partnerSold;
                if (merged > b.getCapacity()) {
                    log.warn("inventory-bucket {}/{} sold={} capacity={} source=gds-nightly-diff",
                            flightId, cabin, merged, b.getCapacity());
                }
                b.setSold(merged);
                b.setUpdatedAt(Instant.now());
                inventory.save(b);
            });
        }
        log.info("gds-nightly-diff import applied partner diff");
    }
}
