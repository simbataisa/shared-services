-- =====================================================
-- Refund and Void Payment Seed Data
-- Version: V23
-- Description: Add seed data for Full Refund, Partial Refund, and Void Payment scenarios
-- Note: This script requires V19, V20, V21, and V22 migrations to be completed first
-- =====================================================

-- =====================================================
-- ADDITIONAL COMPLETED PAYMENT REQUESTS FOR REFUND/VOID SCENARIOS
-- =====================================================

-- Payment Request for Full Refund Scenario
INSERT INTO payment_request (
    request_code, payment_token, title, description, amount, currency,
    payer_name, payer_email, payer_phone,
    allowed_payment_methods, pre_selected_payment_method,
    status, expires_at, paid_at, tenant_id, metadata,
    created_at, updated_at, created_by
) VALUES (
    'PR-2025-000006',
    'refund_token_006_' || substr(md5(random()::text), 1, 16),
    'Software License Annual Subscription',
    'Annual software license subscription - Full refund requested due to business closure',
    1200.00,
    'USD',
    'Jennifer Martinez',
    'jennifer.martinez@techstartup.com',
    '+1-555-0106',
    ARRAY['CREDIT_CARD', 'DIGITAL_WALLET']::payment_method_type[],
    'CREDIT_CARD'::payment_method_type,
    'REFUNDED'::payment_request_status,
    CURRENT_TIMESTAMP + INTERVAL '30 days',
    CURRENT_TIMESTAMP - INTERVAL '15 days',
    1,
    '{"invoice_number": "INV-2025-006", "department": "IT", "project_code": "PROJ-SW-001"}'::jsonb,
    CURRENT_TIMESTAMP - INTERVAL '20 days',
    CURRENT_TIMESTAMP - INTERVAL '2 days',
    1
);

-- Payment Request for Partial Refund Scenario
INSERT INTO payment_request (
    request_code, payment_token, title, description, amount, currency,
    payer_name, payer_email, payer_phone,
    allowed_payment_methods, pre_selected_payment_method,
    status, expires_at, paid_at, tenant_id, metadata,
    created_at, updated_at, created_by
) VALUES (
    'PR-2025-000007',
    'partial_token_007_' || substr(md5(random()::text), 1, 16),
    'Conference Registration and Workshop Bundle',
    'Tech conference registration with workshops - Partial refund for cancelled workshop',
    850.00,
    'USD',
    'David Chen',
    'david.chen@innovatetech.com',
    '+1-555-0107',
    ARRAY['CREDIT_CARD', 'BANK_TRANSFER']::payment_method_type[],
    'CREDIT_CARD'::payment_method_type,
    'PARTIAL_REFUND'::payment_request_status,
    CURRENT_TIMESTAMP + INTERVAL '45 days',
    CURRENT_TIMESTAMP - INTERVAL '12 days',
    1,
    '{"conference_id": "CONF-2025-TECH", "workshop_ids": ["WS-001", "WS-002"], "attendee_type": "professional"}'::jsonb,
    CURRENT_TIMESTAMP - INTERVAL '18 days',
    CURRENT_TIMESTAMP - INTERVAL '3 days',
    1
);

-- Payment Request for Void Payment Scenario
INSERT INTO payment_request (
    request_code, payment_token, title, description, amount, currency,
    payer_name, payer_email, payer_phone,
    allowed_payment_methods, pre_selected_payment_method,
    status, expires_at, paid_at, tenant_id, metadata,
    created_at, updated_at, created_by
) VALUES (
    'PR-2025-000008',
    'void_token_008_' || substr(md5(random()::text), 1, 16),
    'Duplicate Payment - Marketing Campaign Setup',
    'Marketing campaign setup fee - Voided due to duplicate payment processing',
    2800.00,
    'USD',
    'Lisa Thompson',
    'lisa.thompson@marketingpro.com',
    '+1-555-0108',
    ARRAY['CREDIT_CARD', 'DIGITAL_WALLET', 'BANK_TRANSFER']::payment_method_type[],
    'DIGITAL_WALLET'::payment_method_type,
    'VOIDED'::payment_request_status,
    CURRENT_TIMESTAMP + INTERVAL '60 days',
    CURRENT_TIMESTAMP - INTERVAL '8 days',
    1,
    '{"campaign_id": "CAMP-2025-001", "duplicate_of": "PR-2025-000009", "void_reason": "duplicate_processing"}'::jsonb,
    CURRENT_TIMESTAMP - INTERVAL '10 days',
    CURRENT_TIMESTAMP - INTERVAL '1 day',
    1
);

