# Railway Reservation Engine

A production-grade, event-driven railway ticketing backend built with **Java 21 + Spring Boot**, engineered around the real-world constraints of Indian Railways booking: **quotas, RAC, waitlists, chart preparation, and seat races** — handled with pessimistic locking, idempotent booking, Kafka-driven waitlist promotion, and Razorpay payment integration.

> This project is a deep, interview-grade exploration of building a **correct-by-construction** concurrent booking system: the hard problems (double-booking, seat leaks, promotion cascades) are solved at the data layer, not papered over at the API layer.

---

## Table of Contents

- [Features](#features)
- [Architecture Overview](#architecture-overview)
- [Tech Stack](#tech-stack)
- [Domain Model: The Railway Problem](#domain-model-the-railway-problem)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [API Reference](#api-reference)
- [Concurrency & Reliability Design](#concurrency--reliability-design)
- [Database & Migrations](#database--migrations)
- [Testing](#testing)
- [Documentation](#documentation)
- [Roadmap](#roadmap)

---

## Features

### Core booking engine
- **Quota-aware booking** with eligibility validation: General (`GN`), Ladies (`LD`), Senior Citizen (`SS`), Defence (`DEF`), Tatkal (`TQ`).
- **Three-tier booking states** — `CONFIRMED`, `RAC` (Reservation Against Cancellation), `WAITLIST` — mirroring real IRCTC semantics.
- **Per-quota seat pools** (`quota_reservation_pool`) with counters tracked under pessimistic locks, so quotas never overdraw.
- **Idempotent booking** via an `Idempotency-Key` header — retries return the same booking instead of duplicates.

### Waitlist & RAC promotion
- **Event-driven promotion cascade**: a cancellation frees seats, and a Kafka event triggers waitlist → RAC → confirmed promotion — once **per freed seat**, in quota-aware order (oldest entry first).
- **Chart preparation** with a compare-and-swap (CAS) single-winner pattern: one scheduler wins the chart, everyone else is told to back off.

### Payments & lifecycle
- **Razorpay integration** (test mode): order creation, webhook verification, failure handling that *undoes* seat allocations and triggers promotion.
- **Cancellation with charge calculation** (sliding refund based on time-to-departure) and **persisted refund records** as an audit trail.
- **PNR state history** for full lifecycle auditing of every booking.

### Platform concerns
- **JWT auth + refresh-token rotation** with logout/revocation; `defence_personnel` flag drives Defence-quota eligibility.
- **Kafka eventing** with `afterCommit` publishing (no uncommitted reads), consumer groups, and `correlationId` propagated through MDC for end-to-end log tracing.
- **Cache-aside train search** over Redis.
- **Flyway migrations** — a 32-step schema evolution history.
- **Correlation-id logging** across every request.

---

## Architecture Overview

```
                        ┌────────────────────────────────────────────────────────────┐
                        │                         Clients                            │
                        │              (booking app / payment-test.html)              │
                        └──────────────────────────┬─────────────────────────────────┘
                                                   │ HTTPS (JWT Bearer)
                        ┌──────────────────────────▼─────────────────────────────────┐
                        │                     Spring Boot (port 8080)                 │
                        │  ┌────────────┐  ┌──────────────┐  ┌────────────────────┐  │
                        │  │  Security  │  │ Controllers   │  │  Services (lock-   │  │
                        │  │  (JWT/     │  │ (REST API)    │  │  guarded business  │  │
                        │  │  refresh)  │  │               │  │  logic)            │  │
                        │  └────────────┘  └──────┬───────┘  └──────────┬─────────┘  │
                        └─────────────────────────┼─────────────────────┼────────────┘
                    ┌─────────────┬───────────────┼─────────────────┬───┴──────────────┐
                    │             │               │                 │                  │
        ┌───────────▼─────┐ ┌────▼──────────┐ ┌───▼─────────────┐ ┌─▼──────────────┐ ┌─▼────────────┐
        │   PostgreSQL 17 │ │   Redis 7     │ │     Kafka       │ │    Razorpay    │ │  Flyway      │
        │   (JPA + locks) │ │ (search cache)│ │ (booking.created│ │ (orders +      │ │ (V1 → V32)   │
        │                 │ │               │ │  booking.       │ │  webhooks)     │ │              │
        │                 │ │               │ │  cancelled)     │ │                │ │              │
        └─────────────────┘ └───────────────┘ └─────────────────┘ └────────────────┘ └──────────────┘
```

**Event flow (booking lifecycle):**

```
Book ticket ──▶ PENDING_PAYMENT ──▶ Razorpay order ──▶ webhook ──▶ CONFIRMED ──▶ booking.created (Kafka, after commit)
                                                                                      │
Cancel booking ──▶ release seats ──▶ persist refund ──▶ booking.cancelled (Kafka) ────┴─▶ waitlist/RAC promotion
```

---

## Tech Stack

| Layer        | Technology                                                        |
| ------------ | ----------------------------------------------------------------- |
| Language     | Java 21                                                           |
| Framework    | Spring Boot 4.1.0 (WebMVC, Data JPA, Security, Validation)        |
| Database     | PostgreSQL 17 with **pessimistic row locking**                    |
| Migrations   | Flyway (32 versioned migrations)                                  |
| Cache        | Redis 7 (cache-aside train search)                                |
| Messaging    | Apache Kafka 4.x (KRaft, single node) — `booking.created`, `booking.cancelled` topics |
| Auth         | JWT (jjwt 0.12.7) + refresh-token rotation                        |
| Payments     | Razorpay Java SDK 1.4.9 (test mode)                               |
| Build        | Maven (wrapper included), multi-stage Dockerfile                  |
| Testing      | JUnit 5, Testcontainers (PostgreSQL), Spring Security Test        |
| Other        | Lombok, SLF4J with MDC correlation ids                            |

---

## Domain Model: The Railway Problem

This system models the *actual* constraints of Indian Railways seat allocation:

- **Quota** — a seat-pool partition (`GN`, `LD`, `SS`, `DEF`, `TQ`). Each quota has its own allocation per train/coach and its own waiting pool. Bookings are validated against quota eligibility (age/gender for `SS`/`LD`, defence status for `DEF`).
- **Seat hold** — confirmed seats are held with a TTL and expired by a scheduled sweeper, so abandoned bookings do not leak seats forever.
- **RAC (Reservation Against Cancellation)** — a passenger gets a *semi-confirmed* status: they board, but may share a berth until chart preparation.
- **Waitlist** — passengers queue per schedule + quota; promotion respects the queue order and quota pools.
- **Chart preparation** — a CAS-based finalizer: exactly one request wins and freezes the manifest; losers are rejected rather than racing.

---

## Project Structure

The codebase is organized as **feature slices** rather than layers, so each package owns its controller → service → repository → entity:

```
src/main/java/com/soham/railway_reservation_engine/
├── auth/                 # Registration, login, refresh-token rotation
├── bookings/             # Booking creation (idempotent), retrieval, quota validation
├── cancellation/         # ChargeCalculator — sliding refund policy by time-to-departure
├── coach/                # Coach types & berth configuration
├── common/               # Enums, exceptions, shared utilities
├── config/               # Spring configuration (Redis, Kafka, timezone pinning)
├── controller/           # Home + health endpoints
├── kafka/                # Topic config, producers, consumers (promotion, notifications)
├── passenger/            # Passenger entity/repository
├── payment/              # Razorpay orders, webhooks, payment-failure rollback
├── pnrStateHistory/      # PNR lifecycle audit trail
├── quota/                # Quota entity, eligibility validator
├── quotaReservationPool/ # Per-quota seat counters (the anti-overbooking ledger)
├── quotaSeatAllocation/  # Seat↔quota allocation rows (pessimistically locked)
├── rac/                  # RAC queue management
├── refreshToken/         # Refresh token entity & revocation
├── refund/               # Refund records (audit trail for cancellations)
├── route/                # Train stops & sequence semantics
├── schedule/             # Journeys, chart preparation (CAS single-winner)
├── seat/                 # Seat holds, TTL expiry, pessimistic seat locking
├── security/             # JWT filter chain, user details
├── station/              # Station codes
├── train/                # Search (Redis cache-aside) & availability
├── user/                 # User entity (defence flag, roles)
└── waitlist/             # Waitlist queueing, RAC→confirmed promotion cascade
```

---

## Getting Started

### Prerequisites

- **JDK 21** (the project does not build with older JDKs)
- **Docker Desktop** (for PostgreSQL, Redis, Kafka)

### 1. Configure secrets

```bash
cp .env.example .env
```

Then fill in `.env`:

```dotenv
# Generate a fresh value: openssl rand -base64 64
JWT_SECRET=your-base64-jwt-secret

# Razorpay test-mode keys (https://dashboard.razorpay.com → Settings → API Keys)
RAZORPAY_KEY_ID=rzp_test_...
RAZORPAY_KEY_SECRET=...
RAZORPAY_WEBHOOK_SECRET=...
```

> `.env` is git-ignored. Never commit real secrets.

### 2. Run everything with Docker Compose

```bash
docker compose up --build
```

This starts all four services with health checks:

| Service   | Port  | Purpose                            |
| --------- | ----- | ---------------------------------- |
| app       | 8080  | Spring Boot API                    |
| postgres  | 5432  | Primary database (`railway_db`)    |
| redis     | 6379  | Train-search cache                 |
| kafka     | 9092  | Event bus (`booking.*` topics)     |

### 3. Run locally (without containerizing the app)

```bash
# JDK 21 required
$env:JAVA_HOME="C:\path\to\temurin-21"   # Windows PowerShell
docker compose up postgres redis kafka   # infra only
.\mvnw.cmd spring-boot:run
```

### 4. Smoke test

```bash
curl http://localhost:8080/
```

---

## Configuration

Configuration lives in `src/main/resources/application-dev.yml`. All secrets are externalized to environment variables (no defaults for secrets — the app fails fast if they are missing):

| Property                   | Env variable          | Default                    |
| -------------------------- | --------------------- | -------------------------- |
| `jwt.secret`               | `JWT_SECRET`          | *(required)*               |
| `razorpay.key-id`          | `RAZORPAY_KEY_ID`     | *(required)*               |
| `razorpay.key-secret`      | `RAZORPAY_KEY_SECRET` | *(required)*               |
| `razorpay.webhook-secret`  | `RAZORPAY_WEBHOOK_SECRET` | *(required)*           |
| `spring.datasource.username` | `DB_USERNAME`       | `postgres`                 |
| `spring.datasource.password` | `DB_PASSWORD`       | `postgres`                 |
| `spring.kafka.bootstrap-servers` | `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` |

---

## API Reference

All endpoints below are JSON over HTTP. Auth endpoints are unauthenticated; everything else requires a JWT `Authorization: Bearer <token>` header.

| Method | Path                                   | Auth | Description                                        |
| ------ | -------------------------------------- | ---- | -------------------------------------------------- |
| POST   | `/api/v1/auth/register`                | —    | Register a user                                    |
| POST   | `/api/v1/auth/login`                   | —    | Login, returns JWT + refresh token                 |
| POST   | `/api/v1/auth/refresh`                 | —    | Rotate refresh token for a new JWT                 |
| POST   | `/api/v1/auth/logout`                  | JWT  | Revoke the refresh token                           |
| GET    | `/api/v1/trains/search`                | JWT  | Search trains (Redis cache-aside)                  |
| GET    | `/api/v1/trains/{trainId}/availability`| JWT  | Live availability per schedule                     |
| POST   | `/api/v1/bookings`                     | JWT  | Book a ticket. **Requires `Idempotency-Key` header** |
| GET    | `/api/v1/bookings/{pnr}`               | JWT  | Fetch booking by PNR                               |
| POST   | `/api/v1/bookings/{pnr}/cancel`        | JWT  | Cancel booking; returns computed refund            |
| POST   | `/api/v1/schedules/{scheduleId}/prepare-chart` | JWT | Run chart preparation (CAS, single winner) |
| POST   | `/payment/initiate`                    | JWT  | Create a Razorpay order for a `PENDING_PAYMENT` booking |
| POST   | `/payment/webhook`                     | —    | Razorpay webhook (signature-verified)              |
| GET    | `/`                                    | —    | Home / health                                      |
| GET    | `/api/v1/admin/test`, `/api/v1/test`   | JWT  | Simple authenticated smoke checks                  |

> A browser-based payment sandbox is available at `/payment-test.html` — paste values from the `/payment/initiate` response into the two placeholder constants (keys are **never** hardcoded in the repo).

---

## Concurrency & Reliability Design

This is the heart of the project. Every seat race is resolved at the data layer:

1. **Pessimistic locking** — seat/quota-allocation rows are locked (`SELECT ... FOR UPDATE`) during booking and promotion, so two concurrent requests cannot allocate the same seat.
2. **Per-quota reservation pool** — `quota_reservation_pool` keeps live counters per schedule + quota; allocations decrement/increment the counters *inside the same lock scope*, preventing quota overdraw.
3. **Seat holds with TTL** — confirmed seats are held for a bounded window and swept by an expiry job, so abandoned bookings cannot leak capacity permanently.
4. **Idempotent creation** — an `Idempotency-Key` makes booking retries safe under network failures.
5. **Events published only after commit** — Kafka messages are sent via `TransactionSynchronization.afterCommit`, so consumers never observe rolled-back state.
6. **Promotion cascade, once per freed seat** — `booking.cancelled` carries `freedSeatCount`; the consumer runs waitlist → RAC → confirmed promotion for *each* released seat, in oldest-first, quota-aware order.
7. **CAS chart preparation** — the chart finalizer uses a compare-and-swap so exactly one process wins; losers get a clean "chart already prepared" outcome.
8. **Correlation ids** — every request threads a `correlationId` through MDC and into Kafka events for end-to-end traceability across producers and consumers.
9. **Payment-failure rollback** — if Razorpay rejects a payment, seat allocations are released and promotion is triggered, so capacity returns to the pool.

---

## Database & Migrations

Flyway manages a 32-step schema evolution (`V1__init.sql` → `V32__add_chart_preparing_schedule_status.sql`), including:

- **V1–V9** — core entities: stations, trains, routes, coaches, seats, schedules, users, refresh tokens
- **V10–V19** — bookings, passengers, payments, refunds, RAC, waitlist, quota seat allocation, PNR state history, indexes
- **V20–V32** — hardening: constraint strengthening, quota reservation pool, RAC/waitlist restructuring, defence flag, Razorpay payment columns, chart-preparing status

This history is itself an interview story: features were added incrementally without breaking existing data.

---

## Testing

| Test                                 | Scope                                                        |
| ------------------------------------ | ------------------------------------------------------------ |
| `QuotaEligibilityValidatorTest`      | Unit — age/gender/defence rules per quota code               |
| `ChargeCalculatorTest`               | Unit — sliding refund policy                                 |
| `PnrStateMachineTest`                | Unit — PNR lifecycle transitions                             |
| `BookingFlowIntegrationTest`         | Integration — end-to-end booking flow (Testcontainers)       |
| `Day22ConcurrencyTest`               | Load test — concurrent bookings against a shared seat pool   |

```bash
# Unit tests (no infra needed)
.\mvnw.cmd test "-Dtest=QuotaEligibilityValidatorTest,ChargeCalculatorTest,PnrStateMachineTest"

# Full build
.\mvnw.cmd clean verify
```

---

## Documentation

Educational Javadoc covers every package — domain terminology (PNR, quota, RAC, waitlist, chart), plus the advanced patterns used (pessimistic locking, strategy/state patterns, JWT chain, Kafka consumer groups, cache-aside, MDC correlation):

```bash
.\mvnw.cmd javadoc:javadoc "-Dmaven.javadoc.doclint=none"
# Output: target/reports/apidocs/index.html
```

---

## Roadmap

- [ ] Production secrets rotation + history scrub before public release
- [ ] Real (non-test) Razorpay refund execution via gateway API (refunds are currently persisted as `PENDING` audit records)
- [ ] Custom domain exceptions replacing raw `RuntimeException`s
- [ ] Multi-node Kafka + partition scaling for the booking topics
- [ ] `application-prod.yml` with production profiles