# OMS Trading System — Architecture Document

> **Version:** 1.0.0 | **Last Updated:** 2026-04-15

---

## 1. System Overview

The OMS (Order Management System) Trading System is a **distributed, low-latency, high-throughput** capital market platform capable of handling **500K orders** and **2M trades**. It is built using microservices architecture on Java 17 + Spring Boot 3, communicating via REST APIs, WebSockets, and Apache Kafka.

---

## 2. Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         EXTERNAL CLIENTS                                    │
│                                                                             │
│   ┌─────────────────────┐              ┌──────────────────────┐             │
│   │  FIX Client          │              │   Browser (Angular)  │             │
│   │  (B2BITS / MiniFix)  │              │   http://localhost:  │             │
│   │  FIX 4.4 over TCP    │              │   4200               │             │
│   └──────────┬──────────┘              └──────────┬───────────┘             │
└──────────────┼──────────────────────────────────── ┼ ──────────────────────┘
               │ FIX 4.4 (TCP:9878)                  │ HTTP / WebSocket
               ▼                                      ▼
┌──────────────────────────┐        ┌─────────────────────────────────────┐
│   FIX Gateway Service    │        │         API Gateway (:8080)          │
│   QuickFIX/J Acceptor    │        │   Spring Cloud Gateway               │
│   :9878 / :9090 (mgmt)  │        │   Rate limiting, routing, CORS       │
└──────────┬───────────────┘        └──────┬───────────────────────────────┘
           │                               │ routes to...
           │ Kafka [order.created]         │
           ▼                               ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                         KAFKA EVENT BUS                                   │
│                                                                           │
│  Topics:  order.created | order.updated | order.cancelled                 │
│           trade.executed | trade.persisted                                │
│           market.data | pricing.update | exec.report | audit.event        │
│                                                                           │
│  12 partitions per topic | Retention: 7 days                             │
└──────┬──────────────┬──────────────┬─────────────────┬───────────────────┘
       │              │              │                  │
       ▼              ▼              ▼                  ▼
┌──────────────┐ ┌──────────┐ ┌──────────────┐ ┌──────────────────────┐
│  Matching    │ │  Trade   │ │  Audit /     │ │  WebSocket Gateway   │
│  Engine      │ │ Execution│ │  Persistence │ │  STOMP + SockJS      │
│  :8082       │ │  :8083   │ │  Service     │ │  :8085               │
│              │ │          │ │  :8088       │ │                      │
│ ConcurrentSkip│ │BlockingQ │ │  BlockingQ   │ │  → Angular UI        │
│ ListMap      │ │500K queue │ │  200K queue  │ │  real-time push      │
└──────┬───────┘ └────┬─────┘ └──────┬───────┘ └──────────────────────┘
       │              │              │
       │ trade.executed│             │ MySQL writes
       │              ▼              ▼
       │         ┌─────────┐   ┌────────────┐
       │         │  MySQL  │   │  MySQL     │
       │         │  trades │   │  audit_log │
       │         └─────────┘   └────────────┘
       ▼
┌──────────────────────────────────────────────────────────┐
│              Options Pricing Service (:8086)              │
│  Black-Scholes repricing on every trade.executed event   │
│  → Kafka [pricing.update] → WebSocket → Angular          │
└──────────────────────────────────────────────────────────┘

Supporting Services:
  Order Management  :8081  — lifecycle, in-memory store, async persist
  Reference Data    :8087  — SecurityMasterCache (preloaded), Customer lookup
  Market Data       :8084  — CSV polling, delta updates
  Notification      :8089  — event log aggregation
```

---

## 3. Service Inventory

| # | Service | Port | Technology | Responsibility |
|---|---|---|---|---|
| 1 | **FIX Gateway** | 9878/9090 | QuickFIX/J | FIX 4.4 acceptor, message parsing, ExecutionReport |
| 2 | **Order Management** | 8081 | Spring Boot, JPA | Order lifecycle, in-memory store, async persist |
| 3 | **Matching Engine** | 8082 | Spring Boot | Price-Time Priority matching, order book |
| 4 | **Trade Execution** | 8083 | Spring Boot, JPA | Trade booking, idempotency, async DB persist |
| 5 | **Market Data** | 8084 | Spring Boot | CSV polling, delta updates, Kafka producer |
| 6 | **WebSocket Gateway** | 8085 | STOMP/SockJS | Kafka→WebSocket bridge, real-time push |
| 7 | **Options Pricing** | 8086 | Spring Boot | Black-Scholes repricing, Greeks calculation |
| 8 | **Reference Data** | 8087 | Spring Boot, JPA | Security/Customer master, in-memory cache |
| 9 | **Persistence/Audit** | 8088 | Spring Boot, JPA | All-events audit trail, async BlockingQueue |
| 10 | **Notification** | 8089 | Spring Boot | Event aggregation and logging |
| 11 | **API Gateway** | 8080 | Spring Cloud Gateway | Routing, rate-limiting, CORS |

---

## 4. Key Design Decisions

### 4.1 In-Memory Order Store
- `ConcurrentHashMap<Long, OrderDTO>` — O(1) lookup by order ID on the hot path
- Snapshot persisted asynchronously via `BlockingQueue` (capacity: 100K)
- DB is the system of record; in-memory is the speed layer

### 4.2 Order Book Structure
```
Bids (descending):  ConcurrentSkipListMap<BigDecimal, PriceLevel>
Asks (ascending):   ConcurrentSkipListMap<BigDecimal, PriceLevel>
PriceLevel:         LinkedList<OrderDTO>  — FIFO time priority
```

### 4.3 Matching Algorithm — Price-Time Priority
1. If BUY: sweep asks starting from lowest price (best ask)
2. Match condition: `buy.price >= ask.price`
3. Trade price = **resting order price** (not aggressor price)
4. Partial fills: fill as much as possible, residual stays in book
5. Multi-level: continue matching across price levels until filled or no match

### 4.4 Async Persistence Pattern
```
FIX Callback (nanoseconds)
  → orderQueue.offer(order)    [non-blocking, O(1)]
  → return immediately

