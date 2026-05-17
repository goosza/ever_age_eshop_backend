-- Add Zasilkovna pickup point fields to shippings table

-- Pickup point information
ALTER TABLE shippings ADD COLUMN pickup_point_id VARCHAR(50);
ALTER TABLE shippings ADD COLUMN pickup_point_name VARCHAR(255);
ALTER TABLE shippings ADD COLUMN pickup_point_address TEXT;

-- Zasilkovna specific fields
ALTER TABLE shippings ADD COLUMN shipment_id VARCHAR(100);
ALTER TABLE shippings ADD COLUMN label_url VARCHAR(500);

-- Indexes for efficient queries
CREATE INDEX idx_shippings_pickup_point ON shippings(pickup_point_id);
CREATE INDEX idx_shippings_shipment_id ON shippings(shipment_id);
CREATE INDEX idx_shippings_tracking_number ON shippings(tracking_number);
