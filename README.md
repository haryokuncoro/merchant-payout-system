# SpringBoot API

A Spring Boot backend exposing authentication, order, payout, webhook, and admin/DLQ management endpoints.

## Base URL

```
http://localhost:8080
```

## Authentication

Most endpoints require a Bearer JWT token in the `Authorization` header (configured via the `bearerAuth` security scheme):

```
Authorization: Bearer <your-jwt-token>
```

Obtain a token via `/auth/login` after registering with `/auth/register`.

## Response Envelope

Most endpoints (except the Stripe webhook and seed-orders endpoints) return a common `ApiResponse` wrapper:

```json
{
  "success": true,
  "message": "string",
  "data": {},
  "timestamp": "2026-06-23T10:00:00Z"
}
```

The `data` field type varies per endpoint (`string`, `void`/object, a map, or a list — see below).

---

## Endpoints

### Auth

#### `POST /auth/register`
Register a new user.

**Request body** (`RegisterRequest`):
```json
{
  "email": "admin@mail.com",
  "password": "Admin123!"
}
```
| Field | Type | Required |
|---|---|---|
| email | string | yes |
| password | string | yes |

**Response:** `ApiResponseVoid` — `200 OK`

---

#### `POST /auth/login`
Authenticate and receive a JWT.

**Request body** (`LoginRequest`):
```json
{
  "email": "admin@mail.com",
  "password": "Admin123!"
}
```

**Response:** `ApiResponseString` — `data` contains the JWT token string.

---

#### `GET /auth/me`
Get the currently authenticated user's profile. Requires `Authorization` header.

**Response:** `ApiResponseMap` — `data` is a key/value map of user attributes.

---

### Orders

#### `POST /api/orders`
Create a new order.

**Request body** (`CreateOrderRequest`):
```json
{
  "orderNo": "string",
  "merchantId": "uuid",
  "amount": 0,
  "currency": "USD",
  "stripePaymentIntentId": "pi_test0001",
  "paymentStatus": "PAID",
  "paidAt": "2026-06-23T10:00:00Z"
}
```
| Field | Type | Notes |
|---|---|---|
| orderNo | string | |
| merchantId | string (uuid) | |
| amount | number | |
| currency | string | default `USD` |
| stripePaymentIntentId | string | default `pi_test0001` |
| paymentStatus | string enum | `PENDING`, `PAID`, `FAILED`, `REFUNDED` (default `PAID`) |
| paidAt | string (date-time) | e.g. `2026-06-23T10:00:00Z` |

**Response:** `ApiResponseString` — `200 OK`

---

### Payouts

#### `POST /api/payouts`
Create a payout for a merchant over a given period.

**Request body** (`CreatePayoutRequest`):
```json
{
  "merchantId": "uuid",
  "periodStart": "2026-06-01",
  "periodEnd": "2026-06-30"
}
```

**Response:** `ApiResponseString`

---

#### `POST /api/payouts/jobs`
Publish payout jobs for all merchants over a given period.

**Request body** (`CreatePayoutJobRequest`):
```json
{
  "periodStart": "2026-06-01",
  "periodEnd": "2026-06-30"
}
```

**Response:** `ApiResponseString`

---

### Webhooks

#### `POST /api/webhooks/stripe`
Receives Stripe webhook events.

**Headers:**
| Name | Required | Description |
|---|---|---|
| `Stripe-Signature` | yes | Signature used to verify the webhook payload |

**Request body:** raw Stripe event payload (string/JSON).

**Example payload** (`payout.paid` event):
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

**Response:** `200 OK` (no body schema defined).

---

### Admin — Seeding

#### `POST /api/admin/seed/orders`
Seeds sample order data.

**Response:** plain `string` — `200 OK`

#### `POST /api/admin/seed/merchants`
Seeds sample merchant and fee-config data.

**Response:** `SeedResponse`
```json
{
  "merchantCount": 0,
  "feeConfigCount": 0
}
```

---

### Admin — Dead Letter Queue (DLQ)

#### `GET /api/admin/dlq`
List failed events, optionally filtered by topic.

**Query params:**
| Name | Type | Required |
|---|---|---|
| topic | string | no |

**Response:** `ApiResponseListFailedEvent` — `data` is a list of `FailedEvent`:
```json
{
  "id": "uuid",
  "createdAt": "2026-06-23T10:00:00Z",
  "updatedAt": "2026-06-23T10:00:00Z",
  "topic": "string",
  "eventId": "string",
  "payload": "string",
  "errorMessage": "string",
  "retryCount": 0,
  "status": "FAILED"
}
```
`status` enum: `FAILED`, `REPLAYING`, `REPLAYED`, `PERMANENTLY_FAILED`

#### `POST /api/admin/dlq/replay/{eventId}`
Replays a failed event by its event ID.

**Path params:**
| Name | Type | Required |
|---|---|---|
| eventId | string | yes |

**Response:** `ApiResponseString`

---

## Schemas Reference

| Schema | Description |
|---|---|
| `RegisterRequest` | Email + password for registration |
| `LoginRequest` | Email + password for login |
| `CreateOrderRequest` | Order creation payload |
| `CreatePayoutRequest` | Single merchant payout creation |
| `CreatePayoutJobRequest` | Bulk payout job creation |
| `SeedResponse` | Counts of seeded merchants/fee configs |
| `FailedEvent` | A DLQ entry representing a failed event |
| `ApiResponseVoid` / `ApiResponseString` / `ApiResponseMap` / `ApiResponseListFailedEvent` | Generic response envelopes with varying `data` types |

## Security Scheme

| Name | Type | Scheme | Format |
|---|---|---|---|
| `bearerAuth` | http | bearer | JWT |

Applied globally to all endpoints unless overridden.