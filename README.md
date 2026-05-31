# Nakitera Case Study

A Spring Boot Java backend for a simplified brokerage and order-matching system.

## Overview

This project manages customers, assets, and orders for a small brokerage-style workflow.

The application supports:

- user registration
- customer creation
- asset listing
- order creation
- order cancellation
- pending order matching
- Kafka-based reservation and settlement

It also uses an inbox/outbox pattern to make asynchronous event handling more reliable.

## Tech Stack

- Java 21
- Spring Boot 4
- Spring Security
- Spring Data JPA
- H2 Database
- Flyway
- Apache Kafka
- Redis
- Swagger UI / Springdoc OpenAPI
- JUnit 5 + Mockito
- Docker Compose

## Main Features

- HTTP Basic authentication
- `ROLE_ADMIN` and `ROLE_USER` authorization
- automatic customer creation during registration
- initial TRY asset creation for new customers
- buy and sell order creation
- order cancellation
- manual order matching
- self-match prevention
- Kafka-based asset reservation and settlement
- Redis-based rate limiting
- Swagger UI and H2 console support

## Business Flow

1. A user or admin creates an order.
2. The order is stored as `PENDING`.
3. An outbox event is created in the database.
4. A scheduled publisher sends the event to Kafka.
5. Asset consumers reserve or release balances.
6. If reservation fails, the order is marked as `REJECTED`.
7. Admin triggers order matching.
8. Matching creates matched-order outbox events.
9. Settlement updates buyer and seller balances asynchronously.

## Domain Rules

### Customers and Assets

- A newly created customer automatically receives a `TRY` asset.
- Initial TRY amount is `5000`.
- Asset records are unique by `customer + assetName`.

### Orders

- Orders can be `BUY` or `SELL`.
- Available statuses:
  - `PENDING`
  - `MATCHED`
  - `CANCELED`
  - `REJECTED`
- `TRY` cannot be used as an order asset.
- Orders are created in `PENDING` state.

### Matching

Current matching implementation works with these rules:

- only `PENDING` orders are matched
- orders are grouped by `assetName + size`
- buy orders are prioritized by highest price
- sell orders are prioritized by lowest price
- a match requires `buyPrice >= sellPrice`
- self-match is blocked
- only exact-size matches are supported
- partial fill logic does not exist

Important note:

- two orders with the same asset but different sizes will not match each other

## Event-Driven Architecture

The application uses three Kafka topics:

- `ORDER_EVENT`
- `ASSET_EVENT`
- `MATCHED_ORDER_EVENT`

### Order Event Flow

`ORDER_CREATED`

- BUY orders reserve TRY balance
- SELL orders reserve stock balance

`ORDER_CANCELED`

- BUY orders release reserved TRY
- SELL orders release reserved stock

### Reservation Failure Flow

If reservation fails:

1. an `ASSET_RESERVATION_FAILED` outbox event is created
2. the event is published to `ASSET_EVENT`
3. the order service consumes it
4. the related order is marked as `REJECTED`

### Match Settlement Flow

If matching succeeds:

1. a `MatchedOrderOutbox` record is created
2. the event is published to `MATCHED_ORDER_EVENT`
3. the asset service consumes it
4. buyer and seller balances are settled

Settlement behavior:

- buyer pays using the sell price
- if reserved BUY amount is higher than execution cost, the difference is refunded
- buyer receives the stock asset
- seller receives TRY
- seller stock amount decreases

## Security

The project uses HTTP Basic authentication.

### Public Endpoints

- `/auth/**`
- `/swagger-ui/**`
- `/swagger-ui.html`
- `/v3/api-docs/**`
- `/h2-console`

### Admin-Only Endpoints

- `POST /customer/create`
- `PATCH /order/match`

### Authenticated Endpoints

- all remaining business endpoints

### Authorization Behavior

- `ROLE_ADMIN` can act across customers
- `ROLE_USER` is automatically scoped to its own customer
- a regular user cannot cancel another user's order
- admin requests require `customerId` where applicable

