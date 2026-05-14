CREATE TABLE orders (
                        id UUID PRIMARY KEY,
                        customer_id UUID NOT NULL,
                        amount NUMERIC(19,2) NOT NULL,
                        status VARCHAR(50) NOT NULL,
                        created_at TIMESTAMP NOT NULL
);