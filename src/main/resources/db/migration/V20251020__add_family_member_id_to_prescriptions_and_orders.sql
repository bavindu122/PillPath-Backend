-- Add family_member_id column to prescriptions table
ALTER TABLE prescriptions
ADD COLUMN family_member_id BIGINT NULL;

-- Add comment for clarity
COMMENT ON COLUMN prescriptions.family_member_id IS 'References the family member this prescription is for (from family_member table)';

-- Add family_member_id column to customer_orders table
ALTER TABLE customer_orders
ADD COLUMN family_member_id BIGINT NULL;

-- Add comment for clarity
COMMENT ON COLUMN customer_orders.family_member_id IS 'References the family member this order is for (from family_member table)';

-- Optional: Add foreign key constraint if you want referential integrity
-- Note: This is optional because the family_member might be deleted while keeping the prescription history
-- Uncomment the lines below if you want to enforce referential integrity:

-- ALTER TABLE prescriptions
-- ADD CONSTRAINT fk_prescriptions_family_member
-- FOREIGN KEY (family_member_id) REFERENCES family_member(id) ON DELETE SET NULL;

-- ALTER TABLE customer_orders
-- ADD CONSTRAINT fk_customer_orders_family_member
-- FOREIGN KEY (family_member_id) REFERENCES family_member(id) ON DELETE SET NULL;
gf