-- =====================================================
-- SUCCESSFUL PAYMENT TRANSACTIONS FOR REFUND/VOID SCENARIOS
-- =====================================================

-- Successful Payment Transaction for Full Refund Scenario (PR-2025-000006)
INSERT INTO payment_transaction (
    payment_transaction_id, transaction_code, external_transaction_id,
    payment_request_id, transaction_type, transaction_status,
    amount, currency, payment_method, payment_method_details,
    gateway_name, gateway_response, processed_at,
    error_code, error_message, metadata, retry_count, max_retries,
    created_at, updated_at, created_by
) VALUES (
    gen_random_uuid(),
    'TXN-2025-000005',
    'stripe_pi_3N5678901234567890',
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000006'),
    'PAYMENT'::payment_transaction_type,
    'SUCCESS'::payment_transaction_status,
    1200.00,
    'USD',
    'CREDIT_CARD'::payment_method_type,
    '{"card_brand": "mastercard", "last_four": "5555", "exp_month": "08", "exp_year": "2028"}'::jsonb,
    'Stripe',
    '{"payment_intent_id": "pi_3N5678901234567890", "charge_id": "ch_3N5678901234567890", "receipt_url": "https://pay.stripe.com/receipts/acct_1234567890/ch_3N5678901234567890"}'::jsonb,
    CURRENT_TIMESTAMP - INTERVAL '15 days',
    NULL,
    NULL,
    '{"customer_ip": "198.51.100.10", "user_agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36"}'::jsonb,
    0,
    3,
    CURRENT_TIMESTAMP - INTERVAL '15 days',
    CURRENT_TIMESTAMP - INTERVAL '15 days',
    1
);

-- Successful Payment Transaction for Partial Refund Scenario (PR-2025-000007)
INSERT INTO payment_transaction (
    payment_transaction_id, transaction_code, external_transaction_id,
    payment_request_id, transaction_type, transaction_status,
    amount, currency, payment_method, payment_method_details,
    gateway_name, gateway_response, processed_at,
    error_code, error_message, metadata, retry_count, max_retries,
    created_at, updated_at, created_by
) VALUES (
    gen_random_uuid(),
    'TXN-2025-000006',
    'stripe_pi_3N6789012345678901',
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000007'),
    'PAYMENT'::payment_transaction_type,
    'SUCCESS'::payment_transaction_status,
    850.00,
    'USD',
    'CREDIT_CARD'::payment_method_type,
    '{"card_brand": "visa", "last_four": "1234", "exp_month": "11", "exp_year": "2026"}'::jsonb,
    'Stripe',
    '{"payment_intent_id": "pi_3N6789012345678901", "charge_id": "ch_3N6789012345678901", "receipt_url": "https://pay.stripe.com/receipts/acct_1234567890/ch_3N6789012345678901"}'::jsonb,
    CURRENT_TIMESTAMP - INTERVAL '12 days',
    NULL,
    NULL,
    '{"customer_ip": "203.0.113.100", "user_agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"}'::jsonb,
    0,
    3,
    CURRENT_TIMESTAMP - INTERVAL '12 days',
    CURRENT_TIMESTAMP - INTERVAL '12 days',
    1
);

