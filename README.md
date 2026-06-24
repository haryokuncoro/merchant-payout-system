# Order Processing System

A Spring Boot-based order processing system that includes authentication, authorization, database migration, event publishing, and API documentation.

## Overview

This project is built with Spring Boot 3.5.14 and Java 21. It is designed as a backend service for order processing with support for:

- REST API endpoints
- Spring Security authentication and authorization
- PostgreSQL persistence via Spring Data JPA
- Kafka integration for event publishing
- Flyway database migrations
- JWT-based token handling
- OpenAPI / Swagger UI documentation

## Key Features

- Spring Boot web application
- Secure endpoints via Spring Security
- Data persistence with PostgreSQL
- Kafka messaging for asynchronous event processing
- Database migration management with Flyway
- JWT authentication support
- Async processing enabled with `@EnableAsync`
- API documentation via Springdoc OpenAPI

## Tech Stack

- Java 21
- Spring Boot 3.5.14
- Spring Web
- Spring Security
- Spring Validation
- Spring Data JPA
- PostgreSQL
- Apache Kafka (Spring Kafka)
- Flyway
- JSON Web Tokens (JJWT)
- Springdoc OpenAPI
- Gradle

## Requirements

- Java 21
- Gradle 8.x (wrapper included)
- PostgreSQL database
- Kafka broker (if event publishing is used)

## Build and Run

Use the Gradle wrapper included in the repository.

```bash
./gradlew clean build
java -jar build/libs/*.jar
```

## Running with Docker

Build the image from the project root:

```bash
docker build -t order-processing-system .
```

Run the container:

```bash
docker run -p 8080:8080 order-processing-system
```

## Configuration

Default configuration is loaded from `src/main/resources/application.properties`.

Important settings include:

- `spring.application.name=order-processing-system`
- `server.port=8080`
- `spring.profiles.active=local`

Database, Kafka, security, and JWT settings should be configured via application properties or environment variables as needed.

## Kafka Production Setup

This project includes a production-ready Kafka producer and consumer setup with the following patterns:

- `OrderEventPublisher` publishes `OrderCreatedEvent` messages to the `order.created` topic.
- Topics are created with `TopicBuilder` in `KafkaConfig`, using partitioning for throughput.
- Producer settings support strong delivery guarantees with:
  - `spring.kafka.producer.acks=all`
  - `spring.kafka.producer.retries=3`
  - `spring.kafka.producer.properties.enable.idempotence=true`
  - `spring.kafka.producer.compression-type=snappy`
  - batch and linger tuning for throughput
- Consumer setup includes:
  - manual acknowledgment via `manualAckFactory`
  - concurrency with a configured listener container factory
  - retryable topics and dead-letter topic support through `@RetryableTopic`
- Consumer properties include:
  - `spring.kafka.consumer.auto-offset-reset=earliest`
  - `spring.kafka.consumer.enable-auto-commit=false`
  - `spring.kafka.consumer.max-poll-records=50`
  - trusted JSON package deserialization for `com.haryokuncoro.ops.dto`

For production, override the local properties with:

- `spring.kafka.bootstrap-servers` pointing to your Kafka cluster
- security settings such as SASL and SSL if required
- topic replication factors and partition counts to match your availability requirements

## API Documentation

The project includes Springdoc OpenAPI support. After starting the application, access the Swagger UI at:

- `http://localhost:8080/swagger-ui/index.html`

## Notes

- The application package is `com.haryokuncoro.ops`.
- The project currently targets local profile execution by default.
- Tests can be run with:

```bash
./gradlew test
```

## Create Order
```json
{
  "orderNo": "0001",
  "merchantId": "1bbbbd6f-fa57-4ed7-866f-f3615f0dbc6c",
  "amount": 50000,
  "currency": "USD",
  "stripePaymentIntentId": "pi_test123",
  "paymentStatus": "PAID",
  "paidAt": "2026-06-23T10:00:00Z"
}
```

## Stripe Webhook

```json
{
  "id": "evt_1NqK9lJ9XYZAbCdE2G3h4I5j",
  "object": "event",
  "api_version": "2023-10-16",
  "created": 1719234999,
  "type": "payout.paid",
  "livemode": false,
  "pending_webhooks": 1,
  "request": {
    "id": null,
    "idempotency_key": null
  },
  "data": {
    "object": {
      "id": "po_1NqK9lJ9XYZAbCdE2G3h4I5j",
      "object": "payout",
      "amount": 100000,
      "currency": "usd",
      "arrival_date": 1719321600,
      "automatic": true,
      "balance_transaction": "txn_1NqK9lJ9XYZAbCdE2G3h",
      "created": 1719234900,
      "description": "STRIPE PAYOUT",
      "destination": "ba_1NqK9lJ9XYZAbCdE2G3h",
      "failure_balance_transaction": null,
      "failure_code": null,
      "failure_message": null,
      "livemode": false,
      "method": "standard",
      "original_payout": null,
      "reversed_by": null,
      "source_type": "card",
      "statement_descriptor": null,
      "status": "paid",
      "type": "bank_account",
      "metadata": {}
    }
  }
}
```