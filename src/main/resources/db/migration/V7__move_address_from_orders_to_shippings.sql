-- V7__move_address_from_orders_to_shippings.sql
-- Move delivery address fields from orders to shippings table
-- Orders only keep country (needed for shipping options selection)
-- Shippings now have full delivery address (address/city/postalCode for HOME, pickupPointAddress for PICKUP)

-- Add address fields to shippings
ALTER TABLE shippings ADD COLUMN IF NOT EXISTS city VARCHAR(50);
ALTER TABLE shippings ADD COLUMN IF NOT EXISTS postal_code VARCHAR(20);
ALTER TABLE shippings ADD COLUMN IF NOT EXISTS country VARCHAR(50);

-- Remove address fields from orders (keep country)
ALTER TABLE orders DROP COLUMN IF EXISTS address;
ALTER TABLE orders DROP COLUMN IF EXISTS city;
ALTER TABLE orders DROP COLUMN IF EXISTS postal_code;
