-- =====================================================
-- Payment Transaction and Audit Log Seed Data
-- Version: V21
-- Description: Seed payment_transaction and payment_audit_log tables with realistic data
-- Note: This script requires V19 and V20 migrations to be completed first
-- =====================================================

-- =====================================================
-- PAYMENT TRANSACTIONS SEED DATA
-- =====================================================

-- Transaction for COMPLETED Payment Request (PR-2025-000003)
-- This payment request was completed, so it should have a successful transaction
INSERT INTO payment_transaction (
    payment_transaction_id, transaction_code, external_transaction_id,
    payment_request_id, transaction_type, transaction_status,
    amount, currency, payment_method, payment_method_details,
    gateway_name, gateway_response, processed_at,
    error_code, error_message, metadata, created_at, updated_at, created_by
) VALUES (
    gen_random_uuid(),
    'TXN-2025-000001',
    'stripe_pi_3N1234567890123456',
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000003'),
    'PAYMENT'::payment_transaction_type,
    'SUCCESS'::payment_transaction_status,
    5000.00,
    'USD',
    'CREDIT_CARD'::payment_method_type,
    '{"card_brand": "visa", "last_four": "4242", "exp_month": "12", "exp_year": "2027"}'::jsonb,
    'Stripe',
    '{"payment_intent_id": "pi_3N1234567890123456", "charge_id": "ch_3N1234567890123456", "receipt_url": "https://pay.stripe.com/receipts/..."}'::jsonb,
    CURRENT_TIMESTAMP - INTERVAL '5 days',
    NULL,
    NULL,
    '{"customer_ip": "192.168.1.100", "user_agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"}'::jsonb,
    CURRENT_TIMESTAMP - INTERVAL '5 days',
    CURRENT_TIMESTAMP - INTERVAL '5 days',
    1
);

-- Transaction for PENDING Payment Request (PR-2025-000002)
-- This payment request is pending, so it has an attempted but pending transaction
INSERT INTO payment_transaction (
    payment_transaction_id, transaction_code, external_transaction_id,
    payment_request_id, transaction_type, transaction_status,
    amount, currency, payment_method, payment_method_details,
    gateway_name, gateway_response, processed_at,
    error_code, error_message, metadata, created_at, updated_at, created_by
) VALUES (
    gen_random_uuid(),
    'TXN-2025-000002',
    'paypal_txn_ABC123DEF456',
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000002'),
    'PAYMENT'::payment_transaction_type,
    'PENDING'::payment_transaction_status,
    99.99,
    'USD',
    'DIGITAL_WALLET'::payment_method_type,
    '{"wallet_type": "paypal", "payer_email": "sarah.johnson@example.com"}'::jsonb,
    'PayPal',
    '{"payment_id": "PAYID-ABC123DEF456", "status": "PENDING", "payer_status": "VERIFIED"}'::jsonb,
    CURRENT_TIMESTAMP - INTERVAL '1 day',
    NULL,
    NULL,
    '{"customer_ip": "10.0.0.50", "user_agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15"}'::jsonb,
    CURRENT_TIMESTAMP - INTERVAL '1 day',
    CURRENT_TIMESTAMP - INTERVAL '1 day',
    1
);

-- Failed Transaction for FAILED Payment Request (PR-2025-000005)
-- This payment request failed, so it has a failed transaction
INSERT INTO payment_transaction (
    payment_transaction_id, transaction_code, external_transaction_id,
    payment_request_id, transaction_type, transaction_status,
    amount, currency, payment_method, payment_method_details,
    gateway_name, gateway_response, processed_at,
    error_code, error_message, metadata, created_at, updated_at, created_by
) VALUES (
    gen_random_uuid(),
    'TXN-2025-000003',
    'stripe_pi_3N9876543210987654',
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000005'),
    'PAYMENT'::payment_transaction_type,
    'FAILED'::payment_transaction_status,
    3200.00,
    'USD',
    'BANK_TRANSFER'::payment_method_type,
    '{"bank_name": "Chase Bank", "account_type": "checking", "routing_number": "****1234"}'::jsonb,
    'Stripe',
    '{"payment_intent_id": "pi_3N9876543210987654", "error": {"code": "insufficient_funds", "message": "Your card has insufficient funds."}}'::jsonb,
    CURRENT_TIMESTAMP - INTERVAL '5 days',
    'insufficient_funds',
    'Your card has insufficient funds.',
    '{"customer_ip": "172.16.0.25", "user_agent": "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36"}'::jsonb,
    CURRENT_TIMESTAMP - INTERVAL '5 days',
    CURRENT_TIMESTAMP - INTERVAL '5 days',
    1
);

