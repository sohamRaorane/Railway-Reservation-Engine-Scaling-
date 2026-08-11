# Railway Reservation Engine — 30-Day Day-by-Day Execution Guide
**Rule for using this doc:** Don't move to the next day until that day's "Verify" step passes. A task that "mostly works" but fails its verify step is not done — debugging compounds fast if you skip this.

Scope note: This 30-day plan builds the **monolith with correct core logic + concurrency + chart prep + basic events**. Full production-grade Tatkal scale (waiting room, heavy rate limiting) and microservices split are intentionally trimmed to "minimal working version" or "designed, not built" — call this out honestly in interviews, it's a normal and credible answer.

---

## WEEK 1 — Foundation, Auth, Core Schema

### Day 1 — Project Skeleton + Docker + Postgres
**Learn:** Spring Boot project structure, Spring Data JPA basics, Docker Compose fundamentals.
**Build:**
- Initialize Spring Boot project (Web, JPA, Postgres driver, Validation)
- Package structure per the blueprint (`config`, `security`, `common`, `train`, `booking`, `quota`, `waitlist`, `pnr`, `payment`, `cancellation`, `scheduler`)
- `docker-compose.yml` with Postgres service
  **Verify:** `docker-compose up` starts Postgres; Spring Boot app starts and connects (check logs for successful `HikariPool` connection, no errors).

### Day 2 — Core Static Entities + Migrations
**Learn:** Flyway migration basics, JPA entity mapping, foreign keys.
**Build:** Entities + Flyway scripts for `stations`, `trains`, `routes`, `coaches`, `seats`, `schedules` (from Section 2 of the design doc).
**Verify:** Run app, confirm all 6 tables exist in Postgres (`\dt` in psql or pgAdmin) with correct columns/FKs. Manually insert one train + coach + seats via SQL and confirm no constraint errors.

### Day 3 — JWT Auth (Register/Login)
**Learn:** JWT structure (header/payload/signature), BCrypt hashing, Spring Security basics.
**Build:** `users` table, register/login endpoints, JWT issuance.
**Verify:** Postman — register a user, login, confirm you get a valid JWT. Decode the JWT on jwt.io and confirm payload has correct claims (userId, role, expiry).

### Day 4 — RBAC + Refresh Tokens
**Learn:** Spring Security filter chain, role-based endpoint protection.
**Build:** `refresh_tokens` table, `/auth/refresh`, `/auth/logout`, protect one dummy `ADMIN`-only endpoint.
**Verify:** Non-admin JWT hitting admin endpoint → `403`. Admin JWT → `200`. Refresh token returns new valid access token.

### Day 5 — Remaining Schema
**Learn:** Nothing new — this is a schema-heavy day, reinforce ER modeling.
**Build:** `bookings`, `passengers`, `pnr_state_history`, `waitlist`, `rac`, `payments`, `refunds`, `quotas`, `quota_seat_allocation` tables + entities.
**Verify:** All tables from Section 2 of the design doc exist. Cross-check every table against the doc column-by-column — this is your last chance to catch schema gaps cheaply.

### Day 6 — Train Search API
**Learn:** JPQL/Criteria queries, DTO projection.
**Build:** `GET /trains/search?source=&destination=&date=`.
**Verify:** Search a route you seeded manually → correct train(s) returned. Search a non-existent route → empty array, not an error.

### Day 7 — Availability API + Basic Redis Cache
**Learn:** Redis basics, cache-aside pattern.
**Build:** `GET /trains/{id}/availability`, wrap it with a simple Redis cache (manual TTL, no invalidation logic yet).
**Verify:** First call hits DB (check logs/query count), second call within TTL returns from cache (no DB query in logs). Confirm response data matches DB truth.

---

## WEEK 2 — Booking, Quota, Waitlist Core

### Day 8 — Seat Allocation Engine (No Quota Yet)
**Learn:** Strategy pattern basics.
**Build:** `SeatAllocationService.allocate()` — picks any free seat in requested coach type.
**Verify:** Book seats until a coach is full — confirm no double allocation when booking sequentially (one at a time, not concurrent yet).

