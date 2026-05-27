-- V6__add_version_to_items.sql
-- Add optimistic locking version field to items table
-- Prevents race conditions when multiple orders try to buy the same item simultaneously

ALTER TABLE items ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