## Default Admin User

Flyway creates a default admin user:

- username: `admin`
- password: `123`

## API Summary

### Auth

#### `POST /auth/create`

Creates a new user and automatically creates a linked customer.

Example request:

```json
{
  "username": "alice",
  "password": "secret1",
  "confirmPassword": "secret1"
}
```

### Customer

#### `POST /customer/create`

Creates a customer with an initial TRY asset.

### Asset

#### `GET /asset/list?page=0&size=10&customerId=1`

Returns customer assets.

Notes:

- admin can query by `customerId`
- regular users see their own assets

### Order

#### `POST /order/create`

Creates a buy or sell order.

Example request:

```json
{
  "customerId": 1,
  "assetName": "THY",
  "orderSide": "BUY",
  "size": 5,
  "price": 100
}
```

#### `PATCH /order/cancel?orderId=1`

Cancels a pending order.

#### `PATCH /order/match`

Matches pending orders. Admin only.

#### `GET /order/list?page=0&size=10&customerId=1`

Returns customer orders within an optional date range.

## Validation and Error Handling

The application validates:

- blank usernames
- short passwords
- password confirmation mismatch
- blank asset name
- missing order side
- non-positive size
- non-positive price
- missing `customerId` for admin-scoped requests

Common HTTP statuses:

- `200` success
- `201` created
- `400` validation or business rule failure
- `401` authentication required
- `403` access denied
- `404` entity not found
- `409` duplicate username
- `429` rate limit exceeded

Current error responses are mostly plain text.

## Rate Limiting

Redis-backed rate limiting is enabled through a servlet filter.

Current settings:

- limit: `10`
- window: `10 seconds`

Excluded paths:

- `/h2-console`
- `/actuator`

## Database

The project uses H2 in-memory database.

Connection info:

- JDBC URL: `jdbc:h2:mem:testdb`
- username: `sa`
- password: empty

Main tables:

- `users`
- `customer`
- `asset`
- `orders`
- `order_outbox`
- `order_inbox`
- `matched_order_outbox`
- `matched_order_inbox`

Because H2 is in-memory, data is lost when the app stops.

## Profiles and Configuration

### `application.yaml`

- default profile is `dev`

### `application-dev.yaml`

Used for local backend execution.

- Kafka: `localhost:9092`
- Redis: `localhost:6379`

### `application-docker.yaml`

Used for full containerized execution.

- Kafka: `kafka:9092`
- Redis: `redis:6379`

## Running the Project

### Option 1: Run Backend Locally

Start Kafka and Redis only:

```bash
docker compose -f docker-compose.local.yml up
```

Then run the backend locally:

```bash
./mvnw spring-boot:run
```

You can also run it from IntelliJ with the `dev` profile.

### Option 2: Run Everything in Docker

```bash
docker compose -f docker-compose.full.yml up --build
```

This starts:

- Kafka
- Redis
- backend

## Swagger and H2 Console

Swagger UI:

- `http://localhost:8080/swagger-ui/index.html`

OpenAPI JSON:

- `http://localhost:8080/v3/api-docs`

H2 Console:

- `http://localhost:8080/h2-console`

H2 connection settings:

- Driver: `org.h2.Driver`
- JDBC URL: `jdbc:h2:mem:testdb`
- User: `sa`
- Password: empty

## Running Tests

```bash
./mvnw test
```

The project currently includes unit tests for:

- order creation
- order cancellation
- order matching
- customer creation
- user registration
- asset reservation
- asset release
- reservation failure handling

## Project Structure

```text
src/main/java/com/ardao/nakitera_case_study
├── annotation
├── config
├── controller
├── entity
├── enums
├── event
│   ├── consumer
│   ├── dto
│   └── publisher
├── exception
├── model
├── repository
├── request
├── response
├── service
└── util
```

## Current Limitations

- no partial fills
- matching only works for exact `assetName + size`
- H2 is development-only
- scheduled publishers run every 5 seconds
- Docker image build still performs a full Maven package


