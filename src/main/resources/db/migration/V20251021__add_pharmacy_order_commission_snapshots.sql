-- Add immutable finance snapshot columns to pharmacy_orders
ALTER TABLE pharmacy_orders
    ADD COLUMN IF NOT EXISTS commission_percent_snapshot NUMERIC(8,2),
    ADD COLUMN IF NOT EXISTS commission_amount_snapshot NUMERIC(12,2),
    ADD COLUMN IF NOT EXISTS convenience_fee_snapshot NUMERIC(12,2),
    ADD COLUMN IF NOT EXISTS net_after_commission_snapshot NUMERIC(12,2);

-- No backfill here; snapshots will be set when orders are finalized going forward.
-- Optionally, you can backfill existing HANDED_OVER orders in a separate migration/job.

