-- V2__add_collections_and_update_items.sql

-- Create collections table
CREATE TABLE collections (
    uuid UUID NOT NULL,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    image_url VARCHAR(500),
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
);

ALTER TABLE ONLY collection ADD CONSTRAINT collections_pkey PRIMARY KEY (uuid);

-- Add new columns to items table
ALTER TABLE items
    ADD COLUMN color VARCHAR(100),
ADD COLUMN collection_uuid UUID,
ADD COLUMN created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Add foreign key constraint
ALTER TABLE items
    ADD CONSTRAINT fk_item_collection
        FOREIGN KEY (collection_uuid)
            REFERENCES collections(uuid)
            ON DELETE SET NULL;

-- Create indexes
CREATE INDEX idx_items_collection_uuid ON items(collection_uuid);
CREATE INDEX idx_items_color ON items(color);
CREATE INDEX idx_collections_name ON collections(name);

-- Comments
COMMENT ON TABLE collections IS 'Product collections (e.g., Alien, Tribal, Teeth)';
COMMENT ON COLUMN items.color IS 'Product color';
COMMENT ON COLUMN items.collection_uuid IS 'Reference to collection';