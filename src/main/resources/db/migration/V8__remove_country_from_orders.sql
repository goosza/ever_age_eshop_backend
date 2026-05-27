-- V8__remove_country_from_orders.sql
-- Country belongs to Shipping (delivery address), not Order
-- Order only stores customer identity: name, email, phone

ALTER TABLE orders DROP COLUMN IF EXISTS country;
