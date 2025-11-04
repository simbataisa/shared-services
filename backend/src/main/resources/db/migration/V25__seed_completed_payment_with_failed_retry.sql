-- =====================================================
-- Completed Payment with Failed Retry Seed Data
-- Version: V25
-- Description: Add seed data for a COMPLETED payment request with failed credit card transaction followed by successful bank transfer
-- Note: This script requires V24 migration to be completed first
-- =====================================================

-- =====================================================
-- PAYMENT REQUEST WITH COMPLETED STATUS AND RETRY SCENARIO
-- =====================================================

-- Payment Request for Failed Credit Card + Successful Bank Transfer Scenario
INSERT INTO payment_request (
    request_code, payment_token, title, description, amount, currency,
    payer_name, payer_email, payer_phone,
    allowed_payment_methods, pre_selected_payment_method,
    status, expires_at, paid_at, tenant_id, metadata,
    created_at, updated_at, created_by
) VALUES (
    'PR-2025-000009',
    'retry_token_009_' || substr(md5(random()::text), 1, 16),
    'Cloud Infrastructure Migration',
    'Complete cloud infrastructure migration and setup - Payment completed after initial card failure',
    4500.00,
    'USD',
    'Sarah Johnson',
    'sarah.johnson@cloudtech.com',
    '+1-555-0109',
    ARRAY['CREDIT_CARD', 'DEBIT_CARD', 'BANK_TRANSFER']::payment_method_type[],
    'CREDIT_CARD'::payment_method_type,
    'COMPLETED'::payment_request_status,
    CURRENT_TIMESTAMP + INTERVAL '45 days',
    CURRENT_TIMESTAMP - INTERVAL '2 days',
    1,
    '{"project_id": "CLOUD-MIG-001", "infrastructure": "AWS", "services": ["EC2", "RDS", "S3"], "retry_scenario": true}'::jsonb,
    CURRENT_TIMESTAMP - INTERVAL '12 days',
    CURRENT_TIMESTAMP - INTERVAL '2 days',
    1
);

-- =====================================================
-- PAYMENT TRANSACTIONS FOR RETRY SCENARIO
-- =====================================================

-- First Transaction: FAILED Credit Card Payment via Stripe
INSERT INTO payment_transaction (
    payment_transaction_id, transaction_code, external_transaction_id,
    payment_request_id, transaction_type, transaction_status,
    amount, currency, payment_method, payment_method_details,
    gateway_name, gateway_response, processed_at,
    error_code, error_message, retry_count, max_retries,
    metadata, created_at, updated_at, created_by
) VALUES (
    gen_random_uuid(),
    'TXN-2025-000009',
    'stripe_pi_3N9876543210987654',
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000009'),
    'PAYMENT'::payment_transaction_type,
    'FAILED'::payment_transaction_status,
    4500.00,
    'USD',
    'CREDIT_CARD'::payment_method_type,
    '{"card_brand": "mastercard", "last_four": "5555", "exp_month": "08", "exp_year": "2026", "cvc_check": "pass"}'::jsonb,
    'Stripe',
    '{"id": "stripe_pi_3N9876543210987654", "status": "failed", "failure_code": "card_declined", "failure_message": "Your card was declined.", "decline_code": "generic_decline"}'::jsonb,
    CURRENT_TIMESTAMP - INTERVAL '5 days',
    'card_declined',
    'Your card was declined. Please try a different payment method or contact your bank.',
    1,
    3,
    '{"attempt": 1, "gateway": "stripe", "decline_reason": "generic_decline"}'::jsonb,
    CURRENT_TIMESTAMP - INTERVAL '5 days',
    CURRENT_TIMESTAMP - INTERVAL '5 days',
    1
);

-- Second Transaction: SUCCESSFUL Bank Transfer Payment
INSERT INTO payment_transaction (
    payment_transaction_id, transaction_code, external_transaction_id,
    payment_request_id, transaction_type, transaction_status,
    amount, currency, payment_method, payment_method_details,
    gateway_name, gateway_response, processed_at,
    error_code, error_message, retry_count, max_retries,
    metadata, created_at, updated_at, created_by
) VALUES (
    gen_random_uuid(),
    'TXN-2025-000010',
    'bank_transfer_bt_2025_000010',
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000009'),
    'PAYMENT'::payment_transaction_type,
    'SUCCESS'::payment_transaction_status,
    4500.00,
    'USD',
    'BANK_TRANSFER'::payment_method_type,
    '{"bank_name": "Chase Bank", "account_type": "checking", "routing_number": "****1234", "account_number": "****5678"}'::jsonb,
    'Internal Bank Transfer',
    '{"id": "bank_transfer_bt_2025_000010", "status": "completed", "processing_time": "2-3 business days", "confirmation_code": "BT2025010"}'::jsonb,
    CURRENT_TIMESTAMP - INTERVAL '2 days',
    NULL,
    NULL,
    0,
    3,
    '{"attempt": 2, "payment_method_switch": true, "original_failed_method": "CREDIT_CARD"}'::jsonb,
    CURRENT_TIMESTAMP - INTERVAL '3 days',
    CURRENT_TIMESTAMP - INTERVAL '2 days',
    1
);

-- =====================================================
-- AUDIT LOG ENTRIES FOR COMPLETE PAYMENT LIFECYCLE
-- =====================================================