-- Successful Payment Transaction for Void Scenario (PR-2025-000008)
INSERT INTO payment_transaction (
    payment_transaction_id, transaction_code, external_transaction_id,
    payment_request_id, transaction_type, transaction_status,
    amount, currency, payment_method, payment_method_details,
    gateway_name, gateway_response, processed_at,
    error_code, error_message, metadata, retry_count, max_retries,
    created_at, updated_at, created_by
) VALUES (
    gen_random_uuid(),
    'TXN-2025-000007',
    'paypal_txn_DEF456GHI789',
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000008'),
    'PAYMENT'::payment_transaction_type,
    'SUCCESS'::payment_transaction_status,
    2800.00,
    'USD',
    'DIGITAL_WALLET'::payment_method_type,
    '{"wallet_type": "paypal", "payer_email": "lisa.thompson@marketingpro.com"}'::jsonb,
    'PayPal',
    '{"payment_id": "PAYID-DEF456GHI789", "status": "COMPLETED", "payer_status": "VERIFIED"}'::jsonb,
    CURRENT_TIMESTAMP - INTERVAL '8 days',
    NULL,
    NULL,
    '{"customer_ip": "192.0.2.50", "user_agent": "Mozilla/5.0 (iPhone; CPU iPhone OS 17_1 like Mac OS X) AppleWebKit/605.1.15"}'::jsonb,
    0,
    3,
    CURRENT_TIMESTAMP - INTERVAL '8 days',
    CURRENT_TIMESTAMP - INTERVAL '8 days',
    1
);

-- =====================================================
-- REFUND TRANSACTIONS
-- =====================================================

-- Full Refund Transaction for PR-2025-000006
INSERT INTO payment_transaction (
    payment_transaction_id, transaction_code, external_transaction_id,
    payment_request_id, transaction_type, transaction_status,
    amount, currency, payment_method, payment_method_details,
    gateway_name, gateway_response, processed_at,
    error_code, error_message, metadata, retry_count, max_retries,
    created_at, updated_at, created_by
) VALUES (
    gen_random_uuid(),
    'TXN-RFD-2025-000001',
    'stripe_re_3N5678901234567890',
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000006'),
    'REFUND'::payment_transaction_type,
    'SUCCESS'::payment_transaction_status,
    1200.00, -- Positive amount for refund
    'USD',
    'CREDIT_CARD'::payment_method_type,
    '{"card_brand": "mastercard", "last_four": "5555", "refund_type": "full"}'::jsonb,
    'Stripe',
    '{"refund_id": "re_3N5678901234567890", "charge_id": "ch_3N5678901234567890", "status": "succeeded", "reason": "requested_by_customer"}'::jsonb,
    CURRENT_TIMESTAMP - INTERVAL '2 days',
    NULL,
    NULL,
    '{"refund_reason": "business_closure", "processed_by": "admin", "original_transaction": "TXN-2025-000005"}'::jsonb,
    0,
    3,
    CURRENT_TIMESTAMP - INTERVAL '2 days',
    CURRENT_TIMESTAMP - INTERVAL '2 days',
    1
);

-- Partial Refund Transaction for PR-2025-000007
INSERT INTO payment_transaction (
    payment_transaction_id, transaction_code, external_transaction_id,
    payment_request_id, transaction_type, transaction_status,
    amount, currency, payment_method, payment_method_details,
    gateway_name, gateway_response, processed_at,
    error_code, error_message, metadata, retry_count, max_retries,
    created_at, updated_at, created_by
) VALUES (
    gen_random_uuid(),
    'TXN-RFD-2025-000002',
    'stripe_re_3N6789012345678901',
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000007'),
    'REFUND'::payment_transaction_type,
    'SUCCESS'::payment_transaction_status,
    250.00, -- Partial refund amount (positive)
    'USD',
    'CREDIT_CARD'::payment_method_type,
    '{"card_brand": "visa", "last_four": "1234", "refund_type": "partial"}'::jsonb,
    'Stripe',
    '{"refund_id": "re_3N6789012345678901", "charge_id": "ch_3N6789012345678901", "status": "succeeded", "reason": "requested_by_customer"}'::jsonb,
    CURRENT_TIMESTAMP - INTERVAL '3 days',
    NULL,
    NULL,
    '{"refund_reason": "workshop_cancelled", "workshop_id": "WS-002", "processed_by": "admin", "original_transaction": "TXN-2025-000006"}'::jsonb,
    0,
    3,
    CURRENT_TIMESTAMP - INTERVAL '3 days',
    CURRENT_TIMESTAMP - INTERVAL '3 days',
    1
);

