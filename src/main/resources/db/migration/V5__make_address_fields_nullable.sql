-- V5__make_address_fields_nullable.sql
-- Address fields are optional for pickup/ZBOX delivery methods
-- Only required for HOME delivery

ALTER TABLE orders ALTER COLUMN address DROP NOT NULL;
ALTER TABLE orders ALTER COLUMN city DROP NOT NULL;
ALTER TABLE orders ALTER COLUMN postal_code DROP NOT NULL;

-- Also make shipping address nullable (pickup points have their own address stored separately)
ALTER TABLE shippings ALTER COLUMN address DROP NOT NULL;