Background Thread (milliseconds)
  → drain up to 500 orders from queue
  → orderRepository.saveAll(batch)   [single DB round-trip]
```

### 4.5 FIX Protocol Flow
```
NewOrderSingle (FIX 4.4)
  → fromApp() callback
  → NewOrderSingleHandler.handle()
  → parse: ClOrdID(11), Symbol(55), Side(54), OrderQty(38), Price(44)
  → validate fields
  → publish to Kafka [order.created]
  → ExecutionReport(ExecType=NEW) sent back to FIX client
```

### 4.6 Black-Scholes Repricing
- Triggered on every `trade.executed` Kafka event
- Runs `parallelStream()` for all options on the underlying
- Formula: `C = S*N(d1) - K*e^(-rT)*N(d2)`
- Greeks: Delta, Gamma, Vega, Theta
- Output: `OptionPriceDTO` → Kafka `pricing.update` → WebSocket → Angular

---

## 5. Database Schema

| Table | Partitioning | Purpose |
|---|---|---|
| `orders` | RANGE (year) | All order records |
| `trades` | RANGE (year) | All trade records |
| `security_master` | None | Symbol catalog (preloaded to cache) |
| `customer_master` | None | Customer limits and config |
| `execution_reports` | None | FIX execution report audit |
| `audit_log` | None | All system events (JSON payload) |

---

## 6. Kafka Topics

| Topic | Producer | Consumers | Partitions |
|---|---|---|---|
| `order.created` | OMS, FIX Gateway | Matching Engine, Audit, WS Gateway | 12 |
| `order.updated` | OMS | Audit, WS Gateway, Notification | 12 |
| `order.cancelled` | OMS | Audit, WS Gateway | 6 |
| `trade.executed` | Matching Engine | Trade Execution, Options Pricing, Audit, WS Gateway | 12 |
| `trade.persisted` | Trade Execution | Notification | 6 |
| `market.data` | Market Data | WS Gateway | 6 |
| `pricing.update` | Options Pricing | WS Gateway | 6 |
| `exec.report` | OMS | FIX Gateway, WS Gateway | 6 |
| `audit.event` | All services | Audit Service | 6 |

---

## 7. Performance Targets

| Metric | Target | Mechanism |
|---|---|---|
| Tick-to-trade latency | < 1ms avg | In-memory store + async DB |
| Order throughput | 10–20/sec sustained, 500K total | ConcurrentHashMap + BlockingQueue |
| Trade throughput | 2M total | Batch 1000/insert via JPA saveAll |
| FIX reconnect | < 30s | ReconnectInterval=10 in cfg |
| UI update latency | < 100ms | Kafka → WS Gateway → Angular |
| DB batch insert | 500 orders / 1000 trades per batch | BlockingQueue drainTo() |

---

## 8. Resilience

| Failure | Recovery Mechanism |
|---|---|
| FIX session drop | Auto-reconnect (ReconnectInterval=10s), sequence gap via ResendRequest |
| Kafka consumer lag | Partition rebalancing, consumer group offset tracking |
| DB unavailable | Persistence queue buffers events (100K orders / 200K audit) |
| Service crash | Docker `restart: on-failure`, stateless design |
| Duplicate trades | IdempotencyService (in-memory + DB unique key check) |
| Clock skew | Snowflake ID uses system clock with backward-clock protection |

---

## 9. Technology Stack

| Layer | Technology | Version |
|---|---|---|
| Language | Java | 17 |
| Framework | Spring Boot | 3.2.0 |
| Build | Maven | 3.8+ |
| ORM | Spring Data JPA / Hibernate | 6.x |
| FIX Protocol | QuickFIX/J | 2.3.1 |
| Message Broker | Apache Kafka | 3.6 (CP 7.5) |
| Database | MySQL | 8.0 |
| WebSocket | STOMP over SockJS | Spring WebSocket |
| Frontend | Angular | 17 |
| UI Components | Angular Material | 17 |
| State | RxJS Subjects | 7.8 |
| Container | Docker / Docker Compose | v2 |
| JVM GC | ZGC | Java 17+ |
| Gateway | Spring Cloud Gateway | 2023.0 |

---

## 10. Deployment

```bash
# Step 1: Start infrastructure only
docker-compose -f docker-compose-infra.yml up -d

# Step 2: Build all JARs
mvn clean install -DskipTests

# Step 3: Start all microservices
docker-compose up -d

# Step 4: Run Angular UI locally
cd oms-ui && npm install && npm start
```