-- Void Transaction for PR-2025-000008
INSERT INTO payment_transaction (
    payment_transaction_id, transaction_code, external_transaction_id,
    payment_request_id, transaction_type, transaction_status,
    amount, currency, payment_method, payment_method_details,
    gateway_name, gateway_response, processed_at,
    error_code, error_message, metadata, retry_count, max_retries,
    created_at, updated_at, created_by
) VALUES (
    gen_random_uuid(),
    'TXN-VOID-2025-000001',
    'paypal_void_DEF456GHI789',
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000008'),
    'VOID'::payment_transaction_type,
    'SUCCESS'::payment_transaction_status,
    2800.00, -- Positive amount for void
    'USD',
    'DIGITAL_WALLET'::payment_method_type,
    '{"wallet_type": "paypal", "void_type": "duplicate_payment"}'::jsonb,
    'PayPal',
    '{"void_id": "VOID-DEF456GHI789", "payment_id": "PAYID-DEF456GHI789", "status": "VOIDED", "reason": "duplicate"}'::jsonb,
    CURRENT_TIMESTAMP - INTERVAL '1 day',
    NULL,
    NULL,
    '{"void_reason": "duplicate_processing", "duplicate_of": "PR-2025-000009", "processed_by": "system", "original_transaction": "TXN-2025-000007"}'::jsonb,
    0,
    3,
    CURRENT_TIMESTAMP - INTERVAL '1 day',
    CURRENT_TIMESTAMP - INTERVAL '1 day',
    1
);

-- =====================================================
-- PAYMENT REFUND RECORDS
-- =====================================================

-- Full Refund Record for PR-2025-000006
INSERT INTO payment_refund (
    payment_refund_id, refund_code, payment_transaction_id, refund_amount, refund_reason, currency,
    refund_status, external_refund_id, gateway_name, gateway_response,
    processed_at, error_code, error_message, metadata,
    created_at, updated_at, created_by
) VALUES (
    uuid_generate_v4(),
    'RFD-2025-000001',
    (SELECT payment_transaction_id FROM payment_transaction WHERE transaction_code = 'TXN-2025-000005'),
    1200.00,
    'Customer requested full refund due to business closure and inability to use software license',
    'USD',
    'SUCCESS'::payment_transaction_status,
    'stripe_re_3N5678901234567890',
    'Stripe',
    '{"refund_id": "re_3N5678901234567890", "charge_id": "ch_3N5678901234567890", "amount": 120000, "currency": "usd", "status": "succeeded", "reason": "requested_by_customer"}'::jsonb,
    CURRENT_TIMESTAMP - INTERVAL '2 days',
    NULL,
    NULL,
    '{"refund_type": "full", "business_reason": "business_closure", "customer_satisfaction": "high", "processing_time_hours": 2}'::jsonb,
    CURRENT_TIMESTAMP - INTERVAL '2 days',
    CURRENT_TIMESTAMP - INTERVAL '2 days',
    1
);