-- Cancelled Transaction for CANCELLED Payment Request (PR-2025-000004)
-- This payment request was cancelled, so it has a cancelled transaction
INSERT INTO payment_transaction (
    payment_transaction_id, transaction_code, external_transaction_id,
    payment_request_id, transaction_type, transaction_status,
    amount, currency, payment_method, payment_method_details,
    gateway_name, gateway_response, processed_at,
    error_code, error_message, metadata, created_at, updated_at, created_by
) VALUES (
    gen_random_uuid(),
    'TXN-2025-000004',
    'square_txn_XYZ789ABC123',
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000004'),
    'PAYMENT'::payment_transaction_type,
    'CANCELLED'::payment_transaction_status,
    7500.00,
    'USD',
    'BANK_TRANSFER'::payment_method_type,
    '{"bank_name": "Bank of America", "account_type": "business", "routing_number": "****5678"}'::jsonb,
    'Square',
    '{"payment_id": "XYZ789ABC123", "status": "CANCELLED", "cancelled_at": "2025-01-03T10:30:00Z"}'::jsonb,
    CURRENT_TIMESTAMP - INTERVAL '3 days',
    NULL,
    NULL,
    '{"customer_ip": "203.0.113.45", "user_agent": "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15"}'::jsonb,
    CURRENT_TIMESTAMP - INTERVAL '3 days',
    CURRENT_TIMESTAMP - INTERVAL '3 days',
    1
);

-- =====================================================
-- PAYMENT AUDIT LOGS SEED DATA
-- =====================================================

-- Audit logs for DRAFT Payment Request (PR-2025-000001)
INSERT INTO payment_audit_log (
    payment_request_id, payment_transaction_id, payment_refund_id,
    action, entity_type, old_status, new_status, changes,
    reason, ip_address, user_agent, created_at, created_by
) VALUES (
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000001'),
    NULL,
    NULL,
    'CREATE',
    'PAYMENT_REQUEST',
    NULL,
    'DRAFT',
    '{"title": "Website Development Invoice", "amount": 2500.00, "currency": "USD", "payer_name": "John Smith"}'::jsonb,
    'Payment request created by admin',
    '192.168.1.10',
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
    CURRENT_TIMESTAMP - INTERVAL '2 days',
    1
);

-- Audit logs for PENDING Payment Request (PR-2025-000002)
INSERT INTO payment_audit_log (
    payment_request_id, payment_transaction_id, payment_refund_id,
    action, entity_type, old_status, new_status, changes,
    reason, ip_address, user_agent, created_at, created_by
) VALUES (
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000002'),
    NULL,
    NULL,
    'CREATE',
    'PAYMENT_REQUEST',
    NULL,
    'DRAFT',
    '{"title": "Monthly Subscription Fee", "amount": 99.99, "currency": "USD", "payer_name": "Sarah Johnson"}'::jsonb,
    'Payment request created by admin',
    '192.168.1.10',
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
    CURRENT_TIMESTAMP - INTERVAL '1 day' - INTERVAL '2 hours',
    1
),
(
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000002'),
    NULL,
    NULL,
    'UPDATE',
    'PAYMENT_REQUEST',
    'DRAFT',
    'PENDING',
    '{"status": "PENDING", "updated_reason": "Payment request sent to customer"}'::jsonb,
    'Payment request sent to customer for payment',
    '192.168.1.10',
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
    CURRENT_TIMESTAMP - INTERVAL '1 day',
    1
),
(
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000002'),
    (SELECT payment_transaction_id FROM payment_transaction WHERE transaction_code = 'TXN-2025-000002'),
    NULL,
    'CREATE',
    'TRANSACTION',
    NULL,
    'PENDING',
    '{"transaction_code": "TXN-2025-000002", "amount": 99.99, "payment_method": "DIGITAL_WALLET"}'::jsonb,
    'Customer initiated payment via PayPal',
    '10.0.0.50',
    'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15',
    CURRENT_TIMESTAMP - INTERVAL '1 day',
    1
);

