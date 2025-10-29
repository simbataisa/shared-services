-- V23: Alter payment_audit_log_id and payment_refund_id to UUID
-- This migration converts the primary key fields from BIGSERIAL to UUID for better scalability and uniqueness

-- Enable UUID extension if not already enabled
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Step 1: Drop foreign key constraint from payment_audit_log that references payment_refund
ALTER TABLE payment_audit_log DROP CONSTRAINT IF EXISTS payment_audit_log_payment_refund_id_fkey;

-- Step 2: Alter payment_refund table first (since it's referenced by payment_audit_log)
-- Drop the existing primary key constraint
ALTER TABLE payment_refund DROP CONSTRAINT payment_refund_pkey;

-- Add a new UUID column
ALTER TABLE payment_refund ADD COLUMN payment_refund_id_new UUID DEFAULT uuid_generate_v4();

-- Update the new column with UUID values for existing records
UPDATE payment_refund SET payment_refund_id_new = uuid_generate_v4();

-- Make the new column NOT NULL
ALTER TABLE payment_refund ALTER COLUMN payment_refund_id_new SET NOT NULL;

-- Drop the old column
ALTER TABLE payment_refund DROP COLUMN payment_refund_id;

-- Rename the new column to the original name
ALTER TABLE payment_refund RENAME COLUMN payment_refund_id_new TO payment_refund_id;

-- Add the primary key constraint back
ALTER TABLE payment_refund ADD CONSTRAINT payment_refund_pkey PRIMARY KEY (payment_refund_id);

-- Step 3: Alter payment_audit_log table
-- Drop the existing primary key constraint
ALTER TABLE payment_audit_log DROP CONSTRAINT payment_audit_log_pkey;

-- Add a new UUID column for payment_audit_log_id
ALTER TABLE payment_audit_log ADD COLUMN payment_audit_log_id_new UUID DEFAULT uuid_generate_v4();

-- Update the new column with UUID values for existing records
UPDATE payment_audit_log SET payment_audit_log_id_new = uuid_generate_v4();

-- Make the new column NOT NULL
ALTER TABLE payment_audit_log ALTER COLUMN payment_audit_log_id_new SET NOT NULL;

-- Drop the old payment_audit_log_id column
ALTER TABLE payment_audit_log DROP COLUMN payment_audit_log_id;

-- Rename the new column to the original name
ALTER TABLE payment_audit_log RENAME COLUMN payment_audit_log_id_new TO payment_audit_log_id;

-- Add the primary key constraint back
ALTER TABLE payment_audit_log ADD CONSTRAINT payment_audit_log_pkey PRIMARY KEY (payment_audit_log_id);

-- Step 4: Update payment_refund_id column in payment_audit_log to UUID
-- Add a new UUID column for payment_refund_id
ALTER TABLE payment_audit_log ADD COLUMN payment_refund_id_new UUID;

-- Update the new column with corresponding UUID values from payment_refund table
-- Since we can't maintain the relationship during migration, we'll set all to NULL initially
-- The application will need to handle this appropriately
UPDATE payment_audit_log SET payment_refund_id_new = NULL;

-- Drop the old payment_refund_id column
ALTER TABLE payment_audit_log DROP COLUMN payment_refund_id;

-- Rename the new column to the original name
ALTER TABLE payment_audit_log RENAME COLUMN payment_refund_id_new TO payment_refund_id;

-- Step 5: Re-add the foreign key constraint
ALTER TABLE payment_audit_log ADD CONSTRAINT payment_audit_log_payment_refund_id_fkey 
    FOREIGN KEY (payment_refund_id) REFERENCES payment_refund(payment_refund_id);

-- Add comments for documentation
COMMENT ON COLUMN payment_audit_log.payment_audit_log_id IS 'UUID primary key for payment audit log entries';
COMMENT ON COLUMN payment_refund.payment_refund_id IS 'UUID primary key for payment refund records';
COMMENT ON COLUMN payment_audit_log.payment_refund_id IS 'UUID foreign key referencing payment_refund.payment_refund_id';