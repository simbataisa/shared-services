-- =====================================================
-- Payment Service Database Schema
-- Version: V15
-- Description: Create tables for payment management
-- =====================================================

-- =====================================================
-- ENUM TYPES
-- =====================================================

-- Payment request status lifecycle
CREATE TYPE payment_request_status AS ENUM (
    'DRAFT',        -- Initial state, not yet active
    'PENDING',      -- Active, awaiting payment
    'PROCESSING',   -- Payment received, being processed
    'COMPLETED',    -- Successfully completed
    'FAILED',       -- Payment failed
    'CANCELLED',    -- Cancelled by user/admin before payment
    'VOIDED',       -- Voided after successful payment
    'REFUNDED',     -- Full refund processed
    'PARTIAL_REFUND' -- Partial refund processed
);

-- Payment method types
CREATE TYPE payment_method_type AS ENUM (
    'CREDIT_CARD',
    'DEBIT_CARD',
    'BANK_TRANSFER',
    'DIGITAL_WALLET',
    'PAYPAL',
    'STRIPE',
    'MANUAL'
);

-- Payment transaction types
CREATE TYPE payment_transaction_type AS ENUM (
    'PAYMENT',
    'REFUND',
    'VOID',
    'CHARGEBACK'
);

-- Payment transaction status
CREATE TYPE payment_transaction_status AS ENUM (
    'PENDING',
    'SUCCESS',
    'FAILED',
    'CANCELLED'
);

-- =====================================================
-- MAIN TABLES
-- =====================================================

-- Payment Request Table
-- Stores payment request information
CREATE TABLE payment_request (
    payment_request_id BIGSERIAL PRIMARY KEY,

    -- Business Identifiers
    request_code VARCHAR(50) UNIQUE NOT NULL,  -- PR-2025-001234
    payment_token VARCHAR(255) UNIQUE NOT NULL, -- Secure token for payment link

    -- Request Details
    title VARCHAR(255) NOT NULL,
    description TEXT,
    amount DECIMAL(15, 2) NOT NULL CHECK (amount > 0),
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',

    -- Payer Information
    payer_name VARCHAR(255),
    payer_email VARCHAR(255),
    payer_phone VARCHAR(50),

    -- Payment Configuration
    allowed_payment_methods payment_method_type[] NOT NULL DEFAULT ARRAY['CREDIT_CARD', 'DEBIT_CARD']::payment_method_type[],
    pre_selected_payment_method payment_method_type,

    -- Request Status
    status payment_request_status NOT NULL DEFAULT 'DRAFT',

    -- Dates
    expires_at TIMESTAMP,
    paid_at TIMESTAMP,

    -- Multi-tenant
    tenant_id BIGINT NOT NULL,

    -- Metadata
    metadata JSONB DEFAULT '{}'::jsonb,

    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by BIGINT,
    updated_by BIGINT,

    -- Foreign Keys
    FOREIGN KEY (tenant_id) REFERENCES tenant(tenant_id) ON DELETE RESTRICT,
    FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE SET NULL,
    FOREIGN KEY (updated_by) REFERENCES users(user_id) ON DELETE SET NULL,

    -- Constraints
    CONSTRAINT chk_expires_at_future CHECK (expires_at IS NULL OR expires_at > created_at)
);

-- Payment Transaction Table
-- Stores all payment-related transactions
CREATE TABLE payment_transaction (
    payment_transaction_id BIGSERIAL PRIMARY KEY,

    -- Transaction Identifiers
    transaction_code VARCHAR(50) UNIQUE NOT NULL, -- TXN-2025-001234
    external_transaction_id VARCHAR(255),          -- Gateway transaction ID

    -- Transaction Details
    payment_request_id BIGINT NOT NULL,
    transaction_type payment_transaction_type NOT NULL,
    transaction_status payment_transaction_status NOT NULL DEFAULT 'PENDING',

    -- Amount Information
    amount DECIMAL(15, 2) NOT NULL CHECK (amount > 0),
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',

    -- Payment Method
    payment_method payment_method_type NOT NULL,
    payment_method_details JSONB DEFAULT '{}'::jsonb, -- Masked card info, etc.

    -- Gateway Information
    gateway_name VARCHAR(100),
    gateway_response JSONB DEFAULT '{}'::jsonb,

    -- Transaction Dates
    processed_at TIMESTAMP,

    -- Error Information
    error_code VARCHAR(50),
    error_message TEXT,

    -- Metadata
    metadata JSONB DEFAULT '{}'::jsonb,

    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by BIGINT,

    -- Foreign Keys
    FOREIGN KEY (payment_request_id) REFERENCES payment_request(payment_request_id) ON DELETE RESTRICT,
    FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE SET NULL
);

-- Payment Refund Table
-- Stores refund information
CREATE TABLE payment_refund (
    payment_refund_id BIGSERIAL PRIMARY KEY,

    -- Refund Identifiers
    refund_code VARCHAR(50) UNIQUE NOT NULL, -- RFD-2025-001234

    -- Refund Details
    payment_transaction_id BIGINT NOT NULL,
    refund_amount DECIMAL(15, 2) NOT NULL CHECK (refund_amount > 0),
    refund_reason TEXT NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    gateway_name VARCHAR(100),
    error_code VARCHAR(50),
    error_message TEXT,

    -- Refund Status
    refund_status payment_transaction_status NOT NULL DEFAULT 'PENDING',

    -- Gateway Information
    external_refund_id VARCHAR(255),
    gateway_response JSONB DEFAULT '{}'::jsonb,

    -- Dates
    processed_at TIMESTAMP,

    -- Metadata
    metadata JSONB DEFAULT '{}'::jsonb,

    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by BIGINT NOT NULL,

    -- Foreign Keys
    FOREIGN KEY (payment_transaction_id) REFERENCES payment_transaction(payment_transaction_id) ON DELETE RESTRICT,
    FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE SET NULL
);

