CREATE TABLE transactions (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id   UUID NOT NULL REFERENCES clients (id),
    amount      NUMERIC(19, 2) NOT NULL CHECK (amount > 0),
    description VARCHAR(255),
    status      VARCHAR(50) NOT NULL DEFAULT 'PENDING'
                CHECK (status IN ('PENDING','PROCESSING','COMPLETED','FAILED','CANCELLED')),
    created_at  TIMESTAMP NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_transactions_client_id ON transactions (client_id);
CREATE INDEX idx_transactions_status    ON transactions (status);