INSERT INTO payment_audit_log (
    payment_audit_log_id, payment_request_id, payment_transaction_id, payment_refund_id,
    action, entity_type, old_status, new_status, changes,
    reason, ip_address, user_agent, created_at, created_by
) VALUES 
-- Payment Request Created
(
    gen_random_uuid(),
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000009'),
    NULL, NULL, 'CREATE', 'PAYMENT_REQUEST', NULL, 'DRAFT',
    '{"title": "Cloud Infrastructure Migration", "amount": 4500.00, "currency": "USD", "payer_name": "Sarah Johnson"}'::jsonb,
    'Payment request created for cloud infrastructure migration',
    '192.168.1.15', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36',
    CURRENT_TIMESTAMP - INTERVAL '12 days', 1
),
-- Payment Request Sent to Customer
(
    gen_random_uuid(),
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000009'),
    NULL, NULL, 'UPDATE', 'PAYMENT_REQUEST', 'DRAFT', 'PENDING',
    '{"status": "PENDING", "updated_reason": "Payment request sent to customer"}'::jsonb,
    'Payment request sent to customer for payment',
    '192.168.1.15', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36',
    CURRENT_TIMESTAMP - INTERVAL '10 days', 1
),
-- First Payment Transaction Created (Credit Card)
(
    gen_random_uuid(),
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000009'),
    (SELECT payment_transaction_id FROM payment_transaction WHERE transaction_code = 'TXN-2025-000009'),
    NULL, 'CREATE', 'TRANSACTION', NULL, 'PENDING',
    '{"transaction_code": "TXN-2025-000009", "amount": 4500.00, "payment_method": "CREDIT_CARD", "gateway": "Stripe"}'::jsonb,
    'Customer initiated payment via credit card through Stripe',
    '203.0.113.45', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
    CURRENT_TIMESTAMP - INTERVAL '5 days' - INTERVAL '30 minutes', 1
),
-- First Payment Transaction Failed
(
    gen_random_uuid(),
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000009'),
    (SELECT payment_transaction_id FROM payment_transaction WHERE transaction_code = 'TXN-2025-000009'),
    NULL, 'UPDATE', 'TRANSACTION', 'PENDING', 'FAILED',
    '{"status": "FAILED", "error_code": "card_declined", "error_message": "Your card was declined. Please try a different payment method or contact your bank.", "gateway_response": {"decline_code": "generic_decline"}}'::jsonb,
    'Credit card payment failed - card declined by issuer',
    '203.0.113.45', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
    CURRENT_TIMESTAMP - INTERVAL '5 days', 1
),
-- Payment Request Status Updated to Processing (Customer Retry)
(
    gen_random_uuid(),
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000009'),
    NULL, NULL, 'UPDATE', 'PAYMENT_REQUEST', 'PENDING', 'PROCESSING',
    '{"status": "PROCESSING", "updated_reason": "Customer attempting payment with different method"}'::jsonb,
    'Customer attempting payment with bank transfer after credit card failure',
    '203.0.113.45', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
    CURRENT_TIMESTAMP - INTERVAL '3 days' - INTERVAL '30 minutes', 1
),
-- Second Payment Transaction Created (Bank Transfer)
(
    gen_random_uuid(),
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000009'),
    (SELECT payment_transaction_id FROM payment_transaction WHERE transaction_code = 'TXN-2025-000010'),
    NULL, 'CREATE', 'TRANSACTION', NULL, 'PENDING',
    '{"transaction_code": "TXN-2025-000010", "amount": 4500.00, "payment_method": "BANK_TRANSFER", "retry_after_failed": "TXN-2025-000009"}'::jsonb,
    'Customer initiated bank transfer payment after credit card failure',
    '203.0.113.45', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
    CURRENT_TIMESTAMP - INTERVAL '3 days', 1
),
-- Second Payment Transaction Successful
(
    gen_random_uuid(),
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000009'),
    (SELECT payment_transaction_id FROM payment_transaction WHERE transaction_code = 'TXN-2025-000010'),
    NULL, 'UPDATE', 'TRANSACTION', 'PENDING', 'SUCCESS',
    '{"status": "SUCCESS", "processing_time": "2-3 business days", "confirmation_code": "BT2025010"}'::jsonb,
    'Bank transfer payment processed successfully',
    '203.0.113.45', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
    CURRENT_TIMESTAMP - INTERVAL '2 days', 1
),
-- Payment Request Completed
(
    gen_random_uuid(),
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000009'),
    NULL, NULL, 'UPDATE', 'PAYMENT_REQUEST', 'PROCESSING', 'COMPLETED',
    jsonb_build_object(
        'status', 'COMPLETED',
        'paid_at', (CURRENT_TIMESTAMP - INTERVAL '2 days')::text,
        'successful_transaction', 'TXN-2025-000010'
    ),
    'Payment request completed successfully via bank transfer after initial credit card failure',
    '192.168.1.15', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36',
    CURRENT_TIMESTAMP - INTERVAL '2 days', 1
);

-- =====================================================
-- SUMMARY
-- =====================================================
-- This migration adds:
-- - 1 new payment request with COMPLETED status (PR-2025-000009)
-- - 2 payment transactions:
--   * 1 FAILED credit card transaction via Stripe (TXN-2025-000009)
--   * 1 SUCCESS bank transfer transaction (TXN-2025-000010)
-- - 8 comprehensive audit log entries tracking the complete payment lifecycle
-- - Realistic retry scenario demonstrating payment method fallback
-- - Proper foreign key relationships and realistic timestamps
-- - Demonstrates customer resilience in completing payment after initial failure