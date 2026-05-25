# construction-hrms-backend

<div align="center">

# 🏗️ Construction HR Management System

**Spring Boot backend for construction workforce ops — attendance, overtime, settlements.**

![Java](https://img.shields.io/badge/Java-17+-orange?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=flat-square&logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Supabase-4169E1?style=flat-square&logo=postgresql)
![Redis](https://img.shields.io/badge/Redis-optional-DC382D?style=flat-square&logo=redis)
![Maven](https://img.shields.io/badge/Maven-build-C71A36?style=flat-square&logo=apachemaven)

</div>

---

## What This Does

| Feature | Detail |
|---|---|
| Clock-in / Clock-out | Validated attendance with conflict + future-time checks |
| Overtime Engine | 1.5× (first 2h) → 2× beyond, 60h monthly cap enforced |
| Settlements | Atomic `@Transactional`, SMS fires only after commit |
| Active Workers | Redis-cached (16h TTL), graceful DB fallback |

---

## Quick Start

```bash
# Build
mvn clean package

# Run locally
mvn spring-boot:run

# Run with profile
java -jar target/app.jar \
  --spring.profiles.active=staging \
  --spring.datasource.password=$DB_PASS \
  --spring.redis.password=$REDIS_PASS
```

> Redis is **optional** — app degrades gracefully if unavailable.

---

## API

### Attendance
```
POST /api/attendance/clock-in       { workerId, siteId }
POST /api/attendance/clock-out      { workerId }
GET  /api/attendance/active         → Redis-backed active list
GET  /api/attendance/log?workerId=&page=0&size=20
```

### Overtime
```
GET  /api/overtime/summary/{workerId}?month=2026-05
POST /api/overtime/settle/{workerId}?month=2026-05
```

**Error shape:**
```json
{ "error": "WORKER_ALREADY_CLOCKED_IN", "message": "...", "timestamp": "..." }
```

---

## Architecture

```
controller/ → service/ → serviceImpl/ → repository/
                ↓               ↓
            event/         entity/ + dto/
                ↓
           listener/ (post-commit SMS)
```

**Key decisions:**

- `@EntityGraph` on all repo queries — N+1 eliminated
- `@TransactionalEventListener(AFTER_COMMIT)` — SMS never fires on rollback
- HikariCP: pool 20, max-lifetime 270s — tuned for Supabase limits
- CORS origins in YAML per env — no hardcoded values
- Custom `CacheErrorHandler` — Redis down ≠ app down

---

## Entities

```
Worker          → id, name, phone, designation, dailyWageRate, isActive
Site            → id, siteName, location, isActive
AttendanceLog   → worker_id, site_id, clockIn, clockOut, totalHours, overtimeHours, flagged
OvertimeEntry   → worker_id, log_id, yearMonth, hours, multiplier, amount, status{PENDING|SETTLED}
```

---

## Business Rules

**Clock-in:** worker active → no open log → not future time → create log + cache  
**Clock-out:** compute hours → apply OT multiplier → enforce 60h cap → flag if >16h → evict cache  
**Settle:** month must be closed → mark all PENDING→SETTLED atomically → event → SMS after commit

---

## Config Files

| File | Env |
|---|---|
| `application.yml` | Local |
| `application-staging.yml` | Staging |
| `application-prod.yml` | Production |

---

## Production Fixes (Part 2)

| Ticket | Fix |
|---|---|
| LF-201 CORS | Origins in YAML, env-specific |
| LF-202 Redis crash | `CacheErrorHandler` → silent DB fallback |
| LF-203 N+1 | `@EntityGraph` + pagination (default 20, max 100) |
| LF-204 Settlement | Single `@Transactional` + `AFTER_COMMIT` event for SMS |
| LF-205 Pool exhaustion | HikariCP tuned, external calls outside transactions |

---

## Assumptions

- No auth — bare endpoints
- SMS is logged only (no real provider)
- One shift per worker per day
- All timestamps UTC / ISO-8601
