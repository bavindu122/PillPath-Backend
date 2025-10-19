-- =====================================================
-- NOTIFICATION SYSTEM - USEFUL QUERIES
-- Quick reference for testing, debugging, and monitoring
-- =====================================================

-- =====================================================
-- 1. VERIFICATION QUERIES
-- =====================================================

-- Check if notifications table exists
SELECT EXISTS (
    SELECT FROM information_schema.tables 
    WHERE table_schema = 'public' 
    AND table_name = 'notifications'
);

-- View table structure
SELECT 
    column_name, 
    data_type, 
    character_maximum_length,
    is_nullable, 
    column_default
FROM information_schema.columns 
WHERE table_name = 'notifications' 
ORDER BY ordinal_position;

-- List all indexes on notifications table
SELECT 
    indexname, 
    indexdef 
FROM pg_indexes 
WHERE tablename = 'notifications'
ORDER BY indexname;

-- =====================================================
-- 2. DATA INSPECTION QUERIES
-- =====================================================

-- View recent notifications (last 10)
SELECT 
    id,
    title,
    message,
    type,
    recipient_type,
    recipient_id,
    read,
    created_at,
    link_url
FROM notifications 
ORDER BY created_at DESC 
LIMIT 10;

-- View all unread notifications
SELECT 
    id,
    title,
    message,
    type,
    recipient_type,
    recipient_id,
    created_at
FROM notifications 
WHERE read = false 
ORDER BY created_at DESC;

-- View notifications for specific customer
SELECT 
    id,
    title,
    message,
    type,
    read,
    created_at,
    link_url
FROM notifications 
WHERE recipient_id = 1 -- Replace with actual customer ID
  AND recipient_type = 'CUSTOMER'
ORDER BY created_at DESC;

-- View notifications for specific pharmacist
SELECT 
    id,
    title,
    message,
    type,
    read,
    created_at,
    link_url
FROM notifications 
WHERE recipient_id = 1 -- Replace with actual pharmacist ID
  AND recipient_type = 'PHARMACIST'
ORDER BY created_at DESC;

-- =====================================================
-- 3. STATISTICS QUERIES
-- =====================================================

-- Count total notifications
SELECT COUNT(*) as total_notifications 
FROM notifications;

-- Count notifications by type
SELECT 
    type,
    COUNT(*) as count
FROM notifications 
GROUP BY type 
ORDER BY count DESC;

-- Count notifications by recipient type
SELECT 
    recipient_type,
    COUNT(*) as count
FROM notifications 
GROUP BY recipient_type 
ORDER BY count DESC;

-- Count read vs unread notifications
SELECT 
    read,
    COUNT(*) as count
FROM notifications 
GROUP BY read;

-- Count notifications by type and recipient type
SELECT 
    type,
    recipient_type,
    COUNT(*) as count
FROM notifications 
GROUP BY type, recipient_type 
ORDER BY type, recipient_type;

-- Daily notification creation stats (last 7 days)
SELECT 
    DATE(created_at) as date,
    COUNT(*) as total_notifications,
    SUM(CASE WHEN read = true THEN 1 ELSE 0 END) as read_count,
    SUM(CASE WHEN read = false THEN 1 ELSE 0 END) as unread_count
FROM notifications
WHERE created_at >= NOW() - INTERVAL '7 days'
GROUP BY DATE(created_at)
ORDER BY date DESC;

-- Notifications per prescription (shows which prescriptions generated most notifications)
SELECT 
    prescription_id,
    COUNT(*) as notification_count
FROM notifications 
WHERE prescription_id IS NOT NULL
GROUP BY prescription_id 
ORDER BY notification_count DESC 
LIMIT 20;

-- Unread notifications per user
SELECT 
    recipient_type,
    recipient_id,
    COUNT(*) as unread_count
FROM notifications
WHERE read = false
GROUP BY recipient_type, recipient_id
ORDER BY unread_count DESC
LIMIT 20;

-- =====================================================
-- 4. TESTING QUERIES
-- =====================================================

-- Insert test notification for customer
INSERT INTO notifications (
    title, 
    message, 
    type, 
    recipient_id, 
    recipient_type, 
    link_url, 
    created_at
) VALUES (
    'Test Notification',
    'This is a test notification for customer.',
    'INFO',
    1, -- Replace with actual customer ID
    'CUSTOMER',
    '/customer/test',
    NOW()
);

-- Insert test notification for pharmacist
INSERT INTO notifications (
    title, 
    message, 
    type, 
    recipient_id, 
    recipient_type, 
    prescription_id,
    pharmacy_id,
    link_url, 
    created_at
) VALUES (
    'Test Prescription Alert',
    'New prescription from John Doe is awaiting review.',
    'INFO',
    1, -- Replace with actual pharmacist ID
    'PHARMACIST',
    1, -- Replace with actual prescription ID
    1, -- Replace with actual pharmacy ID
    '/pharmacist/prescriptions/1',
    NOW()
);

-- Mark notification as read (for testing)
UPDATE notifications 
SET read = true 
WHERE id = 1; -- Replace with actual notification ID

-- Mark all notifications as read for a user (for testing)
UPDATE notifications 
SET read = true 
WHERE recipient_id = 1 
  AND recipient_type = 'CUSTOMER';

