# OMS Trading System — Quick Start Guide

## Prerequisites
- Java 17+, Maven 3.8+, Docker, Docker Compose v2, Node.js 20+

## 1. Run Environment Check
```bash
cd env-check
mvn package -DskipTests
java -cp target/env-check-1.0.0-SNAPSHOT.jar com.oms.envcheck.EnvCheck
```

## 2. Build All Services
```bash
cd oms-trading-system
mvn clean install -DskipTests
```

## 3. Start Infrastructure (Kafka + MySQL)
```bash
docker-compose up -d zookeeper kafka mysql kafka-ui
# Wait ~30s for health checks to pass
```

## 4. Start All Microservices
```bash
docker-compose up -d
```

## 5. Start Angular UI (dev mode)
```bash
cd oms-ui
npm install
npm start
# UI available at http://localhost:4200
```

## Service URLs
| Service | URL |
|---|---|
| Angular UI | http://localhost:4200 |
| API Gateway | http://localhost:8080 |
| Order Management | http://localhost:8081 |
| Matching Engine | http://localhost:8082 |
| Trade Execution | http://localhost:8083 |
| Market Data | http://localhost:8084 |
| WebSocket Gateway | http://localhost:8085 |
| Options Pricing | http://localhost:8086 |
| Reference Data | http://localhost:8087 |
| Persistence/Audit | http://localhost:8088 |
| Notification | http://localhost:8089 |
| FIX Gateway (TCP) | localhost:9878 |
| Kafka UI | http://localhost:9093 |
| MySQL | localhost:3306 |

## FIX Connectivity (MiniFix / B2BITS)
```
Host: localhost
Port: 9878
SenderCompID: TRADING_CLIENT
TargetCompID: OMS_SERVER
BeginString: FIX.4.4
```

## API Examples
```bash
# Submit an order
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"clOrdId":"ORD-001","symbol":"AAPL","side":"BUY","orderType":"LIMIT","quantity":100,"price":150.00}'

# Get order book
curl http://localhost:8082/api/matching/books/AAPL

# Get latency stats
curl http://localhost:8082/api/matching/latency

# Get trades by symbol
curl "http://localhost:8080/api/trades/symbol/AAPL?page=0&size=50"

# Validate symbol
curl "http://localhost:8080/api/ref-data/symbols/validate?symbol=AAPL"
```

## JVM Tuning (Production)
```bash
export JAVA_OPTS="-XX:+UseZGC -Xmx16g -Xms4g -XX:ConcGCThreads=4 -XX:+AlwaysPreTouch"
```

## Architecture
```
FIX Client → FIX Gateway (9878)
           → Kafka [order.created]
             → Matching Engine (Price-Time Priority)
               → Kafka [trade.executed]
                 → Trade Service (idempotent, async persist)
                 → Options Pricing (Black-Scholes)
                 → Audit Service (async BlockingQueue → MySQL)
                 → WebSocket Gateway → Angular UI
```
