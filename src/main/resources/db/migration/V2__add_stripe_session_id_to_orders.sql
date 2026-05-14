-- Add stripe_session_id column to orders table
ALTER TABLE orders ADD COLUMN stripe_session_id VARCHAR(255);

-- Add unique constraint
ALTER TABLE orders ADD CONSTRAINT uk_stripe_session_id UNIQUE (stripe_session_id);

-- Add index for faster lookups
CREATE INDEX idx_orders_stripe_session_id ON orders(stripe_session_id);
