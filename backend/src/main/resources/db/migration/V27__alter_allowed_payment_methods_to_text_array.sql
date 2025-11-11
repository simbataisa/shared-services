-- =====================================================
-- Payment Service Database Schema Migration
-- Version: V27
-- Description: Alter allowed_payment_methods column from payment_method_type[] to text[]
--              to support Hibernate 6.x mapping
-- =====================================================

-- Cast the existing enum array to text array
ALTER TABLE payment_request
    ALTER COLUMN allowed_payment_methods TYPE text[]
    USING allowed_payment_methods::text[];

-- Update column comment
COMMENT ON COLUMN payment_request.allowed_payment_methods IS 'Array of allowed payment methods (stored as text array for JPA compatibility)';
