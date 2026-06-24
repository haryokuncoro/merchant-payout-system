-- =====================================================
-- V1__create_payout_system.sql
-- PostgreSQL
-- =====================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =====================================================
-- MERCHANTS
-- =====================================================

CREATE TABLE merchants (
                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                           merchant_code VARCHAR(50) NOT NULL UNIQUE,
                           merchant_name VARCHAR(255) NOT NULL,

                           stripe_account_id VARCHAR(255),
                           email VARCHAR(255),
                           phone VARCHAR(50),

                           status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP


);

CREATE INDEX idx_merchants_code
    ON merchants(merchant_code);

-- =====================================================
-- BILLING ORDERS
-- =====================================================

CREATE TABLE billing_orders (
                                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                                order_no VARCHAR(100) NOT NULL,

                                merchant_id UUID NOT NULL,

                                amount NUMERIC(18,2) NOT NULL,
                                currency VARCHAR(10) NOT NULL DEFAULT 'USD',

                                stripe_payment_intent_id VARCHAR(255),
                                payment_status VARCHAR(30) NOT NULL,

                                paid_at TIMESTAMP,

                                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                CONSTRAINT fk_billing_orders_merchant
                                    FOREIGN KEY (merchant_id)
                                    REFERENCES merchants(id)


);

CREATE INDEX idx_billing_orders_merchant
    ON billing_orders(merchant_id);

CREATE INDEX idx_billing_orders_paid_at
    ON billing_orders(paid_at);

-- =====================================================
-- FEE CONFIGS
-- =====================================================

CREATE TABLE fee_configs (
                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                             merchant_id UUID NOT NULL,

                             fee_type VARCHAR(50) NOT NULL,
                             fee_value NUMERIC(18,4) NOT NULL,

                             active BOOLEAN NOT NULL DEFAULT TRUE,

                             effective_from TIMESTAMP NOT NULL,
                             effective_to TIMESTAMP,

                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                             CONSTRAINT fk_fee_configs_merchant
                                 FOREIGN KEY (merchant_id)
                                 REFERENCES merchants(id)


);

CREATE INDEX idx_fee_configs_merchant
    ON fee_configs(merchant_id);

-- =====================================================
-- FEE TRANSACTIONS
-- =====================================================

CREATE TABLE fee_transactions (
                                  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                                  order_id UUID NOT NULL,

                                  fee_type VARCHAR(50) NOT NULL,

                                  amount NUMERIC(18,2) NOT NULL,

                                  currency VARCHAR(10) NOT NULL DEFAULT 'USD',

                                  description VARCHAR(255),

                                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                  CONSTRAINT fk_fee_transactions_order
                                      FOREIGN KEY (order_id)
                                      REFERENCES billing_orders(id)


);

CREATE INDEX idx_fee_transactions_order
    ON fee_transactions(order_id);

CREATE INDEX idx_fee_transactions_type
    ON fee_transactions(fee_type);

-- =====================================================
-- PAYOUTS
-- =====================================================

CREATE TABLE payouts (
                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                         payout_no VARCHAR(100) NOT NULL UNIQUE,

                         merchant_id UUID NOT NULL,

                         period_start DATE NOT NULL,
                         period_end DATE NOT NULL,

                         total_amount NUMERIC(18,2) NOT NULL,

                         currency VARCHAR(10) NOT NULL DEFAULT 'USD',

                         stripe_transfer_id VARCHAR(255),
                         stripe_payout_id VARCHAR(255),

                         status VARCHAR(30) NOT NULL,

                         payout_date TIMESTAMP,

                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                         CONSTRAINT fk_payouts_merchant
                             FOREIGN KEY (merchant_id)
                             REFERENCES merchants(id),

                         CONSTRAINT uk_payout_period
                             UNIQUE (
                                 merchant_id,
                                 period_start,
                                 period_end
                             )


);

CREATE INDEX idx_payouts_merchant
    ON payouts(merchant_id);

CREATE INDEX idx_payouts_period
    ON payouts(period_start, period_end);

-- =====================================================
-- PAYOUT TRANSACTIONS
-- Snapshot value at payout time
-- =====================================================

CREATE TABLE payout_transactions (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                                     payout_id UUID NOT NULL,

                                     order_id UUID NOT NULL,

                                     gross_amount NUMERIC(18,2) NOT NULL,

                                     total_fee NUMERIC(18,2) NOT NULL,

                                     net_amount NUMERIC(18,2) NOT NULL,

                                     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                     CONSTRAINT fk_payout_transactions_payout
                                         FOREIGN KEY (payout_id)
                                         REFERENCES payouts(id),

                                     CONSTRAINT fk_payout_transactions_order
                                         FOREIGN KEY (order_id)
                                         REFERENCES billing_orders(id)


);

CREATE INDEX idx_payout_transactions_payout
    ON payout_transactions(payout_id);

CREATE INDEX idx_payout_transactions_order
    ON payout_transactions(order_id);