-- Delete test notification
DELETE FROM notifications 
WHERE id = 1; -- Replace with actual notification ID

-- =====================================================
-- 5. DEBUGGING QUERIES
-- =====================================================

-- Find duplicate notifications (same prescription + recipient)
SELECT 
    prescription_id,
    recipient_id,
    recipient_type,
    COUNT(*) as duplicate_count
FROM notifications 
WHERE prescription_id IS NOT NULL
GROUP BY prescription_id, recipient_id, recipient_type
HAVING COUNT(*) > 1
ORDER BY duplicate_count DESC;

-- Find notifications with missing links
SELECT 
    id,
    title,
    type,
    recipient_type,
    created_at
FROM notifications 
WHERE link_url IS NULL 
ORDER BY created_at DESC;

-- Find notifications older than 30 days
SELECT 
    id,
    title,
    type,
    recipient_type,
    created_at,
    DATE_PART('day', NOW() - created_at) as days_old
FROM notifications 
WHERE created_at < NOW() - INTERVAL '30 days'
ORDER BY created_at ASC;

-- Check for orphaned notifications (recipient doesn't exist)
-- (Adjust table names based on your user table structure)
SELECT n.id, n.recipient_id, n.recipient_type
FROM notifications n
LEFT JOIN users u ON n.recipient_id = u.id
WHERE u.id IS NULL;

-- Performance check - show slow queries
EXPLAIN ANALYZE
SELECT * 
FROM notifications 
WHERE recipient_id = 1 
  AND recipient_type = 'CUSTOMER' 
  AND read = false
ORDER BY created_at DESC;

-- =====================================================
-- 6. MAINTENANCE QUERIES
-- =====================================================

-- Archive old read notifications (older than 90 days) - BE CAREFUL!
-- CREATE TABLE notifications_archive AS 
-- SELECT * FROM notifications 
-- WHERE read = true 
--   AND created_at < NOW() - INTERVAL '90 days';

-- Delete archived notifications - BE VERY CAREFUL!
-- DELETE FROM notifications 
-- WHERE read = true 
--   AND created_at < NOW() - INTERVAL '90 days';

-- Vacuum table to reclaim space (after large deletes)
-- VACUUM ANALYZE notifications;

-- Update statistics for query optimizer
ANALYZE notifications;

-- Rebuild indexes (if performance degrades)
-- REINDEX TABLE notifications;

-- =====================================================
-- 7. MONITORING QUERIES (for dashboards)
-- =====================================================

-- Notification metrics for today
SELECT 
    COUNT(*) as total_today,
    SUM(CASE WHEN type = 'SUCCESS' THEN 1 ELSE 0 END) as success_count,
    SUM(CASE WHEN type = 'INFO' THEN 1 ELSE 0 END) as info_count,
    SUM(CASE WHEN type = 'WARNING' THEN 1 ELSE 0 END) as warning_count,
    SUM(CASE WHEN type = 'ERROR' THEN 1 ELSE 0 END) as error_count
FROM notifications
WHERE created_at >= CURRENT_DATE;

-- Average time to read notification
SELECT 
    AVG(EXTRACT(EPOCH FROM (updated_at - created_at))/3600) as avg_hours_to_read
FROM notifications
WHERE read = true
  AND updated_at IS NOT NULL;

-- Most active users (receiving most notifications)
SELECT 
    recipient_type,
    recipient_id,
    COUNT(*) as total_notifications,
    SUM(CASE WHEN read = false THEN 1 ELSE 0 END) as unread_count
FROM notifications
GROUP BY recipient_type, recipient_id
ORDER BY total_notifications DESC
LIMIT 10;

-- Notification types breakdown by hour (today)
SELECT 
    EXTRACT(HOUR FROM created_at) as hour,
    type,
    COUNT(*) as count
FROM notifications
WHERE created_at >= CURRENT_DATE
GROUP BY EXTRACT(HOUR FROM created_at), type
ORDER BY hour, type;

-- =====================================================
-- 8. EXPORT QUERIES (for reporting)
-- =====================================================

-- Export all notifications as CSV (use with psql \copy)
-- \copy (SELECT * FROM notifications ORDER BY created_at DESC) TO '/tmp/notifications_export.csv' WITH CSV HEADER;

-- Export unread notifications summary
-- \copy (SELECT recipient_type, recipient_id, COUNT(*) FROM notifications WHERE read = false GROUP BY recipient_type, recipient_id) TO '/tmp/unread_summary.csv' WITH CSV HEADER;

-- =====================================================
-- 9. CLEANUP QUERIES (use with caution!)
-- =====================================================

-- Delete all read notifications older than 180 days
-- WARNING: This is destructive! Backup first!
-- DELETE FROM notifications 
-- WHERE read = true 
--   AND created_at < NOW() - INTERVAL '180 days';

-- Delete all test notifications (if you added test data)
-- DELETE FROM notifications 
-- WHERE title LIKE 'Test%' 
--    OR message LIKE 'This is a test%';

-- Reset all notifications to unread (for testing)
-- UPDATE notifications SET read = false;

-- Truncate entire table (for fresh start) - VERY DANGEROUS!
-- TRUNCATE TABLE notifications RESTART IDENTITY CASCADE;

-- =====================================================
-- END OF QUERY REFERENCE
-- =====================================================
