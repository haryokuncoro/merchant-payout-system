CREATE TABLE orders (
                        id UUID PRIMARY KEY,
                        customer_id UUID NOT NULL,
                        merchant_id UUID NOT NULL,
                        type VARCHAR(50) NOT NULL,
                        amount NUMERIC(19,2) NOT NULL,
                        status VARCHAR(50) NOT NULL,
                        created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_orders_customer_id
    ON orders(customer_id);

CREATE INDEX idx_orders_merchant_id
    ON orders(merchant_id);

CREATE INDEX idx_orders_status
    ON orders(status);

CREATE INDEX idx_orders_created_at
    ON orders(created_at);