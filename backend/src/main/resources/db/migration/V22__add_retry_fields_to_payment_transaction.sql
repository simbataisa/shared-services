-- Migration V22: Add retry fields to payment_transaction table
-- This migration adds retry_count and max_retries columns to support payment retry functionality

-- Add retry_count column (tracks current number of retry attempts)
ALTER TABLE payment_transaction 
ADD COLUMN retry_count INTEGER NOT NULL DEFAULT 0 
CHECK (retry_count >= 0);

-- Add max_retries column (defines maximum allowed retry attempts)
ALTER TABLE payment_transaction 
ADD COLUMN max_retries INTEGER NOT NULL DEFAULT 3 
CHECK (max_retries >= 0);

-- Add constraint to ensure retry_count doesn't exceed max_retries
ALTER TABLE payment_transaction 
ADD CONSTRAINT chk_retry_count_within_max 
CHECK (retry_count <= max_retries);

-- Create index for retry-related queries
CREATE INDEX idx_payment_transaction_retry_count ON payment_transaction(retry_count);
CREATE INDEX idx_payment_transaction_max_retries ON payment_transaction(max_retries);

-- Add comment for documentation
COMMENT ON COLUMN payment_transaction.retry_count IS 'Current number of retry attempts for this transaction';
COMMENT ON COLUMN payment_transaction.max_retries IS 'Maximum allowed retry attempts for this transaction';