### Day 9 — Booking Workflow v1 (No Payment)
**Learn:** Transaction boundaries (`@Transactional`), DTO validation.
**Build:** `POST /booking/initiate` → creates booking + passengers + assigns seat, generates a PNR (simple format for now).
**Verify:** Book a seat, `GET /booking/{pnr}/status` shows CONFIRMED with correct passenger/seat data. Book past capacity → currently should fail loudly (you'll fix this with WL on Day 14) — confirm it fails predictably, not silently.

### Day 10 — Buffer / Consolidation Day
**No new build.** Re-test everything from Days 1–9 end-to-end. Fix any drift between the design doc schema and your actual code — update the doc if you changed anything.
**Verify:** Full flow — register → login → search → book → check status — works with zero manual DB fixes needed.

### Day 11 — Quota Schema Wiring
**Learn:** Real IRCTC quota rules (General, Ladies, Senior Citizen, Defence, Tatkal) — research this properly today.
**Build:** Seed `quotas` + `quota_seat_allocation` per schedule/coach. Write `QuotaEligibilityValidator` (age/gender/defence checks).
**Verify:** Unit test: a 25-year-old male passenger fails Senior Citizen eligibility; a female passenger passes Ladies quota eligibility.

### Day 12 — Quota-Aware Seat Allocation
**Learn:** Nothing new — apply Day 8/11 concepts together.
**Build:** Rework `SeatAllocationService` to check quota pool first, decrement `seats_used` on allocation.
**Verify:** Book a Ladies-quota seat with a male passenger → rejected. Book with a female passenger → succeeds and `seats_used` increments correctly (check DB directly).

### Day 13 — PNR State Machine
**Learn:** State design pattern.
**Build:** `PnrStateMachine` with explicit transition table + guards. Wire into booking (CNF path only for now).
**Verify:** Write a unit test that asserts `CANCELLED → CONFIRMED` throws/rejects, and `WAITLISTED → CONFIRMED` succeeds. This is a pure logic test — no DB needed, fast to verify.

### Day 14 — Waitlist Engine (Enqueue Side)
**Learn:** Per-quota queue design, row locking intent (`SELECT ... FOR UPDATE` — just understand it today, use it Day 21).
**Build:** When seat allocation fails for a quota, enqueue into `waitlist` with correct **quota-scoped** `wl_number` (not global).
**Verify:** Fill General quota fully, book one more General passenger → gets `GNWL1`. Simultaneously Ladies quota still has seats → Ladies passenger gets CONFIRMED, not WL. This dual-check is your key correctness test.

---

## WEEK 3 — Waitlist Promotion, Cancellation, Payment, Concurrency

### Day 15 — RAC Engine
**Learn:** RAC shared-berth concept (2 passengers per side-lower).
**Build:** `rac` table wiring — when RAC pool has space, WL passengers go here instead of pure WL.
**Verify:** Fill CNF + RAC seats for a quota, next booking → correctly lands in WL (not RAC, not CNF). Check `rac_number` sequencing is correct.

### Day 16 — Cancellation Charge Calculator
**Learn:** Time-window business rules, careful datetime/timezone handling.
**Build:** `ChargeCalculator` — >48hrs = 25%, 12-48hrs = 50%, <12hrs = 0% refund.
**Verify:** Unit tests at exact boundaries — 48hrs+1min, exactly 48hrs, 47hrs59min — confirm each lands in the correct slab. Boundary bugs are the most common bug here.

### Day 17 — Cancellation → Waitlist Promotion (Wire Together)
**Learn:** Nothing new — integration of Days 14–16.
**Build:** `POST /booking/{pnr}/cancel` → frees seat → triggers `WaitlistPromotionService` synchronously (Kafka comes later) → promotes correct quota's next WL/RAC.
**Verify:** **This is your standout feature — test it thoroughly.** Cancel a CNF ticket in Ladies quota with both GNWL and LDWL passengers waiting → confirm only the LDWL passenger gets promoted, GNWL untouched.

### Day 18 — Buffer / Debug Day
**No new build.** Stress-test Days 11–17 manually with multiple quotas active simultaneously. Fix bugs found.
**Verify:** Run 5–6 manual booking/cancellation scenarios mixing quotas and confirm every promotion is quota-correct. Write down any bug you find and its root cause — useful for interview "what bugs did you hit" questions.

### Day 19 — Payment Integration (Sandbox)
**Learn:** Payment gateway sandbox docs (Razorpay/Stripe test mode), webhook signature verification.
**Build:** `/payment/initiate`, `/payment/webhook`, wire booking confirmation to payment success.
**Verify:** Complete a test payment in sandbox → webhook fires → booking flips to CONFIRMED. Simulate a failed payment → booking/seat correctly rolls back or releases.

### Day 20 — Seat Hold (TTL) + Idempotency
**Learn:** Redis TTL keys, idempotency key pattern.
**Build:** Seat hold in Redis during payment window; idempotency key on `/booking/initiate` to prevent duplicate PNRs on double-submit.
**Verify:** Initiate booking, don't pay, wait past TTL → seat auto-releases (check availability). Submit the same idempotency key twice → second call returns the same booking, doesn't create a duplicate.

### Day 21 — Pessimistic Locking
**Learn:** `@Lock(PESSIMISTIC_WRITE)`, Postgres row locking, isolation levels.
**Build:** Apply pessimistic lock on the seat row during allocation.
**Verify:** Manually run two overlapping transactions in two DB sessions (psql) on the same seat — confirm the second blocks until the first commits/rolls back.

---

## WEEK 4 — Concurrency Proof, Chart Prep, Events, Ship It

### Day 22 — Concurrency Load Test
**Learn:** `ExecutorService`/`CompletableFuture` for concurrent test simulation.
**Build:** A test that fires 100–500 concurrent booking requests at 1 seat.
**Verify:** Exactly 1 booking is CONFIRMED, all others land correctly in quota-scoped WL — zero duplicate confirmations, zero lost requests. **Save this test output — it's your #1 interview proof point.**

### Day 23 — Chart Preparation Job (Basic)
**Learn:** Spring `@Scheduled` or Quartz basics.
**Build:** Scheduled job that, given a scheduleId, freezes booking and promotes WL→RAC/CNF as capacity allows, in quota order.
**Verify:** Manually trigger for a test schedule with pending WL entries → correct passengers promoted, schedule marked `CHART_PREPARED`, no further bookings accepted after.

### Day 24 — Chart Prep Robustness
**Learn:** Distributed locks (ShedLock) — at least conceptually, implement a simple version.
**Build:** Guard the job so it can't double-run; make it resumable (idempotent re-run doesn't double-promote).
**Verify:** Manually run the job twice in a row for the same schedule → second run is a no-op, no duplicate promotions or errors.

