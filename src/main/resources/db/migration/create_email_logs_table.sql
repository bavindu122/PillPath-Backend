-- ============================================
-- Email Logs Table Migration Script
-- Purpose: Track all email sends for auditing and preventing duplicates
-- ============================================

-- Create email_logs table
CREATE TABLE IF NOT EXISTS email_logs (
    id BIGSERIAL PRIMARY KEY,
    recipient_email VARCHAR(255) NOT NULL,
    recipient_name VARCHAR(255),
    subject VARCHAR(500) NOT NULL,
    email_type VARCHAR(50) NOT NULL,
    sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'SENT',
    
    -- Reference fields for traceability
    notification_id BIGINT,
    prescription_id BIGINT,
    order_id BIGINT,
    pharmacy_id BIGINT,
    customer_id BIGINT,
    pharmacist_id BIGINT,
    
    error_message TEXT,
    
    -- Indexes for performance
    CONSTRAINT email_logs_status_check CHECK (status IN ('SENT', 'FAILED', 'BOUNCED'))
);

-- Create indexes for common queries
CREATE INDEX IF NOT EXISTS idx_email_logs_recipient ON email_logs(recipient_email);
CREATE INDEX IF NOT EXISTS idx_email_logs_type ON email_logs(email_type);
CREATE INDEX IF NOT EXISTS idx_email_logs_status ON email_logs(status);
CREATE INDEX IF NOT EXISTS idx_email_logs_sent_at ON email_logs(sent_at DESC);
CREATE INDEX IF NOT EXISTS idx_email_logs_prescription ON email_logs(prescription_id) WHERE prescription_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_email_logs_order ON email_logs(order_id) WHERE order_id IS NOT NULL;

-- Composite index for duplicate prevention
CREATE INDEX IF NOT EXISTS idx_email_logs_duplicate_check 
    ON email_logs(recipient_email, email_type, prescription_id, order_id, status);

-- Comments for documentation
COMMENT ON TABLE email_logs IS 'Tracks all email notifications sent to users for auditing and duplicate prevention';
COMMENT ON COLUMN email_logs.email_type IS 'Type of email: PRESCRIPTION_SENT, ORDER_PREVIEW_READY, ORDER_CONFIRMED, ORDER_DECLINED, ORDER_READY';
COMMENT ON COLUMN email_logs.status IS 'Email delivery status: SENT (successfully sent), FAILED (sending failed), BOUNCED (email bounced)';
COMMENT ON COLUMN email_logs.notification_id IS 'Reference to the corresponding in-app notification';

-- ============================================
-- Sample Verification Queries
-- ============================================

-- Count emails by type
-- SELECT email_type, COUNT(*) as count, COUNT(CASE WHEN status = 'SENT' THEN 1 END) as sent
-- FROM email_logs 
-- GROUP BY email_type;

-- Check failed emails in last 24 hours
-- SELECT * FROM email_logs 
-- WHERE status = 'FAILED' 
-- AND sent_at > CURRENT_TIMESTAMP - INTERVAL '24 hours'
-- ORDER BY sent_at DESC;

-- Verify email sent for specific order
-- SELECT * FROM email_logs 
-- WHERE order_id = 1 
-- ORDER BY sent_at DESC;
