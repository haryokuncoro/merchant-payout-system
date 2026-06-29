# Merchant Payout System API

A REST API for managing merchants, fee configurations, billing orders, and payouts — including Stripe webhook ingestion and a dead-letter-queue (DLQ) admin interface for replaying failed events.

- **Base URL:** `http://localhost:8080`
- **API spec version:** 1.0
- **Auth:** Bearer JWT (`Authorization: Bearer <token>`) on all endpoints except `/api/auth/**` and webhooks

---

## Table of Contents

- [Authentication](#authentication)
- [Merchants](#merchants)
- [Fee Configs](#fee-configs)
- [Orders](#orders)
- [Payouts](#payouts)
- [Stripe Webhooks](#stripe-webhooks)
- [Admin: Seeding](#admin-seeding)
- [Admin: Dead Letter Queue (DLQ)](#admin-dead-letter-queue-dlq)
- [Schemas](#schemas)
- [Common Response Envelope](#common-response-envelope)

---

## Authentication

### `POST /api/auth/register`

Register a new user.

**Request body** (`RegisterRequest`):

```json
{
  "email": "admin@mail.com",
  "password": "Admin123!"
}
```

| Field    | Type   | Required | Notes              |
|----------|--------|----------|---------------------|
| email    | string | yes      | defaults to example above |
| password | string | yes      | defaults to example above |

**Response:** `ApiResponse`

### `POST /api/auth/login`

Authenticate and obtain a JWT.

**Request body** (`LoginRequest`): same shape as `RegisterRequest`.

**Response:** `ApiResponse` (token expected inside `data`)

Use the returned token as `Authorization: Bearer <token>` for all subsequent calls.

---

## Merchants

### `GET /api/merchants`

List merchants with optional filters and pagination.

| Query param      | Type    | Default     | Description              |
|------------------|---------|-------------|---------------------------|
| name             | string  | —           | filter by merchant name   |
| email            | string  | —           | filter by email           |
| stripeAccountId  | string  | —           | filter by Stripe account  |
| page             | integer | `0`         | page index                |
| size             | integer | `10`        | page size                 |
| sortBy           | string  | `createdAt` | sort field                |
| direction        | string  | `desc`      | `asc` or `desc`           |

### `POST /api/merchants`

Create a merchant.

**Request body** (`Merchant`):

```json
{
  "merchantCode": "string",
  "merchantName": "string",
  "stripeAccountId": "string",
  "email": "string",
  "phone": "string",
  "status": "ACTIVE"
}
```

`status` enum: `ACTIVE`, `INACTIVE`, `SUSPENDED`

### `GET /api/merchants/{id}`

Fetch a merchant by UUID.

### `PUT /api/merchants/{id}`

Update a merchant by UUID. Body: `Merchant`.

### `DELETE /api/merchants/{id}`

Delete a merchant by UUID.

---

## Fee Configs

Fee configs define platform/Stripe/tax fees etc. applied per merchant over an effective date range.

**Fee type enum** (shared across create/update):
`PLATFORM_FEE`, `STRIPE_FEE`, `TAX`, `ADJUSTMENT`, `REFUND_FEE`, `CHARGEBACK_FEE`, `RESERVE_HOLD`, `RESERVE_RELEASE`

### `GET /api/fee-configs`

List fee configs.

| Query param | Type    | Default     | Description           |
|-------------|---------|-------------|-------------------------|
| merchantId  | uuid    | —           | filter by merchant     |
| active      | boolean | —           | filter by active flag  |
| page        | integer | `0`         | page index              |
| size        | integer | `10`        | page size               |
| sortBy      | string  | `createdAt` | sort field              |
| direction   | string  | `desc`      | `asc` or `desc`         |

### `POST /api/fee-configs`

Create a fee config.

**Request body** (`CreateFeeConfigRequest`) — required: `merchantId`, `feeType`, `feeValue`, `active`, `effectiveFrom`

```json
{
  "merchantId": "uuid",
  "feeType": "PLATFORM_FEE",
  "feeValue": 0.0,
  "active": true,
  "effectiveFrom": "2026-06-23T10:00:00Z",
  "effectiveTo": "2026-12-31T23:59:59Z"
}
```

### `GET /api/fee-configs/{id}`

Fetch a fee config by UUID.

### `PUT /api/fee-configs/{id}`

Update a fee config. **Request body** (`UpdateFeeConfigRequest`) — required: `feeType`, `feeValue`, `active`, `effectiveFrom`. Same shape as create, minus `merchantId`.

### `DELETE /api/fee-configs/{id}`

Delete a fee config by UUID.

---

## Orders

Billing orders represent paid (or pending/failed/refunded) charges tied to a merchant and Stripe Payment Intent.

### `GET /api/orders`

List billing orders.

| Query param            | Type    | Default     | Description                  |
|-------------------------|---------|-------------|-------------------------------|
| merchantId              | uuid    | —           | filter by merchant            |
| orderNo                 | string  | —           | filter by order number        |
| stripePaymentIntentId   | string  | —           | filter by Stripe PI id        |
| page                    | integer | `0`         | page index                    |
| size                    | integer | `10`        | page size                      |
| sortBy                  | string  | `createdAt` | sort field                     |
| direction               | string  | `desc`      | `asc` or `desc`                |

### `POST /api/orders`

Create a billing order.

**Request body** (`CreateOrderRequest`):

```json
{
  "orderNo": "string",
  "merchantId": "uuid",
  "amount": 0.0,
  "currency": "USD",
  "stripePaymentIntentId": "pi_test0001",
  "paymentStatus": "PAID",
  "paidAt": "2026-06-23T10:00:00Z"
}
```

`paymentStatus` enum: `PENDING`, `PAID`, `FAILED`, `REFUNDED` (default `PAID`)

---

## Payouts

### `GET /api/payouts`

List payouts.

| Query param | Type    | Default     | Description           |
|-------------|---------|-------------|-------------------------|
| merchantId  | uuid    | —           | filter by merchant     |
| status      | string  | —           | filter by payout status|
| page        | integer | `0`         | page index              |
| size        | integer | `10`        | page size               |
| sortBy      | string  | `createdAt` | sort field              |
| direction   | string  | `desc`      | `asc` or `desc`         |

### `POST /api/payouts`

Create a single payout for a merchant over a date range.

**Request body** (`CreatePayoutRequest`):

```json
{
  "merchantId": "uuid",
  "periodStart": "2026-06-01",
  "periodEnd": "2026-06-30"
}
```

### `POST /api/payouts/jobs`

Publish a batch payout job for **all merchants** over a given period (likely queues async per-merchant payout generation).

**Request body** (`CreatePayoutJobRequest`):

```json
{
  "periodStart": "2026-06-01",
  "periodEnd": "2026-06-30"
}
```

---

## Stripe Webhooks

### `POST /api/webhooks/stripe`

Receives Stripe webhook events. **Not protected by bearer auth** — instead verified via Stripe's signature header.

**Headers:**

| Header             | Required | Description                          |
|---------------------|----------|----------------------------------------|
| `Stripe-Signature`  | yes      | Stripe webhook signature for verification |

**Body:** raw event payload (string — typically the raw JSON body Stripe sends, needed for signature verification).

**Sample request body** (`payout.paid` event):

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

> Note: `amount` is in the smallest currency unit (e.g. cents) — `100000` = `$1,000.00`. The `Stripe-Signature` header must be computed against the **raw, unparsed** body for verification to succeed.

**Response:** `200 OK` with no body on success.

---

## Admin: Seeding

Utility endpoints for populating test/demo data.

### `POST /api/admin/seed/merchants`

Seeds sample merchants (and related fee configs).

**Response** (`SeedResponse`):

```json
{
  "merchantCount": 0,
  "feeConfigCount": 0
}
```

### `POST /api/admin/seed/orders`

Seeds sample billing orders.

**Response:** plain string (status/summary message).

---

## Admin: Dead Letter Queue (DLQ)

Endpoints for inspecting and recovering events that failed processing (e.g. from Kafka/queue consumers).

### `GET /api/admin/dlq`

List dead-lettered events.

| Query param | Type   | Required | Description           |
|-------------|--------|----------|------------------------|
| topic       | string | no       | filter by source topic |

### `POST /api/admin/dlq/replay/{eventId}`

Replay (reprocess) a specific dead-lettered event by its `eventId`.

---

## Schemas

### `Merchant`

| Field             | Type            |
|-------------------|------------------|
| id                | uuid             |
| createdAt         | date-time        |
| updatedAt         | date-time        |
| merchantCode      | string           |
| merchantName      | string           |
| stripeAccountId   | string           |
| email             | string           |
| phone             | string           |
| status            | `ACTIVE` \| `INACTIVE` \| `SUSPENDED` |

### `ApiResponse`

The standard response envelope used by nearly every endpoint. See [Common Response Envelope](#common-response-envelope).

### `CreateFeeConfigRequest` / `UpdateFeeConfigRequest`

| Field          | Type      | Required (create) | Required (update) |
|----------------|-----------|---------------------|----------------------|
| merchantId     | uuid      | ✅                  | n/a (path-scoped)    |
| feeType        | enum      | ✅                  | ✅                   |
| feeValue       | number    | ✅                  | ✅                   |
| active         | boolean   | ✅                  | ✅                   |
| effectiveFrom  | date-time | ✅                  | ✅                   |
| effectiveTo    | date-time | —                   | —                    |

### `CreateOrderRequest`

| Field                  | Type      | Default        |
|------------------------|-----------|----------------|
| orderNo                | string    | —              |
| merchantId             | uuid      | —              |
| amount                 | number    | —              |
| currency               | string    | `USD`          |
| stripePaymentIntentId  | string    | `pi_test0001`  |
| paymentStatus          | enum      | `PAID`         |
| paidAt                 | date-time | —              |

### `CreatePayoutRequest`

| Field       | Type | 
|-------------|------|
| merchantId  | uuid |
| periodStart | date |
| periodEnd   | date |

### `CreatePayoutJobRequest`

| Field       | Type |
|-------------|------|
| periodStart | date |
| periodEnd   | date |

### `RegisterRequest` / `LoginRequest`

| Field    | Type   | Required |
|----------|--------|----------|
| email    | string | ✅       |
| password | string | ✅       |

### `SeedResponse`

| Field          | Type    |
|----------------|---------|
| merchantCount  | integer |
| feeConfigCount | integer |

---

## Common Response Envelope

Most endpoints (everything except seed/orders and the Stripe webhook) wrap their payload in an `ApiResponse`:

```json
{
  "success": true,
  "message": "string",
  "data": { },
  "timestamp": "2026-06-23T10:00:00Z"
}
```

| Field     | Type      | Description                                   |
|-----------|-----------|------------------------------------------------|
| success   | boolean   | whether the request succeeded                  |
| message   | string    | human-readable status/error message            |
| data      | object    | the actual payload (shape varies per endpoint) |
| timestamp | date-time | server time the response was generated         |

---

## Authentication Header

Once you have a token from `/api/auth/login`, include it on every other request:

```
Authorization: Bearer <your-jwt-token>
```

Defined via the `bearerAuth` security scheme (HTTP bearer, JWT format) and applied globally to the API except where explicitly excluded (registration, login, Stripe webhook).