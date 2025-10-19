-- Drop legacy order_items table if exists
-- Flyway migration: V20251016__drop_order_items.sql

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'order_items') THEN
        EXECUTE 'DROP TABLE order_items';
    END IF;
END $$;