-- Partial Refund Record for PR-2025-000007
INSERT INTO payment_refund (
    payment_refund_id, refund_code, payment_transaction_id, refund_amount, refund_reason, currency,
    refund_status, external_refund_id, gateway_name, gateway_response,
    processed_at, error_code, error_message, metadata,
    created_at, updated_at, created_by
) VALUES (
    uuid_generate_v4(),
    'RFD-2025-000002',
    (SELECT payment_transaction_id FROM payment_transaction WHERE transaction_code = 'TXN-2025-000006'),
    250.00,
    'Partial refund for cancelled workshop WS-002 (Advanced Machine Learning) due to insufficient registrations',
    'USD',
    'SUCCESS'::payment_transaction_status,
    'stripe_re_3N6789012345678901',
    'Stripe',
    '{"refund_id": "re_3N6789012345678901", "charge_id": "ch_3N6789012345678901", "amount": 25000, "currency": "usd", "status": "succeeded", "reason": "requested_by_customer"}'::jsonb,
    CURRENT_TIMESTAMP - INTERVAL '3 days',
    NULL,
    NULL,
    '{"refund_type": "partial", "workshop_cancelled": "WS-002", "remaining_amount": 600.00, "customer_satisfaction": "high", "processing_time_hours": 1}'::jsonb,
    CURRENT_TIMESTAMP - INTERVAL '3 days',
    CURRENT_TIMESTAMP - INTERVAL '3 days',
    1
);

-- =====================================================
-- COMPREHENSIVE AUDIT LOGS FOR REFUND/VOID SCENARIOS
-- =====================================================

-- Audit logs for Full Refund Payment Request (PR-2025-000006)
INSERT INTO payment_audit_log (
    payment_audit_log_id, payment_request_id, payment_transaction_id, payment_refund_id,
    action, entity_type, old_status, new_status, changes,
    reason, ip_address, user_agent, created_at, created_by
) VALUES 
-- Payment Request Creation
(
    uuid_generate_v4(),
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000006'),
    NULL, NULL, 'CREATE', 'PAYMENT_REQUEST', NULL, 'DRAFT',
    '{"title": "Software License Annual Subscription", "amount": 1200.00, "currency": "USD", "payer_name": "Jennifer Martinez"}'::jsonb,
    'Payment request created for software license subscription',
    '192.168.1.10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
    CURRENT_TIMESTAMP - INTERVAL '20 days', 1
),
-- Payment Request Sent
(
    uuid_generate_v4(),
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000006'),
    NULL, NULL, 'UPDATE', 'PAYMENT_REQUEST', 'DRAFT', 'PENDING',
    '{"status": "PENDING", "updated_reason": "Payment request sent to customer"}'::jsonb,
    'Payment request sent to customer for payment',
    '192.168.1.10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
    CURRENT_TIMESTAMP - INTERVAL '18 days', 1
),
-- Successful Payment Transaction
(
    uuid_generate_v4(),
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000006'),
    (SELECT payment_transaction_id FROM payment_transaction WHERE transaction_code = 'TXN-2025-000005'),
    NULL, 'CREATE', 'TRANSACTION', NULL, 'SUCCESS',
    '{"transaction_code": "TXN-2025-000005", "amount": 1200.00, "payment_method": "CREDIT_CARD"}'::jsonb,
    'Customer successfully paid via credit card',
    '198.51.100.10', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36',
    CURRENT_TIMESTAMP - INTERVAL '15 days', 1
),
-- Payment Request Completed
(
    uuid_generate_v4(),
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000006'),
    NULL, NULL, 'UPDATE', 'PAYMENT_REQUEST', 'PENDING', 'COMPLETED',
    jsonb_build_object('status', 'COMPLETED', 'paid_at', (CURRENT_TIMESTAMP - INTERVAL '15 days')::text),
    'Payment request completed successfully',
    '192.168.1.10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
    CURRENT_TIMESTAMP - INTERVAL '15 days', 1
),
-- Refund Request Initiated
(
    uuid_generate_v4(),
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000006'),
    NULL, NULL, 'UPDATE', 'PAYMENT_REQUEST', 'COMPLETED', 'REFUNDED',
    '{"status": "REFUNDED", "refund_reason": "business_closure"}'::jsonb,
    'Customer requested full refund due to business closure',
    '192.168.1.10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
    CURRENT_TIMESTAMP - INTERVAL '2 days', 1
),
-- Refund Transaction Created
(
    uuid_generate_v4(),
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000006'),
    (SELECT payment_transaction_id FROM payment_transaction WHERE transaction_code = 'TXN-RFD-2025-000001'),
    (SELECT payment_refund_id FROM payment_refund WHERE refund_code = 'RFD-2025-000001'),
    'CREATE', 'REFUND', NULL, 'SUCCESS',
    '{"refund_code": "RFD-2025-000001", "refund_amount": 1200.00, "refund_type": "full"}'::jsonb,
    'Full refund processed successfully',
    '192.168.1.10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
    CURRENT_TIMESTAMP - INTERVAL '2 days', 1
);