-- Audit logs for COMPLETED Payment Request (PR-2025-000003)
INSERT INTO payment_audit_log (
    payment_request_id, payment_transaction_id, payment_refund_id,
    action, entity_type, old_status, new_status, changes,
    reason, ip_address, user_agent, created_at, created_by
) VALUES (
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000003'),
    NULL,
    NULL,
    'CREATE',
    'PAYMENT_REQUEST',
    NULL,
    'DRAFT',
    '{"title": "E-commerce Platform Setup", "amount": 5000.00, "currency": "USD", "payer_name": "Michael Brown"}'::jsonb,
    'Payment request created by admin',
    '192.168.1.10',
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
    CURRENT_TIMESTAMP - INTERVAL '10 days',
    1
),
(
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000003'),
    NULL,
    NULL,
    'UPDATE',
    'PAYMENT_REQUEST',
    'DRAFT',
    'PENDING',
    '{"status": "PENDING", "updated_reason": "Payment request sent to customer"}'::jsonb,
    'Payment request sent to customer for payment',
    '192.168.1.10',
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
    CURRENT_TIMESTAMP - INTERVAL '8 days',
    1
),
(
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000003'),
    (SELECT payment_transaction_id FROM payment_transaction WHERE transaction_code = 'TXN-2025-000001'),
    NULL,
    'CREATE',
    'TRANSACTION',
    NULL,
    'PENDING',
    '{"transaction_code": "TXN-2025-000001", "amount": 5000.00, "payment_method": "CREDIT_CARD"}'::jsonb,
    'Customer initiated payment via credit card',
    '192.168.1.100',
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
    CURRENT_TIMESTAMP - INTERVAL '5 days' - INTERVAL '30 minutes',
    1
),
(
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000003'),
    (SELECT payment_transaction_id FROM payment_transaction WHERE transaction_code = 'TXN-2025-000001'),
    NULL,
    'UPDATE',
    'TRANSACTION',
    'PENDING',
    'SUCCESS',
    '{"status": "SUCCESS", "external_transaction_id": "stripe_pi_3N1234567890123456"}'::jsonb,
    'Payment successfully processed by Stripe',
    '192.168.1.100',
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
    CURRENT_TIMESTAMP - INTERVAL '5 days',
    1
),
(
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000003'),
    NULL,
    NULL,
    'UPDATE',
    'PAYMENT_REQUEST',
    'PENDING',
    'COMPLETED',
    jsonb_build_object('status', 'COMPLETED', 'paid_at', (CURRENT_TIMESTAMP - INTERVAL '5 days')::text),
    'Payment request completed successfully',
    '192.168.1.10',
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
    CURRENT_TIMESTAMP - INTERVAL '5 days',
    1
);

-- Audit logs for CANCELLED Payment Request (PR-2025-000004)
INSERT INTO payment_audit_log (
    payment_request_id, payment_transaction_id, payment_refund_id,
    action, entity_type, old_status, new_status, changes,
    reason, ip_address, user_agent, created_at, created_by
) VALUES (
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000004'),
    NULL,
    NULL,
    'CREATE',
    'PAYMENT_REQUEST',
    NULL,
    'DRAFT',
    '{"title": "Mobile App Development", "amount": 7500.00, "currency": "USD", "payer_name": "Emily Davis"}'::jsonb,
    'Payment request created by admin',
    '192.168.1.10',
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
    CURRENT_TIMESTAMP - INTERVAL '8 days',
    1
),
(
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000004'),
    NULL,
    NULL,
    'UPDATE',
    'PAYMENT_REQUEST',
    'DRAFT',
    'PENDING',
    '{"status": "PENDING", "updated_reason": "Payment request sent to customer"}'::jsonb,
    'Payment request sent to customer for payment',
    '192.168.1.10',
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
    CURRENT_TIMESTAMP - INTERVAL '6 days',
    1
),
(
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000004'),
    (SELECT payment_transaction_id FROM payment_transaction WHERE transaction_code = 'TXN-2025-000004'),
    NULL,
    'CREATE',
    'TRANSACTION',
    NULL,
    'PENDING',
    '{"transaction_code": "TXN-2025-000004", "amount": 7500.00, "payment_method": "BANK_TRANSFER"}'::jsonb,
    'Customer initiated payment via bank transfer',
    '203.0.113.45',
    'Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15',
    CURRENT_TIMESTAMP - INTERVAL '4 days',
    1
),
(
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000004'),
    (SELECT payment_transaction_id FROM payment_transaction WHERE transaction_code = 'TXN-2025-000004'),
    NULL,
    'UPDATE',
    'TRANSACTION',
    'PENDING',
    'CANCELLED',
    '{"status": "CANCELLED", "cancelled_reason": "Customer requested cancellation"}'::jsonb,
    'Customer requested to cancel the payment',
    '203.0.113.45',
    'Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15',
    CURRENT_TIMESTAMP - INTERVAL '3 days',
    1
),
(
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000004'),
    NULL,
    NULL,
    'UPDATE',
    'PAYMENT_REQUEST',
    'PENDING',
    'CANCELLED',
    '{"status": "CANCELLED", "cancelled_reason": "Customer requested project cancellation"}'::jsonb,
    'Payment request cancelled due to project cancellation',
    '192.168.1.10',
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
    CURRENT_TIMESTAMP - INTERVAL '3 days',
    1
);

