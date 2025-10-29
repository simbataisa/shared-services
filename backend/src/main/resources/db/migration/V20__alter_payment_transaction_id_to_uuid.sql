-- =====================================================
-- Payment Transaction ID Migration to UUID
-- Version: V20
-- Description: Alter payment_transaction_id to use UUID for better scalability and consistency
-- =====================================================

-- Enable uuid-ossp extension for UUID generation (if not already enabled)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Step 1: Drop foreign key constraints that reference payment_transaction_id
ALTER TABLE payment_refund 
DROP CONSTRAINT IF EXISTS payment_refund_payment_transaction_id_fkey;

ALTER TABLE payment_audit_log 
DROP CONSTRAINT IF EXISTS payment_audit_log_payment_transaction_id_fkey;

-- Step 2: Drop primary key constraint on payment_transaction
ALTER TABLE payment_transaction 
DROP CONSTRAINT IF EXISTS payment_transaction_pkey;

-- Step 3: Drop indexes that reference the old BIGINT ID
DROP INDEX IF EXISTS idx_payment_transaction_payment_request_id;
DROP INDEX IF EXISTS idx_payment_refund_transaction_id;
DROP INDEX IF EXISTS idx_payment_audit_log_transaction_id;

-- Step 4: Create a temporary mapping table to preserve relationships
CREATE TEMPORARY TABLE payment_transaction_mapping AS
SELECT 
    payment_transaction_id as old_id,
    gen_random_uuid() as new_uuid
FROM payment_transaction;

-- Step 5: Add temporary UUID columns to all tables
ALTER TABLE payment_transaction ADD COLUMN temp_uuid UUID;
ALTER TABLE payment_refund ADD COLUMN temp_payment_transaction_uuid UUID;
ALTER TABLE payment_audit_log ADD COLUMN temp_payment_transaction_uuid UUID;

-- Step 6: Update payment_transaction with new UUIDs
UPDATE payment_transaction pt
SET temp_uuid = ptm.new_uuid
FROM payment_transaction_mapping ptm
WHERE pt.payment_transaction_id = ptm.old_id;

-- Step 7: Update payment_refund with corresponding UUIDs
UPDATE payment_refund pr
SET temp_payment_transaction_uuid = ptm.new_uuid
FROM payment_transaction_mapping ptm
WHERE pr.payment_transaction_id = ptm.old_id;

-- Step 8: Update payment_audit_log with corresponding UUIDs (where applicable)
UPDATE payment_audit_log pal
SET temp_payment_transaction_uuid = ptm.new_uuid
FROM payment_transaction_mapping ptm
WHERE pal.payment_transaction_id = ptm.old_id;

-- Step 9: Drop old BIGINT columns
ALTER TABLE payment_transaction DROP COLUMN payment_transaction_id;
ALTER TABLE payment_refund DROP COLUMN payment_transaction_id;
ALTER TABLE payment_audit_log DROP COLUMN payment_transaction_id;

-- Step 10: Rename temporary UUID columns to final names
ALTER TABLE payment_transaction RENAME COLUMN temp_uuid TO payment_transaction_id;
ALTER TABLE payment_refund RENAME COLUMN temp_payment_transaction_uuid TO payment_transaction_id;
ALTER TABLE payment_audit_log RENAME COLUMN temp_payment_transaction_uuid TO payment_transaction_id;

-- Step 11: Set NOT NULL constraints
ALTER TABLE payment_transaction ALTER COLUMN payment_transaction_id SET NOT NULL;
ALTER TABLE payment_refund ALTER COLUMN payment_transaction_id SET NOT NULL;
-- Note: payment_transaction_id in audit_log can be NULL (not all audit entries are transaction-related)

-- Step 12: Add primary key constraint back to payment_transaction
ALTER TABLE payment_transaction 
ADD CONSTRAINT payment_transaction_pkey PRIMARY KEY (payment_transaction_id);

-- Step 13: Add foreign key constraints using UUID
ALTER TABLE payment_refund 
ADD CONSTRAINT fk_payment_refund_transaction_id 
FOREIGN KEY (payment_transaction_id) REFERENCES payment_transaction(payment_transaction_id) ON DELETE RESTRICT;

ALTER TABLE payment_audit_log 
ADD CONSTRAINT fk_payment_audit_log_transaction_id 
FOREIGN KEY (payment_transaction_id) REFERENCES payment_transaction(payment_transaction_id) ON DELETE CASCADE;

-- Step 14: Create new indexes for better performance
CREATE INDEX idx_payment_transaction_payment_request_id ON payment_transaction(payment_request_id);
CREATE INDEX idx_payment_refund_transaction_id ON payment_refund(payment_transaction_id);
CREATE INDEX idx_payment_audit_log_transaction_id ON payment_audit_log(payment_transaction_id);

-- Step 15: Add comments for documentation
COMMENT ON COLUMN payment_transaction.payment_transaction_id IS 'UUID primary key for payment transaction';
COMMENT ON COLUMN payment_refund.payment_transaction_id IS 'UUID foreign key reference to payment_transaction.payment_transaction_id';
COMMENT ON COLUMN payment_audit_log.payment_transaction_id IS 'UUID foreign key reference to payment_transaction.payment_transaction_id';

-- Clean up temporary table
DROP TABLE payment_transaction_mapping;

-- =====================================================
-- VERIFICATION QUERIES (for manual testing)
-- =====================================================

-- Verify the new structure
-- SELECT column_name, data_type, is_nullable 
-- FROM information_schema.columns 
-- WHERE table_name = 'payment_transaction' 
-- ORDER BY ordinal_position;

-- Verify foreign key relationships
-- SELECT tc.constraint_name, tc.table_name, kcu.column_name, 
--        ccu.table_name AS foreign_table_name, ccu.column_name AS foreign_column_name
-- FROM information_schema.table_constraints AS tc 
-- JOIN information_schema.key_column_usage AS kcu ON tc.constraint_name = kcu.constraint_name
-- JOIN information_schema.constraint_column_usage AS ccu ON ccu.constraint_name = tc.constraint_name
-- WHERE tc.constraint_type = 'FOREIGN KEY' 
-- AND (tc.table_name = 'payment_refund' OR tc.table_name = 'payment_audit_log')
-- AND kcu.column_name LIKE '%transaction%';

-- Verify data integrity
-- SELECT COUNT(*) FROM payment_transaction;
-- SELECT COUNT(*) FROM payment_refund;
-- SELECT COUNT(*) FROM payment_audit_log;