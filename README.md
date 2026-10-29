# Skylane Multi Region Booking Settlement Design

Web and mobile clients hit a single-region load balancer and API gateway that terminates auth, then route to a Search/Quote service and a Booking service. Seat inventory lives in one PostgreSQL instance as one row per flight cabin bucket, with tables for inventory_buckets, bookings, fares and tickets.

This repository is the Java implementation of that system.

## Helpful Tips

- Read `docs/DESIGN.md` — the design document — for the full background on this task.
- Review the source code below to see exactly how the current system works.
- On the Utkrusht whiteboard for this task, the current system is already drawn and locked for reference — draw your redesigned architecture in the same High-Level Design area.
- You're welcome to update `docs/DESIGN.md` or add your own files to this repository to capture your design notes as you work.

## Objectives
Please commit an updated DESIGN.md capturing your rationale, decisions and assumptions. Also update the excalidraw diagram to show the changes you propose. You are tasked with the following changes:
- Redesign the booking-to-settlement flow so it holds up across regions and failures.
- Ensure a flight is never oversold across your own channel and the GDS.
- Decide seat holds within the 400ms budget, justifying where inventory truly lives.
- Ensure a paid booking is never lost, even when a mid-flow step fails.
- Keep GDS bookings in sync in near real time, with a story for conflicts.
- Decide what each flow sacrifices — availability or consistency — when a region is lost.
- Complete the remaining board sections: NFRs, API routes, trade-offs, capacity, failure modes.

## What's in this repository

- `docs/DESIGN.md` — the design document
- The current implementation, organized as a standard Java/Maven project:
  - API Gateway — `src/main/java/com/skylane/booking/SkylaneBookingApplication.java`, `src/main/java/com/skylane/booking/config/SecurityConfig.java`, `src/main/java/com/skylane/booking/web/BookingController.java`
  - Booking DB — `src/main/java/com/skylane/booking/domain/Entities.java`, `src/main/java/com/skylane/booking/repo/Repositories.java`
  - Fare Service — `src/main/java/com/skylane/booking/service/FareService.java`
  - Search/Quote Service — `src/main/java/com/skylane/booking/service/SearchService.java`
  - Booking Service — `src/main/java/com/skylane/booking/service/BookingService.java`
  - Nightly Diff Job — `src/main/java/com/skylane/booking/batch/GdsNightlyDiffJob.java`
