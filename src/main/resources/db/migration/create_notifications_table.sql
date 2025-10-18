-- =====================================================
-- Notifications Table Migration Script
-- Purpose: Store all notification events for the system
-- =====================================================

-- Drop table if exists (use cautiously in production)
-- DROP TABLE IF EXISTS notifications CASCADE;

-- Create notifications table
CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    
    -- Notification content
    title VARCHAR(255) NOT NULL,
    message VARCHAR(500) NOT NULL,
    type VARCHAR(20) NOT NULL, -- SUCCESS, INFO, WARNING, ERROR
    
    -- Status tracking
    read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Navigation
    link_url VARCHAR(500),
    
    -- Recipient information (who receives this notification)
    recipient_id BIGINT NOT NULL,
    recipient_type VARCHAR(20) NOT NULL, -- CUSTOMER, PHARMACIST, PHARMACY
    
    -- Reference information (what this notification is about)
    prescription_id BIGINT,
    order_id BIGINT,
    pharmacy_id BIGINT,
    customer_id BIGINT,
    
    -- Constraints
    CONSTRAINT chk_notification_type CHECK (type IN ('SUCCESS', 'INFO', 'WARNING', 'ERROR')),
    CONSTRAINT chk_recipient_type CHECK (recipient_type IN ('CUSTOMER', 'PHARMACIST', 'PHARMACY'))
);

-- =====================================================
-- Indexes for Performance Optimization
-- =====================================================

-- Index for fetching user's notifications (most common query)
CREATE INDEX IF NOT EXISTS idx_notifications_recipient 
ON notifications(recipient_id, recipient_type, created_at DESC);

-- Index for counting unread notifications
CREATE INDEX IF NOT EXISTS idx_notifications_unread 
ON notifications(recipient_id, recipient_type, read) 
WHERE read = FALSE;

-- Index for finding notifications by prescription
CREATE INDEX IF NOT EXISTS idx_notifications_prescription 
ON notifications(prescription_id) 
WHERE prescription_id IS NOT NULL;

-- Index for finding notifications by order
CREATE INDEX IF NOT EXISTS idx_notifications_order 
ON notifications(order_id) 
WHERE order_id IS NOT NULL;

-- Index for finding notifications by pharmacy (for pharmacy-wide queries)
CREATE INDEX IF NOT EXISTS idx_notifications_pharmacy 
ON notifications(pharmacy_id) 
WHERE pharmacy_id IS NOT NULL;

-- Composite index for duplicate prevention (prescription notifications)
CREATE INDEX IF NOT EXISTS idx_notifications_prescription_recipient 
ON notifications(prescription_id, recipient_id, recipient_type) 
WHERE prescription_id IS NOT NULL;

-- Composite index for duplicate prevention (order notifications)
CREATE INDEX IF NOT EXISTS idx_notifications_order_recipient 
ON notifications(order_id, recipient_id, recipient_type) 
WHERE order_id IS NOT NULL;

-- =====================================================
-- Comments for Documentation
-- =====================================================

COMMENT ON TABLE notifications IS 'Stores all notification events for customers, pharmacists, and pharmacies';
COMMENT ON COLUMN notifications.id IS 'Primary key, auto-incrementing';
COMMENT ON COLUMN notifications.title IS 'Short notification title (e.g., "New Prescription", "Order Ready")';
COMMENT ON COLUMN notifications.message IS 'Detailed notification message with context';
COMMENT ON COLUMN notifications.type IS 'Notification severity: SUCCESS (green), INFO (blue), WARNING (yellow), ERROR (red)';
COMMENT ON COLUMN notifications.read IS 'Whether the notification has been read by the recipient';
COMMENT ON COLUMN notifications.created_at IS 'Timestamp when notification was created';
COMMENT ON COLUMN notifications.link_url IS 'Deep link to relevant page (e.g., /customer/orders/123)';
COMMENT ON COLUMN notifications.recipient_id IS 'ID of the user/entity receiving the notification';
COMMENT ON COLUMN notifications.recipient_type IS 'Type of recipient: CUSTOMER, PHARMACIST, or PHARMACY';
COMMENT ON COLUMN notifications.prescription_id IS 'Reference to prescription if applicable';
COMMENT ON COLUMN notifications.order_id IS 'Reference to order if applicable';
COMMENT ON COLUMN notifications.pharmacy_id IS 'Reference to pharmacy if applicable';
COMMENT ON COLUMN notifications.customer_id IS 'Reference to customer if applicable';

-- =====================================================
-- Sample Data for Testing (Optional - Remove in Production)
-- =====================================================

-- Uncomment below to insert sample notifications for testing
/*
INSERT INTO notifications (title, message, type, recipient_id, recipient_type, prescription_id, pharmacy_id, link_url, created_at)
VALUES 
    ('New Prescription', 'New prescription from John Doe is awaiting review.', 'INFO', 1, 'PHARMACIST', 1, 1, '/pharmacist/prescriptions/1', NOW() - INTERVAL '30 minutes'),
    ('Order Preview Ready', 'Order preview from Main St Pharmacy is ready to review.', 'INFO', 2, 'CUSTOMER', 1, 1, '/customer/orders/1/preview', NOW() - INTERVAL '2 hours'),
    ('Order Confirmed', 'Customer John Doe confirmed the order. Proceed to preparation.', 'SUCCESS', 1, 'PHARMACIST', NULL, 1, '/pharmacist/orders/1', NOW() - INTERVAL '5 hours'),
    ('Order Ready', 'Your order from Main St Pharmacy is ready for pickup.', 'SUCCESS', 2, 'CUSTOMER', NULL, 1, '/customer/orders/1', NOW() - INTERVAL '1 day'),
    ('Order Declined', 'Customer John Doe declined the order preview. Reason: Price too high', 'WARNING', 3, 'PHARMACIST', NULL, 2, '/pharmacist/orders/2', NOW() - INTERVAL '2 days');
*/

-- =====================================================
-- Verification Queries
-- =====================================================

-- Check table structure
-- SELECT column_name, data_type, is_nullable, column_default 
-- FROM information_schema.columns 
-- WHERE table_name = 'notifications' 
-- ORDER BY ordinal_position;

-- Check indexes
-- SELECT indexname, indexdef 
-- FROM pg_indexes 
-- WHERE tablename = 'notifications';

-- Count notifications by type
-- SELECT type, COUNT(*) 
-- FROM notifications 
-- GROUP BY type;

-- Count notifications by recipient type
-- SELECT recipient_type, COUNT(*) 
-- FROM notifications 
-- GROUP BY recipient_type;

-- Check unread notifications
-- SELECT recipient_type, COUNT(*) 
-- FROM notifications 
-- WHERE read = FALSE 
-- GROUP BY recipient_type;

-- =====================================================
-- End of Migration Script
-- =====================================================
