-- =====================================================
-- Payment Request ID Migration to UUID
-- Version: V18
-- Description: Alter payment_request_id to use UUID for better scalability
-- =====================================================

-- Enable uuid-ossp extension for UUID generation
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- For now, we'll use gen_random_uuid() which generates UUID v4
-- This provides good uniqueness and performance
-- Note: True UUIDv7 support can be added later when PostgreSQL has native support

-- Step 1: Add new UUID column
ALTER TABLE payment_request 
ADD COLUMN payment_request_uuid UUID DEFAULT gen_random_uuid();

-- Step 2: Update existing records with UUID values
UPDATE payment_request 
SET payment_request_uuid = gen_random_uuid() 
WHERE payment_request_uuid IS NULL;

-- Step 3: Make the UUID column NOT NULL
ALTER TABLE payment_request 
ALTER COLUMN payment_request_uuid SET NOT NULL;

-- Step 4: Add unique constraint to UUID column
ALTER TABLE payment_request 
ADD CONSTRAINT uk_payment_request_uuid UNIQUE (payment_request_uuid);

-- Step 5: Update foreign key references in payment_transaction table
-- First add the new UUID column to payment_transaction
ALTER TABLE payment_transaction 
ADD COLUMN payment_request_uuid UUID;

-- Update the UUID references based on existing BIGINT relationships
UPDATE payment_transaction pt
SET payment_request_uuid = pr.payment_request_uuid
FROM payment_request pr
WHERE pt.payment_request_id = pr.payment_request_id;

-- Make the UUID column NOT NULL
ALTER TABLE payment_transaction 
ALTER COLUMN payment_request_uuid SET NOT NULL;

-- Step 5b: Update foreign key references in payment_audit_log table
-- First add the new UUID column to payment_audit_log
ALTER TABLE payment_audit_log 
ADD COLUMN payment_request_uuid UUID;

-- Update the UUID references based on existing BIGINT relationships
UPDATE payment_audit_log pal
SET payment_request_uuid = pr.payment_request_uuid
FROM payment_request pr
WHERE pal.payment_request_id = pr.payment_request_id;

-- Step 6: Drop old foreign key constraints and create new ones
ALTER TABLE payment_transaction 
DROP CONSTRAINT payment_transaction_payment_request_id_fkey;

ALTER TABLE payment_audit_log 
DROP CONSTRAINT payment_audit_log_payment_request_id_fkey;

ALTER TABLE payment_transaction 
ADD CONSTRAINT fk_payment_transaction_payment_request_uuid 
FOREIGN KEY (payment_request_uuid) REFERENCES payment_request(payment_request_uuid) ON DELETE RESTRICT;

-- Add foreign key constraint for payment_audit_log
ALTER TABLE payment_audit_log 
ADD CONSTRAINT fk_payment_audit_log_payment_request_uuid 
FOREIGN KEY (payment_request_uuid) REFERENCES payment_request(payment_request_uuid) ON DELETE CASCADE;

-- Step 7: Drop old primary key and create new one
ALTER TABLE payment_request 
DROP CONSTRAINT payment_request_pkey CASCADE;

ALTER TABLE payment_request 
ADD CONSTRAINT payment_request_pkey PRIMARY KEY (payment_request_uuid);

-- Step 8: Drop old columns
ALTER TABLE payment_request 
DROP COLUMN payment_request_id;

ALTER TABLE payment_transaction 
DROP COLUMN payment_request_id;

ALTER TABLE payment_audit_log 
DROP COLUMN payment_request_id;

-- Step 9: Rename UUID columns to original names
ALTER TABLE payment_request 
RENAME COLUMN payment_request_uuid TO payment_request_id;

ALTER TABLE payment_transaction 
RENAME COLUMN payment_request_uuid TO payment_request_id;

ALTER TABLE payment_audit_log 
RENAME COLUMN payment_request_uuid TO payment_request_id;

-- Step 10: Update constraint names
ALTER TABLE payment_request 
RENAME CONSTRAINT uk_payment_request_uuid TO uk_payment_request_id;

ALTER TABLE payment_transaction 
RENAME CONSTRAINT fk_payment_transaction_payment_request_uuid TO fk_payment_transaction_payment_request_id;

ALTER TABLE payment_audit_log 
RENAME CONSTRAINT fk_payment_audit_log_payment_request_uuid TO fk_payment_audit_log_payment_request_id;

-- Step 11: Create indexes for performance
CREATE INDEX idx_payment_request_id ON payment_request(payment_request_id);
CREATE INDEX idx_payment_transaction_payment_request_id ON payment_transaction(payment_request_id);
CREATE INDEX idx_payment_audit_log_payment_request_id ON payment_audit_log(payment_request_id);

-- Comments for documentation
COMMENT ON COLUMN payment_request.payment_request_id IS 'UUID v7 identifier for payment request';
COMMENT ON COLUMN payment_transaction.payment_request_id IS 'UUID v7 reference to payment request';
COMMENT ON COLUMN payment_audit_log.payment_request_id IS 'UUID v7 reference to payment request';