-- Audit logs for Partial Refund Payment Request (PR-2025-000007)
INSERT INTO payment_audit_log (
    payment_audit_log_id, payment_request_id, payment_transaction_id, payment_refund_id,
    action, entity_type, old_status, new_status, changes,
    reason, ip_address, user_agent, created_at, created_by
) VALUES 
-- Payment Request Creation
(
    uuid_generate_v4(),
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000007'),
    NULL, NULL, 'CREATE', 'PAYMENT_REQUEST', NULL, 'DRAFT',
    '{"title": "Conference Registration and Workshop Bundle", "amount": 850.00, "currency": "USD", "payer_name": "David Chen"}'::jsonb,
    'Payment request created for conference registration',
    '192.168.1.10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
    CURRENT_TIMESTAMP - INTERVAL '18 days', 1
),
-- Payment Request Sent
(
    uuid_generate_v4(),
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000007'),
    NULL, NULL, 'UPDATE', 'PAYMENT_REQUEST', 'DRAFT', 'PENDING',
    '{"status": "PENDING", "updated_reason": "Payment request sent to customer"}'::jsonb,
    'Payment request sent to customer for payment',
    '192.168.1.10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
    CURRENT_TIMESTAMP - INTERVAL '15 days', 1
),
-- Successful Payment Transaction
(
    uuid_generate_v4(),
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000007'),
    (SELECT payment_transaction_id FROM payment_transaction WHERE transaction_code = 'TXN-2025-000006'),
    NULL, 'CREATE', 'TRANSACTION', NULL, 'SUCCESS',
    '{"transaction_code": "TXN-2025-000006", "amount": 850.00, "payment_method": "CREDIT_CARD"}'::jsonb,
    'Customer successfully paid via credit card',
    '203.0.113.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
    CURRENT_TIMESTAMP - INTERVAL '12 days', 1
),
-- Payment Request Completed
(
    uuid_generate_v4(),
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000007'),
    NULL, NULL, 'UPDATE', 'PAYMENT_REQUEST', 'PENDING', 'COMPLETED',
    jsonb_build_object('status', 'COMPLETED', 'paid_at', (CURRENT_TIMESTAMP - INTERVAL '12 days')::text),
    'Payment request completed successfully',
    '192.168.1.10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
    CURRENT_TIMESTAMP - INTERVAL '12 days', 1
),
-- Partial Refund Request
(
    uuid_generate_v4(),
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000007'),
    NULL, NULL, 'UPDATE', 'PAYMENT_REQUEST', 'COMPLETED', 'PARTIAL_REFUND',
    '{"status": "PARTIAL_REFUND", "refund_reason": "workshop_cancelled"}'::jsonb,
    'Customer requested partial refund for cancelled workshop',
    '192.168.1.10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
    CURRENT_TIMESTAMP - INTERVAL '3 days', 1
),
-- Partial Refund Transaction Created
(
    uuid_generate_v4(),
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000007'),
    (SELECT payment_transaction_id FROM payment_transaction WHERE transaction_code = 'TXN-RFD-2025-000002'),
    (SELECT payment_refund_id FROM payment_refund WHERE refund_code = 'RFD-2025-000002'),
    'CREATE', 'REFUND', NULL, 'SUCCESS',
    '{"refund_code": "RFD-2025-000002", "refund_amount": 250.00, "refund_type": "partial"}'::jsonb,
    'Partial refund processed successfully for cancelled workshop',
    '192.168.1.10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
    CURRENT_TIMESTAMP - INTERVAL '3 days', 1
);

