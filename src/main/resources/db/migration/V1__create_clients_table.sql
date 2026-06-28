CREATE TABLE clients (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(255) NOT NULL,
    document_number VARCHAR(50)  NOT NULL UNIQUE,
    email           VARCHAR(255) NOT NULL,
    legacy_id       VARCHAR(100),
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_clients_document_number ON clients (document_number);
CREATE INDEX idx_clients_legacy_id ON clients (legacy_id);