-- Audit logs for FAILED Payment Request (PR-2025-000005)
INSERT INTO payment_audit_log (
    payment_request_id, payment_transaction_id, payment_refund_id,
    action, entity_type, old_status, new_status, changes,
    reason, ip_address, user_agent, created_at, created_by
) VALUES (
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000005'),
    NULL,
    NULL,
    'CREATE',
    'PAYMENT_REQUEST',
    NULL,
    'DRAFT',
    '{"title": "Database Migration Service", "amount": 3200.00, "currency": "USD", "payer_name": "Robert Wilson"}'::jsonb,
    'Payment request created by admin',
    '192.168.1.10',
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
    CURRENT_TIMESTAMP - INTERVAL '20 days',
    1
),
(
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000005'),
    NULL,
    NULL,
    'UPDATE',
    'PAYMENT_REQUEST',
    'DRAFT',
    'PENDING',
    '{"status": "PENDING", "updated_reason": "Payment request sent to customer"}'::jsonb,
    'Payment request sent to customer for payment',
    '192.168.1.10',
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
    CURRENT_TIMESTAMP - INTERVAL '15 days',
    1
),
(
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000005'),
    (SELECT payment_transaction_id FROM payment_transaction WHERE transaction_code = 'TXN-2025-000003'),
    NULL,
    'CREATE',
    'TRANSACTION',
    NULL,
    'PENDING',
    '{"transaction_code": "TXN-2025-000003", "amount": 3200.00, "payment_method": "BANK_TRANSFER"}'::jsonb,
    'Customer initiated payment via bank transfer',
    '172.16.0.25',
    'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36',
    CURRENT_TIMESTAMP - INTERVAL '5 days' - INTERVAL '1 hour',
    1
),
(
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000005'),
    (SELECT payment_transaction_id FROM payment_transaction WHERE transaction_code = 'TXN-2025-000003'),
    NULL,
    'UPDATE',
    'TRANSACTION',
    'PENDING',
    'FAILED',
    '{"status": "FAILED", "error_code": "insufficient_funds", "error_message": "Your card has insufficient funds."}'::jsonb,
    'Payment failed due to insufficient funds',
    '172.16.0.25',
    'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36',
    CURRENT_TIMESTAMP - INTERVAL '5 days',
    1
),
(
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000005'),
    NULL,
    NULL,
    'UPDATE',
    'PAYMENT_REQUEST',
    'PENDING',
    'FAILED',
    '{"status": "FAILED", "failed_reason": "Payment transaction failed"}'::jsonb,
    'Payment request marked as failed due to transaction failure',
    '192.168.1.10',
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
    CURRENT_TIMESTAMP - INTERVAL '5 days',
    1
);

-- =====================================================
-- SUMMARY
-- =====================================================
-- This migration adds:
-- - 4 payment transactions (1 completed, 1 pending, 1 failed, 1 cancelled)
-- - 17 audit log entries tracking the lifecycle of all 5 payment requests
-- - Realistic data with proper timestamps, IP addresses, and user agents
-- - Proper foreign key relationships between payment requests, transactions, and audit logs