-- Audit logs for Void Payment Request (PR-2025-000008)
INSERT INTO payment_audit_log (
    payment_audit_log_id, payment_request_id, payment_transaction_id, payment_refund_id,
    action, entity_type, old_status, new_status, changes,
    reason, ip_address, user_agent, created_at, created_by
) VALUES 
-- Payment Request Creation
(
    uuid_generate_v4(),
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000008'),
    NULL, NULL, 'CREATE', 'PAYMENT_REQUEST', NULL, 'DRAFT',
    '{"title": "Duplicate Payment - Marketing Campaign Setup", "amount": 2800.00, "currency": "USD", "payer_name": "Lisa Thompson"}'::jsonb,
    'Payment request created for marketing campaign setup',
    '192.168.1.10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
    CURRENT_TIMESTAMP - INTERVAL '10 days', 1
),
-- Payment Request Sent
(
    uuid_generate_v4(),
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000008'),
    NULL, NULL, 'UPDATE', 'PAYMENT_REQUEST', 'DRAFT', 'PENDING',
    '{"status": "PENDING", "updated_reason": "Payment request sent to customer"}'::jsonb,
    'Payment request sent to customer for payment',
    '192.168.1.10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
    CURRENT_TIMESTAMP - INTERVAL '9 days', 1
),
-- Successful Payment Transaction
(
    uuid_generate_v4(),
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000008'),
    (SELECT payment_transaction_id FROM payment_transaction WHERE transaction_code = 'TXN-2025-000007'),
    NULL, 'CREATE', 'TRANSACTION', NULL, 'SUCCESS',
    '{"transaction_code": "TXN-2025-000007", "amount": 2800.00, "payment_method": "DIGITAL_WALLET"}'::jsonb,
    'Customer successfully paid via PayPal',
    '192.0.2.50', 'Mozilla/5.0 (iPhone; CPU iPhone OS 17_1 like Mac OS X) AppleWebKit/605.1.15',
    CURRENT_TIMESTAMP - INTERVAL '8 days', 1
),
-- Payment Request Completed
(
    uuid_generate_v4(),
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000008'),
    NULL, NULL, 'UPDATE', 'PAYMENT_REQUEST', 'PENDING', 'COMPLETED',
    jsonb_build_object('status', 'COMPLETED', 'paid_at', (CURRENT_TIMESTAMP - INTERVAL '8 days')::text),
    'Payment request completed successfully',
    '192.168.1.10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
    CURRENT_TIMESTAMP - INTERVAL '8 days', 1
),
-- Void Request Initiated
(
    uuid_generate_v4(),
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000008'),
    NULL, NULL, 'UPDATE', 'PAYMENT_REQUEST', 'COMPLETED', 'VOIDED',
    '{"status": "VOIDED", "void_reason": "duplicate_processing"}'::jsonb,
    'Payment voided due to duplicate processing detected',
    '192.168.1.10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
    CURRENT_TIMESTAMP - INTERVAL '1 day', 1
),
-- Void Transaction Created
(
    uuid_generate_v4(),
    (SELECT payment_request_id FROM payment_request WHERE request_code = 'PR-2025-000008'),
    (SELECT payment_transaction_id FROM payment_transaction WHERE transaction_code = 'TXN-VOID-2025-000001'),
    NULL, 'CREATE', 'TRANSACTION', NULL, 'SUCCESS',
    '{"transaction_code": "TXN-VOID-2025-000001", "amount": 2800.00, "transaction_type": "VOID"}'::jsonb,
    'Void transaction processed successfully',
    '192.168.1.10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
    CURRENT_TIMESTAMP - INTERVAL '1 day', 1
);

-- =====================================================
-- SUMMARY
-- =====================================================
-- This migration adds:
-- - 3 new payment requests with REFUNDED, PARTIAL_REFUND, and VOIDED statuses
-- - 6 payment transactions (3 successful payments + 2 refunds + 1 void)
-- - 2 payment refund records (full and partial)
-- - 18 comprehensive audit log entries tracking complete lifecycles
-- - Realistic scenarios demonstrating successful payment transaction outcomes
-- - Proper foreign key relationships and realistic timestamps