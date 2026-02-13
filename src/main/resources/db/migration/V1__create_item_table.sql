-- V1__initial_schema.sql

-- Items table
CREATE TABLE IF NOT EXISTS items (
    uuid UUID NOT NULL,
    description VARCHAR(1000),
    image_urls JSONB NOT NULL,
    name VARCHAR(255) NOT NULL,
    price NUMERIC(10,2) NOT NULL,
    quantity INTEGER NOT NULL,
    status VARCHAR(30) NOT NULL,
    CONSTRAINT items_status_check CHECK (status IN ('ACTIVE', 'INACTIVE', 'OUT_OF_STOCK'))
    );

ALTER TABLE items DROP CONSTRAINT IF EXISTS items_pkey;
ALTER TABLE items ADD CONSTRAINT items_pkey PRIMARY KEY (uuid);

-- Collections table
CREATE TABLE IF NOT EXISTS collections (
    uuid UUID NOT NULL,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    image_urls JSONB NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL
    );

ALTER TABLE collections DROP CONSTRAINT IF EXISTS collections_pkey;
ALTER TABLE collections ADD CONSTRAINT collections_pkey PRIMARY KEY (uuid);

-- Add collection reference to items if not exists
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = 'items' AND column_name = 'color') THEN
ALTER TABLE items ADD COLUMN color VARCHAR(100);
END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = 'items' AND column_name = 'collection_uuid') THEN
ALTER TABLE items ADD COLUMN collection_uuid UUID;
END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = 'items' AND column_name = 'created_at') THEN
ALTER TABLE items ADD COLUMN created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP;
END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = 'items' AND column_name = 'updated_at') THEN
ALTER TABLE items ADD COLUMN updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP;
END IF;
END $$;

-- Add foreign key constraint
ALTER TABLE items DROP CONSTRAINT IF EXISTS fk_item_collection;
ALTER TABLE items ADD CONSTRAINT fk_item_collection
    FOREIGN KEY (collection_uuid) REFERENCES collections(uuid) ON DELETE SET NULL;

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_items_collection_uuid ON items(collection_uuid);
CREATE INDEX IF NOT EXISTS idx_items_color ON items(color);
CREATE INDEX IF NOT EXISTS idx_collections_name ON collections(name);

-- Orders table
CREATE TABLE IF NOT EXISTS orders (
    uuid UUID NOT NULL,
    address TEXT NOT NULL,
    city VARCHAR(50) NOT NULL,
    country VARCHAR(50) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    customer_notes TEXT,
    email VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    order_number VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    postal_code VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL,
    total_amount NUMERIC(10,2) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT orders_status_check CHECK (status IN ('PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED', 'REFUNDED'))
    );

ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_pkey;
ALTER TABLE orders ADD CONSTRAINT orders_pkey PRIMARY KEY (uuid);

ALTER TABLE orders DROP CONSTRAINT IF EXISTS uk_order_number;
ALTER TABLE orders ADD CONSTRAINT uk_order_number UNIQUE (order_number);

-- Order items table
CREATE TABLE IF NOT EXISTS order_items (
    uuid UUID NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    price NUMERIC(10,2) NOT NULL,
    quantity INTEGER NOT NULL,
    item_uuid UUID NOT NULL,
    order_uuid UUID NOT NULL
    );

ALTER TABLE order_items DROP CONSTRAINT IF EXISTS order_items_pkey;
ALTER TABLE order_items ADD CONSTRAINT order_items_pkey PRIMARY KEY (uuid);

ALTER TABLE order_items DROP CONSTRAINT IF EXISTS fk_order_item_item;
ALTER TABLE order_items ADD CONSTRAINT fk_order_item_item
    FOREIGN KEY (item_uuid) REFERENCES items(uuid);

ALTER TABLE order_items DROP CONSTRAINT IF EXISTS fk_order_item_order;
ALTER TABLE order_items ADD CONSTRAINT fk_order_item_order
    FOREIGN KEY (order_uuid) REFERENCES orders(uuid);

-- Order status history table
CREATE TABLE IF NOT EXISTS order_status_history (
    uuid UUID NOT NULL,
    changed_by VARCHAR(100),
    created_at TIMESTAMP(6) NOT NULL,
    new_status VARCHAR(30) NOT NULL,
    notes TEXT,
    old_status VARCHAR(30),
    order_uuid UUID NOT NULL,
    CONSTRAINT order_status_history_new_status_check CHECK (new_status IN ('PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED', 'REFUNDED')),
    CONSTRAINT order_status_history_old_status_check CHECK (old_status IS NULL OR old_status IN ('PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED', 'REFUNDED'))
    );

ALTER TABLE order_status_history DROP CONSTRAINT IF EXISTS order_status_history_pkey;
ALTER TABLE order_status_history ADD CONSTRAINT order_status_history_pkey PRIMARY KEY (uuid);

ALTER TABLE order_status_history DROP CONSTRAINT IF EXISTS fk_order_status_history_order;
ALTER TABLE order_status_history ADD CONSTRAINT fk_order_status_history_order
    FOREIGN KEY (order_uuid) REFERENCES orders(uuid);

-- Payments table
CREATE TABLE IF NOT EXISTS payments (
    uuid UUID NOT NULL,
    order_uuid UUID NOT NULL UNIQUE,
    amount NUMERIC(10, 2) NOT NULL,
    method VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    payment_reference VARCHAR(100) NOT NULL,
    gateway_response TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
    );

ALTER TABLE payments DROP CONSTRAINT IF EXISTS payments_pkey;
ALTER TABLE payments ADD CONSTRAINT payments_pkey PRIMARY KEY (uuid);

ALTER TABLE payments DROP CONSTRAINT IF EXISTS fk_payment_order;
ALTER TABLE payments ADD CONSTRAINT fk_payment_order
    FOREIGN KEY (order_uuid) REFERENCES orders(uuid);

CREATE INDEX IF NOT EXISTS idx_payments_order_uuid ON payments(order_uuid);
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(status);
CREATE INDEX IF NOT EXISTS idx_payments_created_at ON payments(created_at);

-- Shippings table
CREATE TABLE IF NOT EXISTS shippings (
    uuid UUID NOT NULL,
    order_uuid UUID NOT NULL UNIQUE,
    provider VARCHAR(30) NOT NULL,
    address TEXT NOT NULL,
    cost NUMERIC(10, 2) NOT NULL,
    tracking_number VARCHAR(100),
    status VARCHAR(30) NOT NULL,
    estimated_delivery TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
    );

ALTER TABLE shippings DROP CONSTRAINT IF EXISTS shippings_pkey;
ALTER TABLE shippings ADD CONSTRAINT shippings_pkey PRIMARY KEY (uuid);

ALTER TABLE shippings DROP CONSTRAINT IF EXISTS fk_shipping_order;
ALTER TABLE shippings ADD CONSTRAINT fk_shipping_order
    FOREIGN KEY (order_uuid) REFERENCES orders(uuid);

CREATE INDEX IF NOT EXISTS idx_shippings_order_uuid ON shippings(order_uuid);
CREATE INDEX IF NOT EXISTS idx_shippings_status ON shippings(status);
CREATE INDEX IF NOT EXISTS idx_shippings_tracking_number ON shippings(tracking_number);
CREATE INDEX IF NOT EXISTS idx_shippings_created_at ON shippings(created_at);

-- Comments
COMMENT ON TABLE collections IS 'Product collections (e.g., Alien, Tribal, Teeth)';
COMMENT ON COLUMN items.color IS 'Product color';
COMMENT ON COLUMN items.collection_uuid IS 'Reference to collection';