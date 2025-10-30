-- =====================================================
-- Payment Refund Enhancement Migration
-- Version: V26
-- Description: Add refund_transaction_id field to payment_refund table
--              to establish proper dual-relationship model per PRD requirements
-- =====================================================

-- Add refund_transaction_id column to payment_refund table
-- This field will link to the REFUND transaction (payment_transaction with type=REFUND)
-- while payment_transaction_id continues to link to the original PAYMENT transaction
ALTER TABLE payment_refund 
ADD COLUMN refund_transaction_id UUID;

-- Add foreign key constraint to ensure referential integrity
ALTER TABLE payment_refund 
ADD CONSTRAINT fk_payment_refund_refund_transaction_id 
FOREIGN KEY (refund_transaction_id) 
REFERENCES payment_transaction(payment_transaction_id) 
ON DELETE RESTRICT;

-- Create index for performance optimization
CREATE INDEX idx_payment_refund_refund_transaction_id 
ON payment_refund(refund_transaction_id);

-- Add comment to document the dual-relationship model
COMMENT ON COLUMN payment_refund.payment_transaction_id IS 'Links to the original PAYMENT transaction being refunded';
COMMENT ON COLUMN payment_refund.refund_transaction_id IS 'Links to the REFUND transaction that executes this refund';

-- Update existing refund records to populate the new field
-- Link each payment_refund record to its corresponding REFUND transaction
UPDATE payment_refund 
SET refund_transaction_id = (
    SELECT pt.payment_transaction_id 
    FROM payment_transaction pt 
    WHERE pt.transaction_type = 'REFUND'::payment_transaction_type
    AND pt.external_transaction_id = payment_refund.external_refund_id
    AND pt.amount = payment_refund.refund_amount
    LIMIT 1
);

-- For any refunds that couldn't be matched by external_refund_id, 
-- try to match by amount and processed_at timestamp (within 1 hour)
UPDATE payment_refund 
SET refund_transaction_id = (
    SELECT pt.payment_transaction_id 
    FROM payment_transaction pt 
    WHERE pt.transaction_type = 'REFUND'::payment_transaction_type
    AND pt.amount = payment_refund.refund_amount
    AND pt.payment_request_id = (
        SELECT pt2.payment_request_id 
        FROM payment_transaction pt2 
        WHERE pt2.payment_transaction_id = payment_refund.payment_transaction_id
    )
    AND ABS(EXTRACT(EPOCH FROM (pt.processed_at - payment_refund.processed_at))) < 3600
    LIMIT 1
)
WHERE refund_transaction_id IS NULL;