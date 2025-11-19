-- V1__initial_schema.sql
CREATE TABLE IF NOT EXISTS item (
    uuid UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    image_urls JSONB,
    price DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    quantity INTEGER NOT NULL
);

CREATE INDEX idx_item_uuid ON item(uuid);
CREATE INDEX idx_item_name ON item(name);