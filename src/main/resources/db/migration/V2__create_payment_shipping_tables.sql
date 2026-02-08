-- Create payments table
CREATE TABLE payments (
    uuid UUID PRIMARY KEY,
    order_uuid UUID NOT NULL UNIQUE,
    amount DECIMAL(10, 2) NOT NULL,
    method VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    payment_reference VARCHAR(100) NOT NULL,
    gateway_response TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (order_uuid) REFERENCES orders(uuid)
);

CREATE INDEX idx_payments_order_uuid ON payments(order_uuid);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_created_at ON payments(created_at);

-- Create shippings table
CREATE TABLE shippings (
    uuid UUID PRIMARY KEY,
    order_uuid UUID NOT NULL UNIQUE,
    provider VARCHAR(30) NOT NULL,
    address TEXT NOT NULL,
    cost DECIMAL(10, 2) NOT NULL,
    tracking_number VARCHAR(100),
    status VARCHAR(30) NOT NULL,
    estimated_delivery TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (order_uuid) REFERENCES orders(uuid)
);

CREATE INDEX idx_shippings_order_uuid ON shippings(order_uuid);
CREATE INDEX idx_shippings_status ON shippings(status);
CREATE INDEX idx_shippings_tracking_number ON shippings(tracking_number);
CREATE INDEX idx_shippings_created_at ON shippings(created_at);

-- Add status column to orders if it doesn't exist
ALTER TABLE orders ADD COLUMN status VARCHAR(50) DEFAULT 'PENDING';
