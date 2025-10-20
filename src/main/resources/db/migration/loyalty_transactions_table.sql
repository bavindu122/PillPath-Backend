-- Create loyalty_transactions table to track individual point earnings
-- This preserves the rate at which points were earned, preventing retroactive changes

CREATE TABLE IF NOT EXISTS loyalty_transactions (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    order_total NUMERIC(10, 2) NOT NULL,
    loyalty_rate NUMERIC(10, 2) NOT NULL,
    points_earned INTEGER NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_loyalty_customer FOREIGN KEY (customer_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_loyalty_order FOREIGN KEY (order_id) 
        REFERENCES customer_orders(id) ON DELETE CASCADE,
    CONSTRAINT uk_loyalty_order UNIQUE (order_id)
);

-- Create indexes for faster queries
CREATE INDEX idx_loyalty_customer ON loyalty_transactions(customer_id);
CREATE INDEX idx_loyalty_created_at ON loyalty_transactions(created_at DESC);

-- Add comment to table
COMMENT ON TABLE loyalty_transactions IS 'Tracks loyalty points earned per transaction with the rate at the time of earning';
COMMENT ON COLUMN loyalty_transactions.loyalty_rate IS 'Points rate at the time of transaction (prevents retroactive changes)';
COMMENT ON COLUMN loyalty_transactions.points_earned IS 'Actual points awarded (calculated as orderTotal * loyaltyRate, rounded down)';
