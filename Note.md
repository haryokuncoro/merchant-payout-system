## What I Built

• JWT Authentication & Authorization

* User registration and login
* JWT access token authentication
* Protected REST APIs

---

• Merchant Management

* Merchant onboarding
* Merchant search & filtering
* Merchant status management
* Pagination & sorting

---

• Order Management

* Create payment orders
* Search orders
* Payment status tracking
* Stripe PaymentIntent integration

---

• Payout Management

* Generate merchant payouts
* Calculate payout periods
* Payout history
* Background payout job publishing

---

• Fee Configuration

Supports configurable:

* Platform Fee
* Stripe Fee
* Tax
* Refund Fee
* Chargeback Fee
* Reserve Hold
* Reserve Release

---

• Stripe Integration

* Stripe Webhook endpoint
* Webhook verification
* Payment event processing

---

• Kafka Event Processing

* Asynchronous payout jobs
* Event-driven architecture
* Dead Letter Queue (DLQ)
* Failed event replay

---

• Admin Features

* Seed demo data
* Replay failed Kafka events
* Operational endpoints

---

## Tech Stack

Backend

* Java 21
* Spring Boot
* Spring Security
* Spring Data JPA
* Hibernate

Database

* PostgreSQL

Messaging

* Apache Kafka

Payment

* Stripe API
* Stripe Webhooks

Documentation

* OpenAPI / Swagger

Authentication

* JWT

Build

* Maven

---

## Highlights

✅ RESTful API Design

✅ JWT Authentication

✅ Kafka Event Processing

✅ Stripe Integration

✅ Pagination & Filtering

✅ Fee Configuration Engine

✅ Dead Letter Queue Handling

✅ Webhook Processing

✅ Production-style Project Structure

---

## Architecture

```text
Client
   │
Spring Boot REST API
   │
 ├── Authentication
 ├── Merchant Service
 ├── Order Service
 ├── Payout Service
 ├── Fee Engine
 ├── Stripe Webhook
 │
 ├── PostgreSQL
 └── Kafka
        │
   Payout Jobs
        │
    DLQ Replay
```