### Day 25 — Buffer / Debug Day
**No new build.** Re-run Day 22's concurrency test alongside Day 23–24's chart prep in combination. Fix anything that breaks under combined load.
**Verify:** Full stress scenario passes cleanly, twice in a row.

### Day 26 — Basic Kafka Event Wiring
**Learn:** Kafka topics/producers/consumers basics.
**Build:** Publish `booking.created` / `booking.cancelled` events; move waitlist promotion (Day 17) to consume from Kafka instead of a direct call.
**Verify:** Cancel a booking → confirm event appears in the topic (console consumer or a log line) → promotion still happens correctly, just asynchronously now.

### Day 27 — Notifications + Logging
**Learn:** Structured logging, correlation IDs, Micrometer/Actuator basics.
**Build:** Simple notification consumer (console/email log is fine) reacting to booking/promotion events; add correlation ID to logs.
**Verify:** Trigger a booking → cancellation → promotion chain, confirm you can follow one correlation ID through all the log lines across the flow.

### Day 28 — Testing Pass
**Learn:** Testcontainers basics.
**Build:** Unit tests for quota rules, charge calculator, state machine; one integration test for the full booking flow using Testcontainers.
**Verify:** All tests green. This is your safety net before final packaging — don't skip it even if time is tight.

### Day 29 — Dockerize Everything
**Learn:** Multi-stage Dockerfile, full docker-compose stack.
**Build:** Dockerfile for the app; `docker-compose.yml` covering app + Postgres + Redis + Kafka.
**Verify:** From a clean clone, `docker-compose up` brings up the entire stack and a fresh booking flow works with zero manual setup.

### Day 30 — Polish + Interview Prep
**No new build.** Write the README (architecture diagram, setup steps, design decisions, known limitations). Do one full manual demo run-through end to end. Write your talking-points doc (one paragraph per module: what it does, what problem it solves, what you'd improve with more time).
**Verify:** You can explain, without looking at code: how quota-scoped waitlist promotion works, how the concurrency test proves correctness, and how chart prep behaves under a distributed setup. If you can say all three fluently, you're interview-ready on this project.

---

## If a Day Runs Over
Don't let one hard day eat two days. If you're stuck past ~1.5x the expected time:
1. Note the blocker in one line, move to the next day's task.
2. Come back to it as a buffer-day fix (Days 10, 18, 25 exist for exactly this).
3. Never let Week 3 (waitlist promotion + concurrency) slip — cut Week 4 scope instead if you're behind (Kafka/notifications/Docker can be described as "designed, not fully implemented" in interviews; a broken concurrency story cannot).

## Daily Habit
At the end of each day, write 2 lines in a running log: what you built, what you'd do differently. This becomes your interview prep material for free — don't reconstruct it from memory on Day 30.