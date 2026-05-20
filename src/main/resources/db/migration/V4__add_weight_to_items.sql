-- Add weight field to items table

-- Add weight column (in kilograms, with 2 decimal places)
ALTER TABLE items ADD COLUMN weight DECIMAL(6,3) DEFAULT 0.500;

-- Add comment
COMMENT ON COLUMN items.weight IS 'Item weight in kilograms (kg)';

-- Update existing items to have default weight of 0.5 kg
UPDATE items SET weight = 0.500 WHERE weight IS NULL;

-- Make weight NOT NULL after setting defaults
ALTER TABLE items ALTER COLUMN weight SET NOT NULL;
