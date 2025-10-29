-- =====================================================
-- Payment Request Seed Data with UUID
-- Version: V19
-- Description: Seed payment_request table with UUID IDs and various statuses for testing
-- Note: This script requires V18 migration (UUID function) to be completed first
-- =====================================================

-- DRAFT Status Payment Request
INSERT INTO payment_request (
    payment_request_id, request_code, payment_token, title, description, amount, currency,
    payer_name, payer_email, payer_phone, allowed_payment_methods, pre_selected_payment_method,
    status, expires_at, paid_at, tenant_id, metadata, created_at, updated_at, created_by, updated_by
) VALUES (
    gen_random_uuid(),
    'PR-2025-000001',
    'draft_token_001_' || substr(md5(random()::text), 1, 16),
    'Website Development Invoice',
    'Payment for website development services - Phase 1',
    2500.00,
    'USD',
    'John Smith',
    'john.smith@example.com',
    '+1-555-0101',
    ARRAY['CREDIT_CARD', 'DEBIT_CARD', 'BANK_TRANSFER']::payment_method_type[],
    'CREDIT_CARD'::payment_method_type,
    'DRAFT'::payment_request_status,
    CURRENT_TIMESTAMP + INTERVAL '30 days',
    NULL,
    1,
    '{"project_id": "WEB-001", "invoice_number": "INV-2025-001"}'::jsonb,
    CURRENT_TIMESTAMP - INTERVAL '2 days',
    CURRENT_TIMESTAMP - INTERVAL '2 days',
    1,
    1
);

-- PENDING Status Payment Request
INSERT INTO payment_request (
    payment_request_id, request_code, payment_token, title, description, amount, currency,
    payer_name, payer_email, payer_phone, allowed_payment_methods, pre_selected_payment_method,
    status, expires_at, paid_at, tenant_id, metadata, created_at, updated_at, created_by, updated_by
) VALUES (
    gen_random_uuid(),
    'PR-2025-000002',
    'pending_token_002_' || substr(md5(random()::text), 1, 16),
    'Monthly Subscription Fee',
    'Premium subscription payment for January 2025',
    99.99,
    'USD',
    'Sarah Johnson',
    'sarah.johnson@example.com',
    '+1-555-0102',
    ARRAY['CREDIT_CARD', 'DEBIT_CARD', 'DIGITAL_WALLET']::payment_method_type[],
    'DIGITAL_WALLET'::payment_method_type,
    'PENDING'::payment_request_status,
    CURRENT_TIMESTAMP + INTERVAL '7 days',
    NULL,
    1,
    '{"subscription_type": "premium", "billing_cycle": "monthly"}'::jsonb,
    CURRENT_TIMESTAMP - INTERVAL '1 day',
    CURRENT_TIMESTAMP - INTERVAL '1 day',
    1,
    1
);

-- COMPLETED Status Payment Request
INSERT INTO payment_request (
    payment_request_id, request_code, payment_token, title, description, amount, currency,
    payer_name, payer_email, payer_phone, allowed_payment_methods, pre_selected_payment_method,
    status, expires_at, paid_at, tenant_id, metadata, created_at, updated_at, created_by, updated_by
) VALUES (
    gen_random_uuid(),
    'PR-2025-000003',
    'completed_token_003_' || substr(md5(random()::text), 1, 16),
    'E-commerce Platform Setup',
    'Complete e-commerce platform development and deployment',
    5000.00,
    'USD',
    'Michael Brown',
    'michael.brown@ecommerce.com',
    '+1-555-0103',
    ARRAY['CREDIT_CARD', 'BANK_TRANSFER']::payment_method_type[],
    'CREDIT_CARD'::payment_method_type,
    'COMPLETED'::payment_request_status,
    CURRENT_TIMESTAMP + INTERVAL '45 days',
    CURRENT_TIMESTAMP - INTERVAL '5 days',
    1,
    '{"platform": "shopify", "features": ["inventory", "payments", "analytics"]}'::jsonb,
    CURRENT_TIMESTAMP - INTERVAL '10 days',
    CURRENT_TIMESTAMP - INTERVAL '5 days',
    1,
    1
);

-- CANCELLED Status Payment Request
INSERT INTO payment_request (
    payment_request_id, request_code, payment_token, title, description, amount, currency,
    payer_name, payer_email, payer_phone, allowed_payment_methods, pre_selected_payment_method,
    status, expires_at, paid_at, tenant_id, metadata, created_at, updated_at, created_by, updated_by
) VALUES (
    gen_random_uuid(),
    'PR-2025-000004',
    'cancelled_token_004_' || substr(md5(random()::text), 1, 16),
    'Mobile App Development',
    'iOS and Android mobile application development',
    7500.00,
    'USD',
    'Emily Davis',
    'emily.davis@mobiletech.com',
    '+1-555-0104',
    ARRAY['CREDIT_CARD', 'DEBIT_CARD', 'BANK_TRANSFER']::payment_method_type[],
    'BANK_TRANSFER'::payment_method_type,
    'CANCELLED'::payment_request_status,
    CURRENT_TIMESTAMP + INTERVAL '60 days',
    NULL,
    1,
    '{"app_type": "native", "platforms": ["ios", "android"], "milestone": "cancelled"}'::jsonb,
    CURRENT_TIMESTAMP - INTERVAL '8 days',
    CURRENT_TIMESTAMP - INTERVAL '3 days',
    1,
    1
);

-- FAILED Status Payment Request
INSERT INTO payment_request (
    payment_request_id, request_code, payment_token, title, description, amount, currency,
    payer_name, payer_email, payer_phone, allowed_payment_methods, pre_selected_payment_method,
    status, expires_at, paid_at, tenant_id, metadata, created_at, updated_at, created_by, updated_by
) VALUES (
    gen_random_uuid(),
    'PR-2025-000005',
    'failed_token_005_' || substr(md5(random()::text), 1, 16),
    'Database Migration Service',
    'Legacy database migration to modern cloud infrastructure',
    3200.00,
    'USD',
    'Robert Wilson',
    'robert.wilson@datatech.com',
    '+1-555-0105',
    ARRAY['CREDIT_CARD', 'BANK_TRANSFER']::payment_method_type[],
    'BANK_TRANSFER'::payment_method_type,
    'FAILED'::payment_request_status,
    CURRENT_TIMESTAMP + INTERVAL '30 days',
    NULL,
    1,
    '{"migration_type": "cloud", "database": "postgresql", "size": "large"}'::jsonb,
    CURRENT_TIMESTAMP - INTERVAL '20 days',
    CURRENT_TIMESTAMP - INTERVAL '5 days',
    1,
    1
);