-- Payment Audit Log Table
-- Comprehensive audit trail for all payment operations
CREATE TABLE payment_audit_log (
    payment_audit_log_id BIGSERIAL PRIMARY KEY,

    -- Reference
    payment_request_id BIGINT,
    payment_transaction_id BIGINT,
    payment_refund_id BIGINT,

    -- Action Details
    action VARCHAR(100) NOT NULL, -- CREATE, UPDATE, VERIFY, VOID, REFUND, CANCEL
    entity_type VARCHAR(50) NOT NULL, -- PAYMENT_REQUEST, TRANSACTION, REFUND

    -- Change Tracking
    old_status VARCHAR(50),
    new_status VARCHAR(50),
    changes JSONB DEFAULT '{}'::jsonb,

    -- Context
    reason TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,

    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by BIGINT,

    -- Foreign Keys
    FOREIGN KEY (payment_request_id) REFERENCES payment_request(payment_request_id) ON DELETE CASCADE,
    FOREIGN KEY (payment_transaction_id) REFERENCES payment_transaction(payment_transaction_id) ON DELETE CASCADE,
    FOREIGN KEY (payment_refund_id) REFERENCES payment_refund(payment_refund_id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE SET NULL
);

-- =====================================================
-- INDEXES
-- =====================================================

-- Payment Request Indexes
CREATE INDEX idx_payment_request_tenant_id ON payment_request(tenant_id);
CREATE INDEX idx_payment_request_status ON payment_request(status);
CREATE INDEX idx_payment_request_payment_token ON payment_request(payment_token);
CREATE INDEX idx_payment_request_created_at ON payment_request(created_at DESC);
CREATE INDEX idx_payment_request_expires_at ON payment_request(expires_at) WHERE expires_at IS NOT NULL;
CREATE INDEX idx_payment_request_payer_email ON payment_request(payer_email);

-- Payment Transaction Indexes
CREATE INDEX idx_payment_transaction_payment_request_id ON payment_transaction(payment_request_id);
CREATE INDEX idx_payment_transaction_status ON payment_transaction(transaction_status);
CREATE INDEX idx_payment_transaction_type ON payment_transaction(transaction_type);
CREATE INDEX idx_payment_transaction_external_id ON payment_transaction(external_transaction_id);
CREATE INDEX idx_payment_transaction_created_at ON payment_transaction(created_at DESC);

-- Payment Refund Indexes
CREATE INDEX idx_payment_refund_transaction_id ON payment_refund(payment_transaction_id);
CREATE INDEX idx_payment_refund_status ON payment_refund(refund_status);
CREATE INDEX idx_payment_refund_created_at ON payment_refund(created_at DESC);

-- Payment Audit Log Indexes
CREATE INDEX idx_payment_audit_log_payment_request_id ON payment_audit_log(payment_request_id);
CREATE INDEX idx_payment_audit_log_transaction_id ON payment_audit_log(payment_transaction_id);
CREATE INDEX idx_payment_audit_log_refund_id ON payment_audit_log(payment_refund_id);
CREATE INDEX idx_payment_audit_log_action ON payment_audit_log(action);
CREATE INDEX idx_payment_audit_log_created_at ON payment_audit_log(created_at DESC);
CREATE INDEX idx_payment_audit_log_created_by ON payment_audit_log(created_by);

-- =====================================================
-- FUNCTIONS AND TRIGGERS
-- =====================================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_payment_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Triggers for updated_at
CREATE TRIGGER update_payment_request_updated_at
    BEFORE UPDATE ON payment_request
    FOR EACH ROW
    EXECUTE FUNCTION update_payment_updated_at_column();

CREATE TRIGGER update_payment_transaction_updated_at
    BEFORE UPDATE ON payment_transaction
    FOR EACH ROW
    EXECUTE FUNCTION update_payment_updated_at_column();

CREATE TRIGGER update_payment_refund_updated_at
    BEFORE UPDATE ON payment_refund
    FOR EACH ROW
    EXECUTE FUNCTION update_payment_updated_at_column();

-- =====================================================
-- COMMENTS
-- =====================================================

COMMENT ON TABLE payment_request IS 'Stores payment requests with link generation support';
COMMENT ON TABLE payment_transaction IS 'Stores all payment-related transactions';
COMMENT ON TABLE payment_refund IS 'Stores refund information for completed payments';
COMMENT ON TABLE payment_audit_log IS 'Comprehensive audit trail for payment operations';

COMMENT ON COLUMN payment_request.payment_token IS 'Secure token for payment link access';
COMMENT ON COLUMN payment_request.allowed_payment_methods IS 'Array of allowed payment methods for this request';
COMMENT ON COLUMN payment_request.pre_selected_payment_method IS 'Optional pre-selected payment method';
COMMENT ON COLUMN payment_request.metadata IS 'Flexible JSON field for additional request metadata';

COMMENT ON COLUMN payment_transaction.external_transaction_id IS 'Transaction ID from payment gateway';
COMMENT ON COLUMN payment_transaction.payment_method_details IS 'Masked payment method details (e.g., last 4 digits of card)';
COMMENT ON COLUMN payment_transaction.gateway_response IS 'Complete response from payment gateway';