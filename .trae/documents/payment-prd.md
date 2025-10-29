# Payment Service PRD (Product Requirements Document)

## Document Information

- **Version**: 1.2
- **Last Updated**: 2025-01-29
- **Target Implementation**: Q1 2025
- **Document Type**: Product Requirements Document for AI Coder
- **Implementation Status**: Payment Transaction Retry System Completed

---

## Table of Contents

1. [Overview](#overview)
2. [Implementation Status](#implementation-status)
3. [Technical Context](#technical-context)
4. [Database Schema Design](#database-schema-design)
5. [Backend API Specifications](#backend-api-specifications)
6. [Frontend Requirements](#frontend-requirements)
7. [Frontend-Backend Synchronization](#frontend-backend-synchronization)
8. [Payment Workflows](#payment-workflows)
9. [Security & Compliance](#security--compliance)
10. [Integration Requirements](#integration-requirements)
11. [Testing Requirements](#testing-requirements)
12. [Troubleshooting](#troubleshooting)
13. [Success Criteria](#success-criteria)

---

## Overview

### Purpose

Implement a comprehensive payment management system within the existing AHSS Shared Services application, enabling users to create payment requests, generate payment links, process payments through multiple payment methods, and manage the complete payment lifecycle.

### Business Objectives

- Enable secure payment request creation with shareable payment links
- Support multiple payment methods (credit card, bank transfer, digital wallets)
- Provide complete payment lifecycle management (verify, void, refund, cancel)
- Maintain audit trail for all payment operations
- Support multi-tenant payment isolation and reporting

### Scope

#### In Scope

- Payment request creation and management
- Payment link generation with secure tokens
- Multiple payment method support
- Payment verification and status management
- Void and refund capabilities
- Payment cancellation workflow
- Multi-tenant payment isolation
- Audit logging for all payment operations
- Permission-based access control for payment operations

#### Out of Scope

- Direct payment gateway integration (Phase 2)
- Recurring payment/subscription management (Phase 2)
- Payment analytics and reporting dashboard (Phase 2)
- Multi-currency support (Phase 2)
- Payment dispute management (Phase 2)

---

## Implementation Status

### Current Implementation Phase

**Status**: Payment Transaction Retry System Completed ✅  
**Date Completed**: January 29, 2025  
**Phase**: Enhanced Payment Transaction Management

### Completed Work

#### 1. PaymentRequest Interface Synchronization
- ✅ **Frontend PaymentRequest Interface Updated**: Aligned with backend `PaymentRequestDto.java`
- ✅ **Field Mapping Corrections**: Updated field names and types to match backend structure
- ✅ **Component Updates**: Modified `PaymentRequestList.tsx` and `PaymentRequestCreate.tsx`
- ✅ **Type Safety**: Ensured TypeScript compatibility across all payment components

#### 2. PaymentTransaction Interface Synchronization
- ✅ **Frontend PaymentTransaction Interface Updated**: Aligned with backend `PaymentTransactionDto.java`
- ✅ **Field Renaming**: Updated `status` to `transactionStatus` across all components
- ✅ **New Fields Added**: Added `externalTransactionId` and `paymentMethodDetails`
- ✅ **Component Updates**: Modified `PaymentTransactionList.tsx` and `PaymentTransactionTable.tsx`

#### 3. Payment Transaction Retry System Implementation
- ✅ **Database Migration**: Added `V22__add_retry_fields_to_payment_transaction.sql`
- ✅ **Backend Entity**: Enhanced `PaymentTransaction.java` with retry fields and logic
- ✅ **Backend DTO**: Updated `PaymentTransactionDto.java` with retry fields
- ✅ **Service Layer**: Enhanced retry logic in `PaymentTransactionServiceImpl.java`
- ✅ **Frontend Interface**: Added retry fields to TypeScript interface
- ✅ **Full Stack Testing**: Verified compilation and functionality across all layers

#### 4. Payment Method Type Alignment
- ✅ **PAYMENT_METHOD_TYPE_MAPPINGS**: Synchronized frontend mappings with backend enum
- ✅ **Enum Consistency**: Added `PAYPAL`, `STRIPE`, `MANUAL` and removed `CASH`, `CHECK`
- ✅ **Component Integration**: Updated all components using payment method types

#### 5. Data Structure Changes

##### PaymentRequest Fields
| Field | Frontend (Old) | Frontend (New) | Backend | Status |
|-------|---------------|----------------|---------|---------|
| Description | `description` | `title` | `title` | ✅ Aligned |
| Requestor Name | `requestorName` | `payerName` | `payerName` | ✅ Aligned |
| Requestor Email | `requestorEmail` | `payerEmail` | `payerEmail` | ✅ Aligned |
| Payment Method | `paymentMethod` | `preSelectedPaymentMethod` | `preSelectedPaymentMethod` | ✅ Aligned |
| Due Date | `dueDate` | `expiresAt` | `expiresAt` | ✅ Aligned |
| - | - | `allowedPaymentMethods` | `allowedPaymentMethods` | ✅ Added |
| - | - | `payerPhone` | `payerPhone` | ✅ Added |
| - | - | `paymentToken` | `paymentToken` | ✅ Added |

##### PaymentTransaction Fields
| Field | Frontend (Old) | Frontend (New) | Backend | Status |
|-------|---------------|----------------|---------|---------|
| Status | `status` | `transactionStatus` | `transactionStatus` | ✅ Aligned |
| External ID | - | `externalTransactionId` | `externalTransactionId` | ✅ Added |
| Payment Details | - | `paymentMethodDetails` | `paymentMethodDetails` | ✅ Added |
| Retry Count | `retryCount` (frontend-only) | `retryCount` | `retryCount` | ✅ Aligned |
| Max Retries | `maxRetries` (frontend-only) | `maxRetries` | `maxRetries` | ✅ Aligned |

### Next Implementation Phases

#### Phase 2: Payment Gateway Integration
- 📋 **Pending**: Stripe payment gateway integration
- 📋 **Pending**: PayPal payment gateway integration
- 📋 **Pending**: Payment processing workflow implementation
- 📋 **Pending**: Webhook handling for payment status updates

#### Phase 3: Advanced Payment Features
- 📋 **Pending**: Payment link generation and sharing
- 📋 **Pending**: Payment verification workflow
- 📋 **Pending**: Void and refund processing
- 📋 **Pending**: Payment cancellation workflow

#### Phase 4: Frontend Integration & UI
- 📋 **Pending**: Payment form components with validation
- 📋 **Pending**: Payment status display and tracking
- 📋 **Pending**: Payment history and transaction details
- 📋 **Pending**: Payment retry functionality UI

### Recent Implementation Details

#### Retry System Architecture

The payment transaction retry system has been implemented with the following components:

1. **Database Schema Enhancement**
   - Added `retry_count` column (INTEGER, default 0)
   - Added `max_retries` column (INTEGER, default 3)
   - Added constraints to ensure `retry_count <= max_retries`
   - Added indexes for performance optimization

2. **Backend Entity Logic**
   - Enhanced `PaymentTransaction.java` with retry fields
   - Added `canBeRetried()` method for retry validation
   - Added `incrementRetryCount()` method for retry tracking
   - Updated retry logic in service layer

3. **Frontend Interface Alignment**
   - Moved retry fields from frontend-specific to main interface
   - Updated all payment transaction components
   - Ensured type safety across TypeScript interfaces

4. **Service Layer Enhancements**
   - Updated `convertToDto()` method to include retry fields
   - Enhanced `retryTransaction()` method with proper validation
   - Added retry limit checking and error handling

---

## Technical Context

### Existing Technology Stack

#### Backend

- **Framework**: Spring Boot 3.3.4 with Java 21
- **Database**: PostgreSQL with Flyway migrations
- **Security**: Spring Security + JWT
- **Build Tool**: Gradle
- **Architecture**: Controller-Service-Repository-Model layers

#### Frontend

- **Framework**: React 19.1.1 with TypeScript
- **Build Tool**: Vite 7.1.7
- **UI Library**: Shadcn/UI with TailwindCSS 4.1.14
- **State Management**: Zustand 5.0.8
- **HTTP Client**: Axios 1.12.2
- **Form Handling**: React Hook Form 7.64.0 + Zod 4.1.11

### Integration Points

- Existing authentication and authorization system
- Multi-tenant architecture
- Permission system (RBAC/ABAC)
- Audit logging framework
- Email notification system (for payment confirmations)

---

## Database Schema Design

### Migration File: `V15__create_payment_tables.sql`

```sql
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
    payment_request_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

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
    FOREIGN KEY (created_by) REFERENCES "user"(user_id) ON DELETE SET NULL,
    FOREIGN KEY (updated_by) REFERENCES "user"(user_id) ON DELETE SET NULL,

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
    payment_request_id UUID NOT NULL,
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
    FOREIGN KEY (created_by) REFERENCES "user"(user_id) ON DELETE SET NULL
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
    FOREIGN KEY (created_by) REFERENCES "user"(user_id) ON DELETE SET NULL
);

-- Payment Audit Log Table
-- Comprehensive audit trail for all payment operations
CREATE TABLE payment_audit_log (
    payment_audit_log_id BIGSERIAL PRIMARY KEY,

    -- Reference
    payment_request_id UUID,
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
    FOREIGN KEY (created_by) REFERENCES "user"(user_id) ON DELETE SET NULL
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
```

### Migration File: `V16__add_payment_permissions.sql`

```sql
-- =====================================================
-- Payment Permissions
-- Version: V16
-- Description: Add permissions for payment management
-- =====================================================

-- Insert payment management permissions
INSERT INTO permission (name, description, resource_type, action, created_at, updated_at)
VALUES
    -- Payment Request Permissions
    ('PAYMENT_MGMT:read', 'View payment requests and transactions', 'PAYMENT_MGMT', 'read', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('PAYMENT_MGMT:create', 'Create new payment requests', 'PAYMENT_MGMT', 'create', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('PAYMENT_MGMT:update', 'Update payment requests', 'PAYMENT_MGMT', 'update', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('PAYMENT_MGMT:delete', 'Delete payment requests', 'PAYMENT_MGMT', 'delete', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('PAYMENT_MGMT:verify', 'Verify payment requests', 'PAYMENT_MGMT', 'verify', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('PAYMENT_MGMT:void', 'Void completed payments', 'PAYMENT_MGMT', 'void', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('PAYMENT_MGMT:refund', 'Process payment refunds', 'PAYMENT_MGMT', 'refund', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('PAYMENT_MGMT:cancel', 'Cancel pending payment requests', 'PAYMENT_MGMT', 'cancel', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('PAYMENT_MGMT:admin', 'Full administrative access to payment management', 'PAYMENT_MGMT', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Assign permissions to Super Administrator role
INSERT INTO role_permission (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM role r
CROSS JOIN permission p
WHERE r.name = 'Super Administrator'
AND p.resource_type = 'PAYMENT_MGMT'
ON CONFLICT DO NOTHING;

-- Assign basic permissions to System Administrator role
INSERT INTO role_permission (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM role r
CROSS JOIN permission p
WHERE r.name = 'System Administrator'
AND p.resource_type = 'PAYMENT_MGMT'
AND p.action IN ('read', 'create', 'update', 'verify', 'cancel')
ON CONFLICT DO NOTHING;

COMMENT ON COLUMN permission.resource_type IS 'Resource type for permission (e.g., PAYMENT_MGMT)';
COMMENT ON COLUMN permission.action IS 'Action allowed by permission (e.g., read, create, verify, void, refund)';
```

### Migration File: `V22__add_payment_transaction_retry_fields.sql`

```sql
-- =====================================================
-- Payment Transaction Retry Fields
-- Version: V22
-- Description: Add retry functionality to payment transactions
-- =====================================================

-- Add retry count field to track number of retry attempts
ALTER TABLE payment_transaction 
ADD COLUMN retry_count INTEGER NOT NULL DEFAULT 0;

-- Add max retries field to define retry limit per transaction
ALTER TABLE payment_transaction 
ADD COLUMN max_retries INTEGER NOT NULL DEFAULT 3;

-- Add constraint to ensure retry_count doesn't exceed max_retries
ALTER TABLE payment_transaction 
ADD CONSTRAINT chk_retry_count_within_limit 
CHECK (retry_count <= max_retries);

-- Add constraint to ensure retry_count is non-negative
ALTER TABLE payment_transaction 
ADD CONSTRAINT chk_retry_count_non_negative 
CHECK (retry_count >= 0);

-- Add constraint to ensure max_retries is positive
ALTER TABLE payment_transaction 
ADD CONSTRAINT chk_max_retries_positive 
CHECK (max_retries > 0);

-- Add index for efficient retry queries
CREATE INDEX idx_payment_transaction_retry_status 
ON payment_transaction (status, retry_count, max_retries);

-- Add comments for documentation
COMMENT ON COLUMN payment_transaction.retry_count IS 'Number of retry attempts made for this transaction';
COMMENT ON COLUMN payment_transaction.max_retries IS 'Maximum number of retry attempts allowed for this transaction';
```

---

## Backend API Specifications

### Package Structure

```
com.ahss/
├── controller/
│   └── PaymentController.java
├── service/
│   ├── PaymentRequestService.java
│   └── PaymentTransactionService.java
├── service/impl/
│   ├── PaymentRequestServiceImpl.java
│   └── PaymentTransactionServiceImpl.java
├── repository/
│   ├── PaymentRequestRepository.java
│   ├── PaymentTransactionRepository.java
│   ├── PaymentRefundRepository.java
│   └── PaymentAuditLogRepository.java
├── entity/
│   ├── PaymentRequest.java
│   ├── PaymentTransaction.java
│   ├── PaymentRefund.java
│   └── PaymentAuditLog.java
├── dto/
│   ├── PaymentRequestDto.java
│   ├── PaymentTransactionDto.java
│   ├── PaymentRefundDto.java
│   ├── CreatePaymentRequestDto.java
│   ├── UpdatePaymentRequestDto.java
│   ├── ProcessPaymentDto.java
│   └── RefundRequestDto.java
└── enums/
    ├── PaymentRequestStatus.java
    ├── PaymentMethodType.java
    ├── PaymentTransactionType.java
    └── PaymentTransactionStatus.java
```

### Entity Classes

#### PaymentRequest.java

```java
package com.ahss.entity;

import com.ahss.enums.PaymentMethodType;
import com.ahss.enums.PaymentRequestStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import io.hypersistence.utils.hibernate.type.array.EnumArrayType;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "payment_request")
@Data
public class PaymentRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.OTHER)
    @Column(name = "payment_request_id")
    private UUID id;

    @Column(name = "request_code", unique = true, nullable = false, length = 50)
    private String requestCode;

    @Column(name = "payment_token", unique = true, nullable = false)
    private String paymentToken;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency = "USD";

    @Column(name = "payer_name")
    private String payerName;

    @Column(name = "payer_email")
    private String payerEmail;

    @Column(name = "payer_phone", length = 50)
    private String payerPhone;

    @Type(EnumArrayType.class)
    @Column(name = "allowed_payment_methods", columnDefinition = "payment_method_type[]", nullable = false)
    private PaymentMethodType[] allowedPaymentMethods;

    @Enumerated(EnumType.STRING)
    @Column(name = "pre_selected_payment_method", columnDefinition = "payment_method_type")
    private PaymentMethodType preSelectedPaymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "payment_request_status")
    private PaymentRequestStatus status = PaymentRequestStatus.DRAFT;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (requestCode == null) {
            requestCode = generateRequestCode();
        }
        if (paymentToken == null) {
            paymentToken = generatePaymentToken();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    private String generateRequestCode() {
        return "PR-" + LocalDateTime.now().getYear() + "-" +
               String.format("%06d", System.currentTimeMillis() % 1000000);
    }

    private String generatePaymentToken() {
        return java.util.UUID.randomUUID().toString();
    }
}
```

#### PaymentTransaction.java

```java
package com.ahss.entity;

import com.ahss.enums.PaymentMethodType;
import com.ahss.enums.PaymentTransactionStatus;
import com.ahss.enums.PaymentTransactionType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "payment_transaction")
@Data
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_transaction_id")
    private Long id;

    @Column(name = "transaction_code", unique = true, nullable = false, length = 50)
    private String transactionCode;

    @Column(name = "external_transaction_id")
    private String externalTransactionId;

    @JdbcTypeCode(SqlTypes.OTHER)
    @Column(name = "payment_request_id", nullable = false)
    private UUID paymentRequestId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, columnDefinition = "payment_transaction_type")
    private PaymentTransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_status", nullable = false, columnDefinition = "payment_transaction_status")
    private PaymentTransactionStatus transactionStatus = PaymentTransactionStatus.PENDING;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, columnDefinition = "payment_method_type")
    private PaymentMethodType paymentMethod;

    @Type(JsonBinaryType.class)
    @Column(name = "payment_method_details", columnDefinition = "jsonb")
    private Map<String, Object> paymentMethodDetails;

    @Column(name = "gateway_name", length = 100)
    private String gatewayName;

    @Type(JsonBinaryType.class)
    @Column(name = "gateway_response", columnDefinition = "jsonb")
    private Map<String, Object> gatewayResponse;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "error_code", length = 50)
    private String errorCode;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "created_by")
    private Long createdBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (transactionCode == null) {
            transactionCode = generateTransactionCode();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    private String generateTransactionCode() {
        return "TXN-" + LocalDateTime.now().getYear() + "-" +
               String.format("%06d", System.currentTimeMillis() % 1000000);
    }
}
```

### REST API Endpoints

#### Base Path: `/api/v1/payments`

#### 1. Create Payment Request

**Endpoint**: `POST /api/v1/payments/requests`

**Permission Required**: `PAYMENT_MGMT:create`

**Request Body**:

```json
{
  "title": "Invoice Payment - INV-2025-001",
  "description": "Payment for services rendered in January 2025",
  "amount": 1500.0,
  "currency": "USD",
  "payerName": "John Doe",
  "payerEmail": "john.doe@example.com",
  "payerPhone": "+1234567890",
  "allowedPaymentMethods": ["CREDIT_CARD", "DEBIT_CARD", "PAYPAL"],
  "preSelectedPaymentMethod": "CREDIT_CARD",
  "expiresAt": "2025-12-31T23:59:59",
  "metadata": {
    "invoiceNumber": "INV-2025-001",
    "customerId": "CUST-12345"
  }
}
```

**Response**:

```json
{
  "data": {
    "id": 1,
    "requestCode": "PR-2025-001234",
    "paymentToken": "550e8400-e29b-41d4-a716-446655440000",
    "paymentLink": "https://yourapp.com/pay/550e8400-e29b-41d4-a716-446655440000",
    "title": "Invoice Payment - INV-2025-001",
    "description": "Payment for services rendered in January 2025",
    "amount": 1500.0,
    "currency": "USD",
    "payerName": "John Doe",
    "payerEmail": "john.doe@example.com",
    "payerPhone": "+1234567890",
    "allowedPaymentMethods": ["CREDIT_CARD", "DEBIT_CARD", "PAYPAL"],
    "preSelectedPaymentMethod": "CREDIT_CARD",
    "status": "PENDING",
    "expiresAt": "2025-12-31T23:59:59",
    "createdAt": "2025-10-15T10:30:00",
    "updatedAt": "2025-10-15T10:30:00"
  },
  "message": "Payment request created successfully",
  "success": true
}
```

**Status Codes**:

- `201`: Payment request created successfully
- `400`: Invalid request data
- `403`: Insufficient permissions
- `500`: Internal server error

---

#### 2. Get Payment Request by Token

**Endpoint**: `GET /api/v1/payments/requests/by-token/{paymentToken}`

**Permission Required**: None (public endpoint with token validation)

**Response**:

```json
{
  "data": {
    "id": 1,
    "requestCode": "PR-2025-001234",
    "title": "Invoice Payment - INV-2025-001",
    "description": "Payment for services rendered in January 2025",
    "amount": 1500.0,
    "currency": "USD",
    "payerName": "John Doe",
    "payerEmail": "john.doe@example.com",
    "allowedPaymentMethods": ["CREDIT_CARD", "DEBIT_CARD", "PAYPAL"],
    "preSelectedPaymentMethod": "CREDIT_CARD",
    "status": "PENDING",
    "expiresAt": "2025-12-31T23:59:59"
  },
  "message": "Payment request retrieved successfully",
  "success": true
}
```

**Status Codes**:

- `200`: Success
- `404`: Payment request not found or token invalid
- `410`: Payment request expired

---

#### 3. Get All Payment Requests (Admin)

**Endpoint**: `GET /api/v1/payments/requests`

**Permission Required**: `PAYMENT_MGMT:read`

**Query Parameters**:

- `page` (default: 0)
- `size` (default: 20)
- `status` (optional: DRAFT, PENDING, COMPLETED, etc.)
- `search` (optional: search by request code, payer name, or email)
- `sortBy` (default: createdAt)
- `sortDirection` (default: DESC)

**Response**:

```json
{
  "data": {
    "content": [
      {
        "id": 1,
        "requestCode": "PR-2025-001234",
        "title": "Invoice Payment - INV-2025-001",
        "amount": 1500.0,
        "currency": "USD",
        "payerName": "John Doe",
        "payerEmail": "john.doe@example.com",
        "status": "PENDING",
        "createdAt": "2025-10-15T10:30:00"
      }
    ],
    "totalElements": 50,
    "totalPages": 3,
    "size": 20,
    "number": 0
  },
  "message": "Payment requests retrieved successfully",
  "success": true
}
```

---

#### 4. Get Payment Request by ID

**Endpoint**: `GET /api/v1/payments/requests/{id}`

**Permission Required**: `PAYMENT_MGMT:read`

**Response**: Same as Create Payment Request response

---

#### 5. Update Payment Request

**Endpoint**: `PUT /api/v1/payments/requests/{id}`

**Permission Required**: `PAYMENT_MGMT:update`

**Restrictions**: Only DRAFT and PENDING status can be updated

**Request Body**:

```json
{
  "title": "Updated Invoice Payment",
  "description": "Updated description",
  "amount": 1600.0,
  "expiresAt": "2025-12-31T23:59:59"
}
```

**Response**: Updated payment request object

**Status Codes**:

- `200`: Updated successfully
- `400`: Invalid status for update
- `403`: Insufficient permissions
- `404`: Payment request not found

---

#### 6. Process Payment (Public Endpoint)

**Endpoint**: `POST /api/v1/payments/requests/{paymentToken}/process`

**Permission Required**: None (validated by token)

**Request Body**:

```json
{
  "paymentMethod": "CREDIT_CARD",
  "paymentMethodDetails": {
    "cardNumber": "4111111111111111",
    "expiryMonth": "12",
    "expiryYear": "2026",
    "cvv": "123",
    "cardHolderName": "John Doe"
  }
}
```

**Response**:

```json
{
  "data": {
    "transactionId": 1,
    "transactionCode": "TXN-2025-001234",
    "status": "PROCESSING",
    "message": "Payment is being processed"
  },
  "message": "Payment initiated successfully",
  "success": true
}
```

**Status Codes**:

- `200`: Payment processing initiated
- `400`: Invalid payment details
- `404`: Payment request not found
- `410`: Payment request expired
- `422`: Payment request not in valid state

---

#### 7. Verify Payment Request

**Endpoint**: `POST /api/v1/payments/requests/{id}/verify`

**Permission Required**: `PAYMENT_MGMT:verify`

**Request Body**:

```json
{
  "verificationNotes": "Payment verified through bank statement"
}
```

**Response**:

```json
{
  "data": {
    "id": 1,
    "status": "COMPLETED",
    "verifiedAt": "2025-10-15T14:30:00"
  },
  "message": "Payment request verified successfully",
  "success": true
}
```

---

#### 8. Void Payment

**Endpoint**: `POST /api/v1/payments/requests/{id}/void`

**Permission Required**: `PAYMENT_MGMT:void`

**Restrictions**: Only COMPLETED payments can be voided

**Request Body**:

```json
{
  "voidReason": "Duplicate payment processed"
}
```

**Response**:

```json
{
  "data": {
    "id": 1,
    "status": "VOIDED",
    "voidedAt": "2025-10-15T15:00:00"
  },
  "message": "Payment voided successfully",
  "success": true
}
```

---

#### 9. Refund Payment

**Endpoint**: `POST /api/v1/payments/requests/{id}/refund`

**Permission Required**: `PAYMENT_MGMT:refund`

**Restrictions**: Only COMPLETED payments can be refunded

**Request Body**:

```json
{
  "refundAmount": 1500.0,
  "refundReason": "Customer requested refund",
  "refundType": "FULL"
}
```

**Response**:

```json
{
  "data": {
    "refundId": 1,
    "refundCode": "RFD-2025-001234",
    "refundAmount": 1500.0,
    "status": "PENDING",
    "createdAt": "2025-10-15T16:00:00"
  },
  "message": "Refund initiated successfully",
  "success": true
}
```

---

#### 10. Cancel Payment Request

**Endpoint**: `POST /api/v1/payments/requests/{id}/cancel`

**Permission Required**: `PAYMENT_MGMT:cancel`

**Restrictions**: Only DRAFT and PENDING payments can be cancelled

**Request Body**:

```json
{
  "cancellationReason": "Customer no longer requires service"
}
```

**Response**:

```json
{
  "data": {
    "id": 1,
    "status": "CANCELLED",
    "cancelledAt": "2025-10-15T17:00:00"
  },
  "message": "Payment request cancelled successfully",
  "success": true
}
```

---

#### 11. Get Payment Transactions

**Endpoint**: `GET /api/v1/payments/requests/{id}/transactions`

**Permission Required**: `PAYMENT_MGMT:read`

**Response**:

```json
{
  "data": [
    {
      "id": 1,
      "transactionCode": "TXN-2025-001234",
      "transactionType": "PAYMENT",
      "transactionStatus": "SUCCESS",
      "amount": 1500.0,
      "paymentMethod": "CREDIT_CARD",
      "processedAt": "2025-10-15T10:35:00",
      "createdAt": "2025-10-15T10:30:00"
    }
  ],
  "message": "Transactions retrieved successfully",
  "success": true
}
```

---

#### 12. Get Payment Audit Log

**Endpoint**: `GET /api/v1/payments/requests/{id}/audit-log`

**Permission Required**: `PAYMENT_MGMT:read`

**Response**:

```json
{
  "data": [
    {
      "id": 1,
      "action": "CREATE",
      "entityType": "PAYMENT_REQUEST",
      "oldStatus": null,
      "newStatus": "PENDING",
      "createdAt": "2025-10-15T10:30:00",
      "createdBy": "admin@example.com"
    },
    {
      "id": 2,
      "action": "VERIFY",
      "entityType": "PAYMENT_REQUEST",
      "oldStatus": "PROCESSING",
      "newStatus": "COMPLETED",
      "reason": "Payment verified through bank statement",
      "createdAt": "2025-10-15T14:30:00",
      "createdBy": "admin@example.com"
    }
  ],
  "message": "Audit log retrieved successfully",
  "success": true
}
```

---

### Service Layer Implementation Guidelines

#### PaymentRequestService Interface

```java
package com.ahss.service;

import com.ahss.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentRequestService {

    // CRUD Operations
    PaymentRequestDto createPaymentRequest(CreatePaymentRequestDto dto);
    PaymentRequestDto getPaymentRequestById(Long id);
    PaymentRequestDto getPaymentRequestByToken(String paymentToken);
    Page<PaymentRequestDto> getAllPaymentRequests(Pageable pageable, String status, String search);
    PaymentRequestDto updatePaymentRequest(Long id, UpdatePaymentRequestDto dto);

    // Payment Operations
    PaymentTransactionDto processPayment(String paymentToken, ProcessPaymentDto dto);
    PaymentRequestDto verifyPaymentRequest(Long id, String verificationNotes);
    PaymentRequestDto voidPayment(Long id, String voidReason);
    PaymentRefundDto refundPayment(Long id, RefundRequestDto dto);
    PaymentRequestDto cancelPaymentRequest(Long id, String cancellationReason);

    // Query Operations
    List<PaymentTransactionDto> getPaymentTransactions(Long paymentRequestId);
    List<PaymentAuditLogDto> getPaymentAuditLog(Long paymentRequestId);

    // Validation Methods
    boolean isPaymentRequestExpired(PaymentRequest paymentRequest);
    boolean canUpdatePaymentRequest(PaymentRequest paymentRequest);
    boolean canCancelPaymentRequest(PaymentRequest paymentRequest);
    boolean canVoidPayment(PaymentRequest paymentRequest);
    boolean canRefundPayment(PaymentRequest paymentRequest);
}
```

#### Key Implementation Notes

1. **Transaction Management**: All state-changing operations must be wrapped in `@Transactional`
2. **Audit Logging**: Every operation must create an audit log entry
3. **Permission Validation**: Use `@PreAuthorize` annotations for method-level security
4. **Error Handling**: Use custom exceptions (e.g., `PaymentNotFoundException`, `InvalidPaymentStateException`)
5. **Multi-tenant Isolation**: Always filter by `tenantId` from JWT token
6. **Token Generation**: Use secure random UUID for payment tokens
7. **Code Generation**: Generate sequential codes with year prefix (PR-2025-XXXXXX)

#### Updated DTO Field Mappings

**PaymentRequestDto.java** (Synchronized with Frontend)
```java
public class PaymentRequestDto {
    private Long id;
    private String code;
    private String title;                    // ✅ Aligned with frontend
    private BigDecimal amount;
    private String currency;
    private String payerName;                // ✅ Aligned with frontend
    private String payerEmail;               // ✅ Aligned with frontend
    private String payerPhone;               // ✅ New field added
    private PaymentMethodType[] allowedPaymentMethods;  // ✅ New field added
    private PaymentMethodType preSelectedPaymentMethod; // ✅ Aligned with frontend
    private LocalDateTime expiresAt;         // ✅ Aligned with frontend (ISO string in JSON)
    private String paymentToken;             // ✅ Aligned with frontend
    private PaymentRequestStatus status;
    private LocalDateTime createdAt;         // ✅ Serialized as ISO string
    private LocalDateTime updatedAt;         // ✅ Serialized as ISO string
    // ... other fields
}
```

**CreatePaymentRequestDto.java** (Synchronized with Frontend)
```java
public class CreatePaymentRequestDto {
    @NotBlank(message = "Title is required")
    private String title;                    // ✅ Changed from description
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    @NotBlank(message = "Currency is required")
    private String currency = "USD";
    
    private String payerName;                // ✅ Changed from requestorName
    
    @Email(message = "Invalid email format")
    private String payerEmail;               // ✅ Changed from requestorEmail
    
    private String payerPhone;               // ✅ New field added
    
    @NotEmpty(message = "At least one payment method must be allowed")
    private PaymentMethodType[] allowedPaymentMethods;  // ✅ New field added
    
    private PaymentMethodType preSelectedPaymentMethod; // ✅ Changed from paymentMethod
    
    @Future(message = "Expiration date must be in the future")
    private LocalDateTime expiresAt;         // ✅ Changed from dueDate
}
```

**Field Migration Summary**:
- `description` → `title` (more descriptive naming)
- `requestorName` → `payerName` (clearer role definition)
- `requestorEmail` → `payerEmail` (clearer role definition)
- `paymentMethod` → `preSelectedPaymentMethod` (more explicit)
- `dueDate` → `expiresAt` (clearer semantics)
- Added: `payerPhone` (additional contact method)
- Added: `allowedPaymentMethods[]` (flexible payment options)
- Added: `paymentToken` (secure access token)

---

## Frontend Requirements

### Page Structure

```
src/pages/payments/
├── PaymentRequestList.tsx          # List all payment requests (admin)
├── PaymentRequestCreate.tsx        # Create new payment request
├── PaymentRequestDetail.tsx        # View/manage single payment request
├── PaymentLinkPage.tsx            # Public payment page (token-based)
├── PaymentSuccess.tsx             # Payment success confirmation
└── PaymentFailed.tsx              # Payment failure page
```

### Component Structure

```
src/components/payments/
├── PaymentRequestCard.tsx         # Card display for payment request
├── PaymentStatusBadge.tsx         # Status badge component
├── PaymentMethodSelector.tsx      # Payment method selection
├── PaymentForm.tsx                # Payment form for credit card, etc.
├── TransactionHistory.tsx         # Display transaction history
├── RefundDialog.tsx               # Refund initiation dialog
├── VoidDialog.tsx                 # Void payment dialog
├── CancelDialog.tsx               # Cancel payment dialog
└── PaymentAuditLog.tsx           # Audit log display
```

### 1. Payment Request List Page

**File**: `src/pages/payments/PaymentRequestList.tsx`

**Features**:

- Paginated list of all payment requests
- Search and filter by status, date range, payer email
- Bulk operations support
- Permission-based action buttons
- Status indicators with color coding
- Quick actions (view, verify, cancel)

**UI Components**:

- Use `SearchAndFilter` component for consistent search UI
- Use `Table` component from Shadcn/UI
- Use `Badge` component for status display
- Use `Button` component for actions
- Use `Dialog` component for confirmations

**Key Permissions**:

- View list: `PAYMENT_MGMT:read`
- Create request: `PAYMENT_MGMT:create`
- Update request: `PAYMENT_MGMT:update`
- Delete request: `PAYMENT_MGMT:delete`

**Implementation Example**:

```typescript
import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { Plus, Eye, CheckCircle, XCircle } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import SearchAndFilter from "@/components/SearchAndFilter";
import PaymentStatusBadge from "@/components/payments/PaymentStatusBadge";
import PermissionGuard from "@/components/PermissionGuard";
import { paymentService } from "@/lib/api";

const PaymentRequestList: React.FC = () => {
  const navigate = useNavigate();
  const [paymentRequests, setPaymentRequests] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState("all");
  const [pagination, setPagination] = useState({
    page: 0,
    size: 20,
    totalElements: 0,
    totalPages: 0,
  });

  useEffect(() => {
    fetchPaymentRequests();
  }, [searchTerm, statusFilter, pagination.page]);

  const fetchPaymentRequests = async () => {
    setLoading(true);
    try {
      const response = await paymentService.getPaymentRequests({
        page: pagination.page,
        size: pagination.size,
        status: statusFilter !== "all" ? statusFilter : undefined,
        search: searchTerm,
      });
      setPaymentRequests(response.data.content);
      setPagination({
        ...pagination,
        totalElements: response.data.totalElements,
        totalPages: response.data.totalPages,
      });
    } catch (error) {
      console.error("Error fetching payment requests:", error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container mx-auto py-6 space-y-6">
      <Card>
        <CardHeader className="flex flex-row items-center justify-between">
          <CardTitle className="text-2xl font-bold">Payment Requests</CardTitle>
          <PermissionGuard permission="PAYMENT_MGMT:create">
            <Button onClick={() => navigate("/payments/create")}>
              <Plus className="h-4 w-4 mr-2" />
              Create Payment Request
            </Button>
          </PermissionGuard>
        </CardHeader>
        <CardContent>
          <SearchAndFilter
            searchTerm={searchTerm}
            onSearchChange={setSearchTerm}
            searchPlaceholder="Search by request code, payer name, or email..."
            filters={[
              {
                key: "status",
                label: "Status",
                value: statusFilter,
                onChange: setStatusFilter,
                options: [
                  { value: "all", label: "All Statuses" },
                  { value: "DRAFT", label: "Draft" },
                  { value: "PENDING", label: "Pending" },
                  { value: "PROCESSING", label: "Processing" },
                  { value: "COMPLETED", label: "Completed" },
                  { value: "FAILED", label: "Failed" },
                  { value: "CANCELLED", label: "Cancelled" },
                  { value: "VOIDED", label: "Voided" },
                  { value: "REFUNDED", label: "Refunded" },
                ],
              },
            ]}
          />

          {/* Table implementation */}
          <div className="mt-6">
            {/* Payment requests table with actions */}
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default PaymentRequestList;
```

---

### 2. Payment Request Create Page

**File**: `src/pages/payments/PaymentRequestCreate.tsx`

**Features**:

- Comprehensive form for payment request creation
- Payment method selection (multiple)
- Pre-selected payment method option
- Expiration date picker
- Amount and currency input
- Payer information fields
- Metadata/custom fields support
- Form validation with Zod schema
- Preview payment link before creation

**Form Fields**:

```typescript
interface CreatePaymentRequestForm {
  title: string;
  description?: string;
  amount: number;
  currency: string;
  payerName?: string;
  payerEmail?: string;
  payerPhone?: string;
  allowedPaymentMethods: PaymentMethodType[];
  preSelectedPaymentMethod?: PaymentMethodType;
  expiresAt?: Date;
  metadata?: Record<string, any>;
}

const paymentRequestSchema = z.object({
  title: z.string().min(3, "Title must be at least 3 characters"),
  description: z.string().optional(),
  amount: z.number().positive("Amount must be positive"),
  currency: z.string().length(3, "Currency must be 3 characters"),
  payerEmail: z.string().email("Invalid email format").optional(),
  allowedPaymentMethods: z
    .array(
      z.enum([
        "CREDIT_CARD",
        "DEBIT_CARD",
        "BANK_TRANSFER",
        "DIGITAL_WALLET",
        "PAYPAL",
        "STRIPE",
      ])
    )
    .min(1, "Select at least one payment method"),
  expiresAt: z
    .date()
    .min(new Date(), "Expiration date must be in future")
    .optional(),
});
```

---

### 3. Payment Request Detail Page

**File**: `src/pages/payments/PaymentRequestDetail.tsx`

**Features**:

- Complete payment request information display
- Payment link with copy-to-clipboard functionality
- QR code for payment link
- Transaction history
- Audit log display
- Status management (Verify, Void, Refund, Cancel)
- Permission-based action buttons
- Timeline view of all activities

**Sections**:

1. **Payment Details Card**: Amount, currency, status, payer info
2. **Payment Link Card**: Shareable link with QR code
3. **Actions Card**: Verify, void, refund, cancel buttons
4. **Transaction History**: All associated transactions
5. **Audit Log**: Complete activity trail

**Implementation Pattern**:

```typescript
import React, { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import PermissionGuard from "@/components/PermissionGuard";
import PaymentStatusBadge from "@/components/payments/PaymentStatusBadge";
import TransactionHistory from "@/components/payments/TransactionHistory";
import PaymentAuditLog from "@/components/payments/PaymentAuditLog";
import RefundDialog from "@/components/payments/RefundDialog";
import VoidDialog from "@/components/payments/VoidDialog";
import CancelDialog from "@/components/payments/CancelDialog";

const PaymentRequestDetail: React.FC = () => {
  const { id } = useParams();
  const [paymentRequest, setPaymentRequest] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [auditLog, setAuditLog] = useState([]);

  // Implementation details...

  return (
    <div className="container mx-auto py-6 space-y-6">
      {/* Payment Details Card */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle>{paymentRequest?.title}</CardTitle>
            <PaymentStatusBadge status={paymentRequest?.status} />
          </div>
        </CardHeader>
        <CardContent>{/* Payment details content */}</CardContent>
      </Card>

      {/* Payment Link Card */}
      <Card>
        <CardHeader>
          <CardTitle>Payment Link</CardTitle>
        </CardHeader>
        <CardContent>
          {/* Payment link with copy button and QR code */}
        </CardContent>
      </Card>

      {/* Actions Card */}
      <Card>
        <CardHeader>
          <CardTitle>Actions</CardTitle>
        </CardHeader>
        <CardContent className="flex gap-2">
          <PermissionGuard permission="PAYMENT_MGMT:verify">
            <Button variant="default">Verify Payment</Button>
          </PermissionGuard>
          <PermissionGuard permission="PAYMENT_MGMT:void">
            <Button variant="destructive">Void Payment</Button>
          </PermissionGuard>
          <PermissionGuard permission="PAYMENT_MGMT:refund">
            <Button variant="outline">Refund Payment</Button>
          </PermissionGuard>
          <PermissionGuard permission="PAYMENT_MGMT:cancel">
            <Button variant="ghost">Cancel Request</Button>
          </PermissionGuard>
        </CardContent>
      </Card>

      {/* Transaction History */}
      <TransactionHistory transactions={transactions} />

      {/* Audit Log */}
      <PaymentAuditLog auditLog={auditLog} />
    </div>
  );
};
```

---

### 4. Payment Link Page (Public)

**File**: `src/pages/payments/PaymentLinkPage.tsx`

**Features**:

- Public page accessible via payment token
- No authentication required
- Display payment request details
- Payment method selection (if not pre-selected)
- Payment form for selected method
- Credit card form (card number, expiry, CVV)
- Bank transfer instructions
- Digital wallet integration
- Real-time form validation
- Loading states during processing
- Redirect to success/failure page

**Security Considerations**:

- Validate payment token on every request
- Check expiration date before showing form
- Implement CSRF protection
- Use HTTPS only
- Do not store sensitive payment data
- Validate all inputs client and server-side

**Implementation Example**:

```typescript
import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import PaymentMethodSelector from "@/components/payments/PaymentMethodSelector";
import PaymentForm from "@/components/payments/PaymentForm";
import { paymentService } from "@/lib/api";

const PaymentLinkPage: React.FC = () => {
  const { paymentToken } = useParams();
  const navigate = useNavigate();
  const [paymentRequest, setPaymentRequest] = useState(null);
  const [selectedMethod, setSelectedMethod] = useState(null);
  const [loading, setLoading] = useState(true);
  const [processing, setProcessing] = useState(false);

  useEffect(() => {
    fetchPaymentRequest();
  }, [paymentToken]);

  const fetchPaymentRequest = async () => {
    try {
      const response = await paymentService.getPaymentRequestByToken(
        paymentToken
      );
      setPaymentRequest(response.data);
      setSelectedMethod(response.data.preSelectedPaymentMethod);
    } catch (error) {
      console.error("Error fetching payment request:", error);
      navigate("/payment-failed");
    } finally {
      setLoading(false);
    }
  };

  const handlePayment = async (paymentDetails) => {
    setProcessing(true);
    try {
      const response = await paymentService.processPayment(paymentToken, {
        paymentMethod: selectedMethod,
        paymentMethodDetails: paymentDetails,
      });
      navigate("/payment-success", {
        state: { transactionCode: response.data.transactionCode },
      });
    } catch (error) {
      navigate("/payment-failed", {
        state: { error: error.message },
      });
    } finally {
      setProcessing(false);
    }
  };

  if (loading) {
    return <div>Loading payment details...</div>;
  }

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
      <Card className="w-full max-w-2xl">
        <CardHeader>
          <CardTitle className="text-2xl">{paymentRequest.title}</CardTitle>
          {paymentRequest.description && (
            <p className="text-gray-600">{paymentRequest.description}</p>
          )}
        </CardHeader>
        <CardContent className="space-y-6">
          {/* Amount Display */}
          <div className="bg-blue-50 p-6 rounded-lg text-center">
            <div className="text-sm text-gray-600">Amount Due</div>
            <div className="text-4xl font-bold text-blue-600">
              {paymentRequest.currency} {paymentRequest.amount}
            </div>
          </div>

          {/* Payment Method Selection */}
          {!paymentRequest.preSelectedPaymentMethod && (
            <PaymentMethodSelector
              allowedMethods={paymentRequest.allowedPaymentMethods}
              selectedMethod={selectedMethod}
              onSelect={setSelectedMethod}
            />
          )}

          {/* Payment Form */}
          {selectedMethod && (
            <PaymentForm
              paymentMethod={selectedMethod}
              onSubmit={handlePayment}
              processing={processing}
            />
          )}
        </CardContent>
      </Card>
    </div>
  );
};

export default PaymentLinkPage;
```

---

### 5. Component: PaymentStatusBadge

**File**: `src/components/payments/PaymentStatusBadge.tsx`

```typescript
import React from "react";
import { Badge } from "@/components/ui/badge";

interface PaymentStatusBadgeProps {
  status: string;
}

const statusConfig = {
  DRAFT: { variant: "secondary", label: "Draft" },
  PENDING: { variant: "default", label: "Pending" },
  PROCESSING: { variant: "default", label: "Processing" },
  COMPLETED: { variant: "success", label: "Completed" },
  FAILED: { variant: "destructive", label: "Failed" },
  CANCELLED: { variant: "secondary", label: "Cancelled" },
  VOIDED: { variant: "destructive", label: "Voided" },
  REFUNDED: { variant: "outline", label: "Refunded" },
  PARTIAL_REFUND: { variant: "outline", label: "Partial Refund" },
};

const PaymentStatusBadge: React.FC<PaymentStatusBadgeProps> = ({ status }) => {
  const config = statusConfig[status] || statusConfig.DRAFT;

  return <Badge variant={config.variant}>{config.label}</Badge>;
};

export default PaymentStatusBadge;
```

---

### 6. API Service Layer (Frontend)

**File**: `src/lib/api/paymentService.ts`

```typescript
import api from "@/lib/api";

export interface CreatePaymentRequestDto {
  title: string;
  description?: string;
  amount: number;
  currency: string;
  payerName?: string;
  payerEmail?: string;
  payerPhone?: string;
  allowedPaymentMethods: string[];
  preSelectedPaymentMethod?: string;
  expiresAt?: string;
  metadata?: Record<string, any>;
}

export interface ProcessPaymentDto {
  paymentMethod: string;
  paymentMethodDetails: Record<string, any>;
}

export interface RefundRequestDto {
  refundAmount: number;
  refundReason: string;
  refundType: "FULL" | "PARTIAL";
}

export const paymentService = {
  // Create payment request
  createPaymentRequest: (data: CreatePaymentRequestDto) =>
    api.post("/v1/payments/requests", data),

  // Get payment request by ID
  getPaymentRequestById: (id: number) => api.get(`/v1/payments/requests/${id}`),

  // Get payment request by token (public)
  getPaymentRequestByToken: (token: string) =>
    api.get(`/v1/payments/requests/by-token/${token}`),

  // Get all payment requests
  getPaymentRequests: (params?: {
    page?: number;
    size?: number;
    status?: string;
    search?: string;
  }) => api.get("/v1/payments/requests", { params }),

  // Update payment request
  updatePaymentRequest: (id: number, data: Partial<CreatePaymentRequestDto>) =>
    api.put(`/v1/payments/requests/${id}`, data),

  // Process payment
  processPayment: (token: string, data: ProcessPaymentDto) =>
    api.post(`/v1/payments/requests/${token}/process`, data),

  // Verify payment
  verifyPayment: (id: number, verificationNotes?: string) =>
    api.post(`/v1/payments/requests/${id}/verify`, { verificationNotes }),

  // Void payment
  voidPayment: (id: number, voidReason: string) =>
    api.post(`/v1/payments/requests/${id}/void`, { voidReason }),

  // Refund payment
  refundPayment: (id: number, data: RefundRequestDto) =>
    api.post(`/v1/payments/requests/${id}/refund`, data),

  // Cancel payment request
  cancelPaymentRequest: (id: number, cancellationReason: string) =>
    api.post(`/v1/payments/requests/${id}/cancel`, { cancellationReason }),

  // Get transactions
  getPaymentTransactions: (id: number) =>
    api.get(`/v1/payments/requests/${id}/transactions`),

  // Get audit log
  getPaymentAuditLog: (id: number) =>
    api.get(`/v1/payments/requests/${id}/audit-log`),
};
```

---

### 7. Permission Hook Updates

**File**: `src/hooks/usePermissions.ts`

Add payment permissions to the existing hook:

```typescript
// Add to existing usePermissions hook
export const usePermissions = () => {
  const { permissions } = useAuth();

  // ... existing permissions ...

  // Payment Management Permissions
  const canViewPayments = hasPermission("PAYMENT_MGMT:read");
  const canCreatePayments = hasPermission("PAYMENT_MGMT:create");
  const canUpdatePayments = hasPermission("PAYMENT_MGMT:update");
  const canDeletePayments = hasPermission("PAYMENT_MGMT:delete");
  const canVerifyPayments = hasPermission("PAYMENT_MGMT:verify");
  const canVoidPayments = hasPermission("PAYMENT_MGMT:void");
  const canRefundPayments = hasPermission("PAYMENT_MGMT:refund");
  const canCancelPayments = hasPermission("PAYMENT_MGMT:cancel");

  return {
    // ... existing permissions ...

    // Payment permissions
    canViewPayments,
    canCreatePayments,
    canUpdatePayments,
    canDeletePayments,
    canVerifyPayments,
    canVoidPayments,
    canRefundPayments,
    canCancelPayments,
  };
};
```

---

### 8. Routing Configuration

**File**: `src/App.tsx`

Add payment routes to existing routing:

```typescript
// Add to existing routes
<Route
  path="/payments"
  element={
    <ProtectedRoute permission="PAYMENT_MGMT:read">
      <PaymentRequestList />
    </ProtectedRoute>
  }
/>

<Route
  path="/payments/create"
  element={
    <ProtectedRoute permission="PAYMENT_MGMT:create">
      <PaymentRequestCreate />
    </ProtectedRoute>
  }
/>

<Route
  path="/payments/:id"
  element={
    <ProtectedRoute permission="PAYMENT_MGMT:read">
      <PaymentRequestDetail />
    </ProtectedRoute>
  }
/>

{/* Public payment link route - no authentication required */}
<Route
  path="/pay/:paymentToken"
  element={<PaymentLinkPage />}
/>

<Route path="/payment-success" element={<PaymentSuccess />} />
<Route path="/payment-failed" element={<PaymentFailed />} />
```

---

### 9. Navigation Menu Updates

**File**: `src/components/Layout.tsx`

Add payment menu item to navigation:

```typescript
// Add to navigation items in Layout.tsx
const navigationItems = [
  // ... existing items ...
  {
    path: "/payments",
    label: "Payments",
    icon: DollarSign, // from lucide-react
    permission: "PAYMENT_MGMT:read",
  },
];
```

---

## Frontend-Backend Synchronization

### PaymentRequest Interface Alignment

The frontend and backend PaymentRequest structures have been synchronized to ensure consistent data handling across the application.

#### Updated Field Mappings

| Purpose | Frontend Field | Backend Field | Type | Notes |
|---------|---------------|---------------|------|-------|
| Payment Title | `title` | `title` | `string` | Replaces old `description` field |
| Payer Name | `payerName` | `payerName` | `string` | Replaces old `requestorName` |
| Payer Email | `payerEmail` | `payerEmail` | `string` | Replaces old `requestorEmail` |
| Payer Phone | `payerPhone` | `payerPhone` | `string` | New field for contact info |
| Payment Methods | `allowedPaymentMethods` | `allowedPaymentMethods` | `PaymentMethodType[]` | New array field |
| Pre-selected Method | `preSelectedPaymentMethod` | `preSelectedPaymentMethod` | `PaymentMethodType` | Replaces old `paymentMethod` |
| Expiration | `expiresAt` | `expiresAt` | `string` (ISO) | Replaces old `dueDate` |
| Payment Token | `paymentToken` | `paymentToken` | `string` | New field for secure access |

#### Payment Method Type Enum Synchronization

**Backend Enum**: `PaymentMethodType.java`
```java
public enum PaymentMethodType {
    CREDIT_CARD,
    DEBIT_CARD,
    BANK_TRANSFER,
    PAYPAL,
    STRIPE,
    MANUAL
}
```

**Frontend Mapping**: `payment.ts`
```typescript
export const PAYMENT_METHOD_TYPE_MAPPINGS = {
  CREDIT_CARD: "Credit Card",
  DEBIT_CARD: "Debit Card", 
  BANK_TRANSFER: "Bank Transfer",
  PAYPAL: "PayPal",
  STRIPE: "Stripe",
  MANUAL: "Manual Payment"
} as const;
```

#### Component Updates Summary

##### PaymentRequestList.tsx
- **Search Filter**: Updated to search by `title`, `payerName`, `payerEmail`
- **Table Headers**: Changed "Requestor" → "Payer", "Due Date" → "Expires At"
- **Data Display**: Updated field references to new structure
- **Date Formatting**: Added `.toString()` for `createdAt` field

##### PaymentRequestCreate.tsx
- **Form Fields**: Updated all form inputs to use new field names
- **Validation**: Updated validation logic for new required fields
- **State Management**: Updated `formData` state structure
- **Submit Logic**: Aligned with new `CreatePaymentRequestDto` structure

#### Type Definitions Updated

##### Frontend Types (`payment.ts`)
```typescript
// Updated PaymentRequest interface
export interface PaymentRequest {
  id: string;
  code: string;
  title: string;                    // was: description
  amount: number;
  currency: string;
  payerName?: string;               // was: requestorName
  payerEmail?: string;              // was: requestorEmail
  payerPhone?: string;              // new field
  allowedPaymentMethods: PaymentMethodType[];  // new field
  preSelectedPaymentMethod?: PaymentMethodType; // was: paymentMethod
  expiresAt?: string;               // was: dueDate
  paymentToken: string;             // new field
  status: PaymentRequestStatus;
  createdAt: string;                // changed from Date to string
  updatedAt: string;
  // ... other fields remain the same
}

// Updated CreatePaymentRequestDto
export interface CreatePaymentRequestDto {
  title: string;                    // was: description
  amount: number;
  currency: string;
  payerName?: string;               // was: requestorName
  payerEmail?: string;              // was: requestorEmail
  payerPhone?: string;              // new field
  allowedPaymentMethods: PaymentMethodType[];  // new field
  preSelectedPaymentMethod?: PaymentMethodType; // was: paymentMethod
  expiresAt?: string;               // was: dueDate
}
```

#### Migration Notes

1. **Breaking Changes**: Old field names are no longer supported
2. **Type Safety**: All components now use consistent TypeScript types
3. **Validation**: Form validation updated to match new structure
4. **API Compatibility**: Frontend DTOs now match backend exactly
5. **Date Handling**: All dates are now handled as ISO strings

#### Testing Verification

- ✅ TypeScript compilation successful with no errors
- ✅ Development server running without type conflicts
- ✅ Form validation working with new field structure
- ✅ Payment method display correctly mapped
- ✅ Component rendering with updated field references

---

## Payment Workflows

### Workflow 1: Create Payment Request with Pre-selected Method

```
1. Admin creates payment request
   ├─ Fill form with amount, title, description
   ├─ Add payer information (optional)
   ├─ Select allowed payment methods
   ├─ Pre-select specific payment method (e.g., CREDIT_CARD)
   └─ Set expiration date (optional)

2. System generates payment link
   ├─ Create unique payment token (UUID)
   ├─ Generate payment link: https://yourapp.com/pay/{token}
   ├─ Store payment request in database (status: PENDING)
   └─ Return payment link to admin

3. Admin shares payment link
   └─ Via email, SMS, or direct sharing

4. Payer accesses payment link
   ├─ System validates token and expiration
   ├─ Display payment details
   ├─ Show pre-selected payment method (CREDIT_CARD)
   └─ Payer cannot change payment method

5. Payer completes payment
   ├─ Enter credit card details
   ├─ Submit payment form
   ├─ System processes payment (status: PROCESSING)
   └─ Create payment transaction record

6. Payment verification
   ├─ Admin verifies payment (manual or automatic)
   ├─ Update status to COMPLETED
   ├─ Send confirmation email to payer
   └─ Log audit trail
```

---

### Workflow 2: Create Payment Request with Method Selection

```
1. Admin creates payment request
   ├─ Fill form with amount, title, description
   ├─ Add payer information (optional)
   ├─ Select multiple allowed payment methods
   │  └─ e.g., [CREDIT_CARD, DEBIT_CARD, BANK_TRANSFER]
   ├─ Do NOT pre-select payment method
   └─ Set expiration date (optional)

2. System generates payment link
   └─ Same as Workflow 1

3. Payer accesses payment link
   ├─ System validates token and expiration
   ├─ Display payment details
   ├─ Show all allowed payment methods
   └─ Payer selects preferred payment method

4. Payer completes payment
   ├─ Based on selected method:
   │  ├─ Credit/Debit Card: Enter card details
   │  ├─ Bank Transfer: Show bank details and instructions
   │  └─ Digital Wallet: Redirect to wallet provider
   ├─ Submit payment
   └─ System processes based on selected method

5. Payment verification
   └─ Same as Workflow 1
```

---

### Workflow 3: Verify Payment Request

```
1. Admin views payment request details
   └─ Status must be PROCESSING or PENDING

2. Admin clicks "Verify Payment"
   ├─ Enter verification notes (optional)
   └─ Confirm verification

3. System processes verification
   ├─ Check if payment transaction exists
   ├─ Update payment request status to COMPLETED
   ├─ Set paid_at timestamp
   ├─ Create audit log entry
   └─ Send confirmation email to payer

4. System response
   └─ Display success message with updated status
```

---

### Workflow 4: Void Payment

```
1. Admin views completed payment request
   └─ Status must be COMPLETED

2. Admin clicks "Void Payment"
   ├─ Enter void reason (required)
   └─ Confirm void action

3. System processes void
   ├─ Validate payment can be voided
   ├─ Update payment request status to VOIDED
   ├─ Create void transaction record
   ├─ Create audit log entry
   └─ Optional: Trigger gateway void operation

4. System response
   └─ Display success message with voided status

5. Financial reconciliation
   └─ Admin manually handles financial adjustments
```

---

### Workflow 5: Refund Payment

```
1. Admin views completed payment request
   └─ Status must be COMPLETED

2. Admin clicks "Refund Payment"
   ├─ Enter refund amount
   ├─ Select refund type (FULL or PARTIAL)
   ├─ Enter refund reason (required)
   └─ Confirm refund

3. System processes refund
   ├─ Validate refund amount ≤ payment amount
   ├─ Create refund record (status: PENDING)
   ├─ Optional: Trigger gateway refund operation
   ├─ Update payment request status
   │  ├─ REFUNDED (if full refund)
   │  └─ PARTIAL_REFUND (if partial)
   └─ Create audit log entry

4. Refund processing
   ├─ If automatic: Gateway processes refund
   │  ├─ On success: Update refund status to SUCCESS
   │  └─ On failure: Update refund status to FAILED
   └─ If manual: Admin marks refund as processed

5. System response
   └─ Display refund details with status
```

---

### Workflow 6: Cancel Payment Request

```
1. Admin views payment request
   └─ Status must be DRAFT or PENDING

2. Admin clicks "Cancel Request"
   ├─ Enter cancellation reason (required)
   └─ Confirm cancellation

3. System processes cancellation
   ├─ Validate payment has not been processed
   ├─ Update payment request status to CANCELLED
   ├─ Create audit log entry
   └─ Optional: Send cancellation email to payer

4. System response
   └─ Display cancellation confirmation

5. Payment link behavior
   └─ Link becomes inaccessible (shows "Payment request cancelled" message)
```

---

## Security & Compliance

### Payment Data Security

#### PCI DSS Compliance Considerations

1. **Never Store Sensitive Card Data**

   - Do NOT store full credit card numbers
   - Do NOT store CVV/CVC codes
   - Do NOT store magnetic stripe data
   - Store only last 4 digits for reference (masked)

2. **Data Encryption**

   - All payment data transmitted over HTTPS/TLS 1.2+
   - Use payment gateway tokenization
   - Encrypt sensitive data at rest
   - Use strong encryption algorithms (AES-256)

3. **Access Control**
   - Implement principle of least privilege
   - Require MFA for payment operations
   - Log all access to payment data
   - Regular access reviews

#### Implementation Guidelines

**Backend Security**:

```java
// PaymentController.java - Security annotations
@RestController
@RequestMapping("/api/v1/payments")
@PreAuthorize("isAuthenticated()")
public class PaymentController {

    @PostMapping("/requests")
    @PreAuthorize("hasAuthority('PAYMENT_MGMT:create')")
    public ResponseEntity<ApiResponse<PaymentRequestDto>> createPaymentRequest(
        @Valid @RequestBody CreatePaymentRequestDto dto,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        // Validate tenant context
        Long tenantId = getCurrentTenantId(userDetails);

        // Implementation
    }

    @PostMapping("/requests/{paymentToken}/process")
    @RateLimiter(name = "payment-processing", fallbackMethod = "paymentRateLimitFallback")
    public ResponseEntity<ApiResponse<PaymentTransactionDto>> processPayment(
        @PathVariable String paymentToken,
        @Valid @RequestBody ProcessPaymentDto dto,
        HttpServletRequest request
    ) {
        // Log IP address for fraud detection
        String ipAddress = getClientIpAddress(request);

        // Validate token
        // Process payment
        // Never log sensitive card data
    }
}
```

**Frontend Security**:

```typescript
// Never log sensitive payment data
const handlePaymentSubmit = async (paymentDetails) => {
  // Remove sensitive data before logging
  const sanitizedData = {
    ...paymentDetails,
    cardNumber: "****", // Never log actual card number
    cvv: "***", // Never log CVV
  };

  console.log("Processing payment:", sanitizedData);

  try {
    // Send actual data to backend (over HTTPS only)
    const response = await paymentService.processPayment(token, paymentDetails);

    // Clear sensitive data from memory
    paymentDetails = null;

    return response;
  } catch (error) {
    // Log error without sensitive data
    console.error("Payment failed");
    throw error;
  }
};
```

---

### Multi-Tenant Security

#### Tenant Isolation

```java
// PaymentRequestServiceImpl.java
@Service
@Transactional
public class PaymentRequestServiceImpl implements PaymentRequestService {

    @Override
    public PaymentRequestDto createPaymentRequest(CreatePaymentRequestDto dto) {
        // Get current user's tenant ID from security context
        Long tenantId = SecurityUtils.getCurrentTenantId();

        // Ensure tenant isolation
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setTenantId(tenantId);
        // ... other fields

        return convertToDto(paymentRequestRepository.save(paymentRequest));
    }

    @Override
    public PaymentRequestDto getPaymentRequestById(Long id) {
        Long tenantId = SecurityUtils.getCurrentTenantId();

        // Always filter by tenant ID
        PaymentRequest paymentRequest = paymentRequestRepository
            .findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new PaymentNotFoundException("Payment request not found"));

        return convertToDto(paymentRequest);
    }
}
```

#### Repository Layer Tenant Filtering

```java
// PaymentRequestRepository.java
@Repository
public interface PaymentRequestRepository extends JpaRepository<PaymentRequest, Long> {

    // Always include tenantId in queries
    Optional<PaymentRequest> findByIdAndTenantId(Long id, Long tenantId);

    Page<PaymentRequest> findByTenantIdAndStatusIn(
        Long tenantId,
        List<PaymentRequestStatus> statuses,
        Pageable pageable
    );

    @Query("SELECT pr FROM PaymentRequest pr WHERE pr.paymentToken = :token")
    Optional<PaymentRequest> findByPaymentToken(@Param("token") String token);
    // Note: Public endpoint, no tenant filtering needed
}
```

---

### Audit Logging

#### Comprehensive Audit Trail

```java
// PaymentAuditService.java
@Service
public class PaymentAuditService {

    @Autowired
    private PaymentAuditLogRepository auditLogRepository;

    public void logPaymentAction(
        Long paymentRequestId,
        String action,
        String entityType,
        String oldStatus,
        String newStatus,
        String reason,
        HttpServletRequest request
    ) {
        PaymentAuditLog auditLog = new PaymentAuditLog();
        auditLog.setPaymentRequestId(paymentRequestId);
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setOldStatus(oldStatus);
        auditLog.setNewStatus(newStatus);
        auditLog.setReason(reason);
        auditLog.setIpAddress(getClientIpAddress(request));
        auditLog.setUserAgent(request.getHeader("User-Agent"));
        auditLog.setCreatedBy(SecurityUtils.getCurrentUserId());

        auditLogRepository.save(auditLog);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
```

---

### Rate Limiting

#### Prevent Payment Abuse

```java
// Application.yml configuration
resilience4j:
  ratelimiter:
    instances:
      payment-processing:
        limitForPeriod: 5
        limitRefreshPeriod: 60s
        timeoutDuration: 0s

// Controller implementation
@PostMapping("/requests/{paymentToken}/process")
@RateLimiter(name = "payment-processing")
public ResponseEntity<ApiResponse<PaymentTransactionDto>> processPayment(
    @PathVariable String paymentToken,
    @Valid @RequestBody ProcessPaymentDto dto
) {
    // Rate limited to 5 attempts per minute per IP
    return ResponseEntity.ok(
        ApiResponse.ok(paymentService.processPayment(paymentToken, dto))
    );
}
```

---

## Integration Requirements

### Email Notification Integration

#### Required Email Templates

1. **Payment Request Created**

   - To: Payer (if email provided)
   - Content: Payment details, payment link, expiration date

2. **Payment Received**

   - To: Payer
   - Content: Payment confirmation, transaction ID, receipt

3. **Payment Verified**

   - To: Payer
   - Content: Verification confirmation, final receipt

4. **Payment Failed**

   - To: Payer
   - Content: Failure reason, retry instructions

5. **Refund Processed**

   - To: Payer
   - Content: Refund confirmation, amount, expected timeline

6. **Payment Cancelled**
   - To: Payer
   - Content: Cancellation confirmation, reason

#### Email Service Interface

```java
// EmailService.java
@Service
public interface EmailService {

    void sendPaymentRequestEmail(
        String toEmail,
        String payerName,
        PaymentRequestDto paymentRequest,
        String paymentLink
    );

    void sendPaymentReceivedEmail(
        String toEmail,
        String payerName,
        PaymentTransactionDto transaction
    );

    void sendPaymentVerifiedEmail(
        String toEmail,
        String payerName,
        PaymentRequestDto paymentRequest
    );

    void sendPaymentFailedEmail(
        String toEmail,
        String payerName,
        String failureReason
    );

    void sendRefundProcessedEmail(
        String toEmail,
        String payerName,
        PaymentRefundDto refund
    );

    void sendPaymentCancelledEmail(
        String toEmail,
        String payerName,
        String cancellationReason
    );
}
```

---

### Payment Gateway Integration (Placeholder)

**Note**: Direct payment gateway integration is out of scope for Phase 1, but the architecture should support future integration.

#### Gateway Interface Design

```java
// PaymentGatewayService.java
@Service
public interface PaymentGatewayService {

    /**
     * Process payment through gateway
     * @return Gateway transaction ID
     */
    String processPayment(
        PaymentMethodType paymentMethod,
        BigDecimal amount,
        String currency,
        Map<String, Object> paymentDetails
    ) throws PaymentGatewayException;

    /**
     * Void payment transaction
     */
    void voidTransaction(String gatewayTransactionId)
        throws PaymentGatewayException;

    /**
     * Refund payment transaction
     */
    String refundTransaction(
        String gatewayTransactionId,
        BigDecimal refundAmount
    ) throws PaymentGatewayException;

    /**
     * Get transaction status from gateway
     */
    PaymentTransactionStatus getTransactionStatus(String gatewayTransactionId);
}
```

#### Mock Implementation for Phase 1

```java
// MockPaymentGatewayService.java
@Service
@Profile("dev")
public class MockPaymentGatewayService implements PaymentGatewayService {

    @Override
    public String processPayment(
        PaymentMethodType paymentMethod,
        BigDecimal amount,
        String currency,
        Map<String, Object> paymentDetails
    ) {
        // Mock successful payment
        return "MOCK-TXN-" + UUID.randomUUID().toString();
    }

    @Override
    public void voidTransaction(String gatewayTransactionId) {
        // Mock void - always succeeds
    }

    @Override
    public String refundTransaction(
        String gatewayTransactionId,
        BigDecimal refundAmount
    ) {
        // Mock refund - always succeeds
        return "MOCK-REFUND-" + UUID.randomUUID().toString();
    }

    @Override
    public PaymentTransactionStatus getTransactionStatus(String gatewayTransactionId) {
        return PaymentTransactionStatus.SUCCESS;
    }
}
```

---

## Testing Requirements

### Unit Tests

#### Backend Unit Tests

1. **Entity Tests**

   - Test entity creation with all fields
   - Test validation constraints
   - Test enum mappings
   - Test audit field generation

2. **Service Layer Tests**

   - Test all CRUD operations
   - Test business logic validation
   - Test permission checks
   - Test tenant isolation
   - Test state transitions
   - Test error handling

3. **Controller Tests**
   - Test all endpoints with valid data
   - Test validation errors
   - Test permission requirements
   - Test error responses

**Example Service Test**:

```java
// PaymentRequestServiceTest.java
@SpringBootTest
@Transactional
class PaymentRequestServiceTest {

    @Autowired
    private PaymentRequestService paymentRequestService;

    @Test
    void testCreatePaymentRequest_Success() {
        // Arrange
        CreatePaymentRequestDto dto = new CreatePaymentRequestDto();
        dto.setTitle("Test Payment");
        dto.setAmount(new BigDecimal("100.00"));
        dto.setCurrency("USD");
        dto.setAllowedPaymentMethods(
            new PaymentMethodType[]{PaymentMethodType.CREDIT_CARD}
        );

        // Act
        PaymentRequestDto result = paymentRequestService.createPaymentRequest(dto);

        // Assert
        assertNotNull(result.getId());
        assertNotNull(result.getPaymentToken());
        assertEquals("Test Payment", result.getTitle());
        assertEquals(PaymentRequestStatus.PENDING, result.getStatus());
    }

    @Test
    void testVoidPayment_InvalidStatus_ThrowsException() {
        // Arrange
        PaymentRequest paymentRequest = createPaymentRequest(PaymentRequestStatus.PENDING);

        // Act & Assert
        assertThrows(InvalidPaymentStateException.class, () -> {
            paymentRequestService.voidPayment(paymentRequest.getId(), "Test void");
        });
    }
}
```

---

### Integration Tests

#### API Integration Tests

```java
// PaymentControllerIntegrationTest.java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class PaymentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(authorities = {"PAYMENT_MGMT:create"})
    void testCreatePaymentRequest_Integration() throws Exception {
        // Arrange
        CreatePaymentRequestDto dto = new CreatePaymentRequestDto();
        dto.setTitle("Integration Test Payment");
        dto.setAmount(new BigDecimal("200.00"));
        dto.setCurrency("USD");
        dto.setAllowedPaymentMethods(
            new PaymentMethodType[]{PaymentMethodType.CREDIT_CARD}
        );

        // Act & Assert
        mockMvc.perform(post("/api/v1/payments/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.id").exists())
            .andExpect(jsonPath("$.data.paymentToken").exists())
            .andExpect(jsonPath("$.data.status").value("PENDING"));
    }
}
```

---

### Frontend Tests

#### Component Tests

```typescript
// PaymentRequestList.test.tsx
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import PaymentRequestList from "@/pages/payments/PaymentRequestList";
import { paymentService } from "@/lib/api";

jest.mock("@/lib/api");

describe("PaymentRequestList", () => {
  it("should display payment requests", async () => {
    // Arrange
    const mockPaymentRequests = [
      {
        id: 1,
        requestCode: "PR-2025-001",
        title: "Test Payment",
        amount: 100.0,
        status: "PENDING",
      },
    ];

    (paymentService.getPaymentRequests as jest.Mock).mockResolvedValue({
      data: {
        content: mockPaymentRequests,
        totalElements: 1,
        totalPages: 1,
      },
    });

    // Act
    render(<PaymentRequestList />);

    // Assert
    await waitFor(() => {
      expect(screen.getByText("Test Payment")).toBeInTheDocument();
      expect(screen.getByText("PR-2025-001")).toBeInTheDocument();
    });
  });

  it("should filter by status", async () => {
    // Test implementation
  });
});
```

---

### End-to-End Tests

#### E2E Test Scenarios

1. **Complete Payment Flow**

   - Create payment request
   - Access payment link
   - Select payment method
   - Complete payment
   - Verify payment status

2. **Refund Flow**

   - Complete payment
   - Initiate refund
   - Verify refund status

3. **Cancellation Flow**
   - Create payment request
   - Cancel before payment
   - Verify link is inaccessible

---

## Troubleshooting

### Common Issues and Solutions

#### 1. Payment Method Display Issues

**Problem**: Payment methods not displaying correctly or showing undefined values

**Possible Causes**:
- Frontend-backend enum mismatch
- Missing payment method mappings
- Incorrect field references in components

**Solutions**:
```typescript
// ✅ Correct: Use PAYMENT_METHOD_TYPE_MAPPINGS
import { PAYMENT_METHOD_TYPE_MAPPINGS } from '@/types/payment';

const displayMethod = PAYMENT_METHOD_TYPE_MAPPINGS[paymentMethod] || 'Unknown';

// ❌ Incorrect: Direct enum usage
const displayMethod = paymentMethod; // May show "CREDIT_CARD" instead of "Credit Card"
```

**Verification Steps**:
1. Check that `PaymentMethodType` enum matches between frontend and backend
2. Verify `PAYMENT_METHOD_TYPE_MAPPINGS` includes all enum values
3. Ensure components use the mapping for display

#### 2. Field Reference Errors

**Problem**: TypeScript errors about missing properties or undefined field access

**Common Errors**:
```typescript
// ❌ Old field names (will cause errors)
request.description     // Use: request.title
request.requestorName   // Use: request.payerName
request.requestorEmail  // Use: request.payerEmail
request.paymentMethod   // Use: request.preSelectedPaymentMethod
request.dueDate        // Use: request.expiresAt
```

**Solutions**:
```typescript
// ✅ Updated field references
const title = request.title;
const payerName = request.payerName;
const payerEmail = request.payerEmail;
const paymentMethod = request.preSelectedPaymentMethod;
const expiresAt = request.expiresAt;
```

#### 3. Date Handling Issues

**Problem**: Date formatting errors or type mismatches

**Issue**: `createdAt` and `updatedAt` are now strings (ISO format) instead of Date objects

**Solution**:
```typescript
// ✅ Correct: Convert to string before formatting
const formattedDate = formatDate(request.createdAt.toString());

// ✅ Alternative: Handle both string and Date types
const formattedDate = formatDate(
  typeof request.createdAt === 'string' 
    ? request.createdAt 
    : request.createdAt.toISOString()
);
```

#### 4. Form Validation Errors

**Problem**: Form validation failing with new field structure

**Common Issues**:
- Validation rules referencing old field names
- Missing validation for new required fields
- Incorrect field types in validation schema

**Solution**:
```typescript
// ✅ Updated validation schema
const validationSchema = z.object({
  title: z.string().min(1, "Title is required"),           // was: description
  payerName: z.string().optional(),                        // was: requestorName
  payerEmail: z.string().email().optional(),               // was: requestorEmail
  payerPhone: z.string().optional(),                       // new field
  allowedPaymentMethods: z.array(z.enum(PaymentMethodType)), // new field
  preSelectedPaymentMethod: z.enum(PaymentMethodType).optional(), // was: paymentMethod
  expiresAt: z.string().optional(),                        // was: dueDate
});
```

#### 5. API Integration Issues

**Problem**: API calls failing due to DTO structure mismatch

**Symptoms**:
- 400 Bad Request errors
- Field validation errors from backend
- Null or undefined values in API responses

**Debugging Steps**:
1. Check network tab for actual request/response data
2. Verify DTO structure matches backend expectations
3. Ensure all required fields are included in requests

**Example Fix**:
```typescript
// ✅ Correct API call structure
const createPaymentRequest = async (data: CreatePaymentRequestDto) => {
  const payload = {
    title: data.title,                    // not description
    amount: data.amount,
    currency: data.currency,
    payerName: data.payerName,            // not requestorName
    payerEmail: data.payerEmail,          // not requestorEmail
    payerPhone: data.payerPhone,          // new field
    allowedPaymentMethods: data.allowedPaymentMethods, // new field
    preSelectedPaymentMethod: data.preSelectedPaymentMethod, // not paymentMethod
    expiresAt: data.expiresAt,            // not dueDate
  };
  
  return await api.post('/payment-requests', payload);
};
```

#### 6. Component Rendering Issues

**Problem**: Components not rendering or showing blank data

**Common Causes**:
- Accessing undefined properties
- Incorrect conditional rendering logic
- Missing null/undefined checks

**Solution**:
```typescript
// ✅ Safe property access with fallbacks
const PaymentRequestCard = ({ request }: { request: PaymentRequest }) => {
  return (
    <div>
      <h3>{request.title || 'Untitled Payment'}</h3>
      <p>Payer: {request.payerName || 'Not specified'}</p>
      <p>Email: {request.payerEmail || 'Not provided'}</p>
      <p>Method: {
        request.preSelectedPaymentMethod 
          ? PAYMENT_METHOD_TYPE_MAPPINGS[request.preSelectedPaymentMethod]
          : 'Any method allowed'
      }</p>
      <p>Expires: {
        request.expiresAt 
          ? formatDate(request.expiresAt) 
          : 'No expiration'
      }</p>
    </div>
  );
};
```

### Development Environment Issues

#### TypeScript Compilation Errors

**Problem**: Build failing due to type errors

**Quick Fixes**:
1. Clear TypeScript cache: `rm -rf node_modules/.cache`
2. Restart TypeScript server in IDE
3. Run `npm run type-check` to identify specific errors
4. Ensure all imports are updated to use new types

#### Development Server Issues

**Problem**: Hot reload not working after interface changes

**Solution**:
1. Stop development server (`Ctrl+C`)
2. Clear build cache: `rm -rf dist/ .vite/`
3. Restart server: `npm run dev`

### Performance Considerations

#### Large Payment Lists

**Issue**: Slow rendering with many payment requests

**Optimization**:
```typescript
// ✅ Use React.memo for payment request cards
const PaymentRequestCard = React.memo(({ request }: { request: PaymentRequest }) => {
  // Component implementation
});

// ✅ Implement virtual scrolling for large lists
import { FixedSizeList as List } from 'react-window';
```

#### Search Performance

**Issue**: Slow search with large datasets

**Solution**:
- Implement debounced search
- Use server-side filtering for large datasets
- Add pagination to limit results

---

## Success Criteria

### Functional Requirements

- [ ] Payment requests can be created with all required fields
- [ ] Payment links are generated with secure tokens
- [ ] Payment links are accessible without authentication
- [ ] Pre-selected payment method restricts choice correctly
- [ ] Multiple payment methods can be offered and selected
- [ ] Payment forms validate all inputs correctly
- [ ] Payments can be verified by authorized users
- [ ] Payments can be voided with proper authorization
- [ ] Refunds (full and partial) can be processed
- [ ] Payment requests can be cancelled before payment
- [ ] Transaction history is complete and accurate
- [ ] Audit logs capture all payment operations
- [ ] Multi-tenant isolation is enforced
- [ ] Permission system controls access correctly

### Non-Functional Requirements

- [ ] API response time < 500ms for reads
- [ ] API response time < 2s for payment processing
- [ ] Payment link generation < 1s
- [ ] Support 100 concurrent payment processes
- [ ] All sensitive data is encrypted
- [ ] PCI DSS compliance guidelines followed
- [ ] Rate limiting prevents abuse (5 attempts/minute)
- [ ] All operations logged for audit
- [ ] 99.9% uptime for payment services
- [ ] Payment forms are mobile-responsive
- [ ] Payment links work across all major browsers
- [ ] Error messages are user-friendly
- [ ] Loading states provide feedback

### Testing Requirements

- [ ] Unit test coverage ≥ 80%
- [ ] Integration tests for all API endpoints
- [ ] E2E tests for critical payment flows
- [ ] Security testing completed
- [ ] Performance testing under load
- [ ] Penetration testing for vulnerabilities

### Documentation

- [ ] API documentation complete (Swagger/OpenAPI)
- [ ] Database schema documented
- [ ] Permission matrix documented
- [ ] Email templates documented
- [ ] Deployment guide created
- [ ] User manual for payment operations
- [ ] Admin guide for payment management

---

## Implementation Checklist

### Phase 1: Backend Development

- [ ] Create database migration V15 (payment tables)
- [ ] Create database migration V16 (payment permissions)
- [ ] Create all entity classes with proper annotations
- [ ] Create all enum types
- [ ] Create repository interfaces with tenant filtering
- [ ] Create service interfaces
- [ ] Implement service layer with business logic
- [ ] Create DTOs for all request/response objects
- [ ] Implement REST controllers with security
- [ ] Add validation annotations
- [ ] Implement audit logging service
- [ ] Create mock payment gateway service
- [ ] Add rate limiting configuration
- [ ] Write unit tests (≥80% coverage)
- [ ] Write integration tests for all endpoints
- [ ] Test multi-tenant isolation
- [ ] Test permission enforcement

### Phase 2: Frontend Development

- [ ] Create payment service API layer
- [ ] Add payment permissions to usePermissions hook
- [ ] Create PaymentRequestList page
- [ ] Create PaymentRequestCreate page
- [ ] Create PaymentRequestDetail page
- [ ] Create PaymentLinkPage (public)
- [ ] Create PaymentSuccess page
- [ ] Create PaymentFailed page
- [ ] Create PaymentStatusBadge component
- [ ] Create PaymentMethodSelector component
- [ ] Create PaymentForm component
- [ ] Create TransactionHistory component
- [ ] Create RefundDialog component
- [ ] Create VoidDialog component
- [ ] Create CancelDialog component
- [ ] Create PaymentAuditLog component
- [ ] Add payment routes to App.tsx
- [ ] Add payment navigation to Layout
- [ ] Implement form validation with Zod
- [ ] Add loading states with Skeleton
- [ ] Add error handling
- [ ] Write component tests
- [ ] Write E2E tests for critical flows
- [ ] Test responsive design
- [ ] Test cross-browser compatibility

### Phase 3: Integration & Testing

- [ ] Integrate email notification service
- [ ] Test email templates
- [ ] Test payment link sharing
- [ ] Test QR code generation
- [ ] Test all permission guards
- [ ] Test rate limiting
- [ ] Perform security audit
- [ ] Conduct penetration testing
- [ ] Load testing (100 concurrent users)
- [ ] Test multi-tenant scenarios
- [ ] Verify audit logs are complete
- [ ] Test error scenarios
- [ ] Test edge cases

### Phase 4: Documentation & Deployment

- [ ] Complete API documentation (Swagger)
- [ ] Document permission matrix
- [ ] Create user manual
- [ ] Create admin guide
- [ ] Write deployment guide
- [ ] Create runbook for operations
- [ ] Document troubleshooting steps
- [ ] Set up monitoring and alerts
- [ ] Configure backup procedures
- [ ] Plan disaster recovery
- [ ] Deploy to staging environment
- [ ] Conduct UAT (User Acceptance Testing)
- [ ] Deploy to production
- [ ] Monitor production metrics

---

## Appendices

### Appendix A: Sample Data

#### Sample Payment Request JSON

```json
{
  "title": "Monthly Subscription - Premium Plan",
  "description": "Payment for Premium plan subscription for January 2025",
  "amount": 49.99,
  "currency": "USD",
  "payerName": "Jane Smith",
  "payerEmail": "jane.smith@example.com",
  "payerPhone": "+1-555-0123",
  "allowedPaymentMethods": ["CREDIT_CARD", "DEBIT_CARD", "PAYPAL"],
  "preSelectedPaymentMethod": null,
  "expiresAt": "2025-01-31T23:59:59",
  "metadata": {
    "subscriptionId": "SUB-2025-001",
    "planType": "PREMIUM",
    "billingCycle": "MONTHLY"
  }
}
```

---

### Appendix B: Error Codes

| Code    | Message                              | HTTP Status |
| ------- | ------------------------------------ | ----------- |
| PAY-001 | Payment request not found            | 404         |
| PAY-002 | Payment request expired              | 410         |
| PAY-003 | Invalid payment method               | 400         |
| PAY-004 | Invalid payment state for operation  | 422         |
| PAY-005 | Insufficient permissions             | 403         |
| PAY-006 | Payment already processed            | 409         |
| PAY-007 | Refund amount exceeds payment amount | 400         |
| PAY-008 | Payment token invalid or expired     | 401         |
| PAY-009 | Rate limit exceeded                  | 429         |
| PAY-010 | Payment gateway error                | 502         |

---

### Appendix C: Status Transitions

```
DRAFT → PENDING → PROCESSING → COMPLETED
                               ↓
                            VOIDED
                               ↓
                          REFUNDED / PARTIAL_REFUND

DRAFT → CANCELLED
PENDING → CANCELLED
PROCESSING → FAILED
```

**Valid Transitions**:

- DRAFT → PENDING (on payment link activation)
- PENDING → PROCESSING (on payment initiation)
- PROCESSING → COMPLETED (on successful payment)
- PROCESSING → FAILED (on payment failure)
- COMPLETED → VOIDED (admin action)
- COMPLETED → REFUNDED (admin action - full refund)
- COMPLETED → PARTIAL_REFUND (admin action - partial refund)
- DRAFT → CANCELLED (admin action)
- PENDING → CANCELLED (admin action)

---

## Version History

| Version | Date       | Author   | Changes              |
| ------- | ---------- | -------- | -------------------- |
| 1.0     | 2025-10-15 | AI Coder | Initial PRD creation |

---

## Notes for AI Coder

1. **Follow Existing Patterns**: This application has well-established patterns for:

   - Database migrations (Flyway)
   - Entity-Repository-Service-Controller architecture
   - Permission system (RBAC/ABAC)
   - Frontend component structure (Shadcn/UI)
     -Frontend component structure (Shadcn/UI)
   - Multi-tenant architecture

2. **Code Quality Standards**:

   - Use TypeScript strict mode for frontend
   - Use Java 21 features where appropriate
   - Follow existing naming conventions (e.g., `payment_request_id` for database, `paymentRequestId` for Java)
   - Use Lombok annotations (`@Data`, `@Builder`) for entity classes
   - Implement proper error handling with custom exceptions
   - Add comprehensive logging (use SLF4J)

3. **Security Considerations**:

   - **CRITICAL**: Never log sensitive payment data (card numbers, CVV, etc.)
   - Always validate tenant context in service layer
   - Use `@PreAuthorize` for method-level security
   - Implement CSRF protection for state-changing operations
   - Rate limit payment processing endpoints
   - Validate all inputs on both client and server side

4. **Database Considerations**:

   - Use PostgreSQL-specific features (JSONB, array types, enums)
   - Create proper indexes for performance
   - Add foreign key constraints with appropriate cascade behavior
   - Use `ON DELETE RESTRICT` for payment-related foreign keys to prevent accidental data loss
   - Add database comments for documentation

5. **Frontend Considerations**:

   - Use existing Shadcn/UI components for consistency
   - Follow the `SearchAndFilter` component pattern for list pages
   - Implement proper loading states with `Skeleton` components
   - Use `PermissionGuard` and `ProtectedRoute` for access control
   - Implement proper form validation with React Hook Form + Zod
   - Use the existing `usePermissions` hook pattern
   - Follow the existing API service pattern in `src/lib/api`

6. **Testing Requirements**:

   - Write unit tests for all service methods
   - Write integration tests for all API endpoints
   - Write component tests for all React components
   - Mock external dependencies (payment gateway, email service)
   - Test permission enforcement at all levels
   - Test multi-tenant isolation thoroughly

7. **Performance Optimization**:

   - Use pagination for list endpoints
   - Add database indexes on frequently queried columns
   - Use `@Transactional(readOnly = true)` for read operations
   - Implement caching for frequently accessed data (optional)
   - Optimize database queries (avoid N+1 problems)

8. **Backward Compatibility**:

   - Ensure new migrations don't break existing data
   - Add new permissions to existing Super Administrator role
   - Don't modify existing API endpoints unless necessary
   - Follow semantic versioning for API changes

9. **Documentation Requirements**:

   - Add JavaDoc comments to all public methods
   - Add JSDoc comments to TypeScript functions
   - Document all API endpoints with OpenAPI/Swagger annotations
   - Create migration comments explaining database changes
   - Update the technical documentation files

10. **Email Integration**:

    - Use existing email service pattern (if available)
    - Create email templates in a separate templates directory
    - Use HTML templates with proper styling
    - Include unsubscribe links where required
    - Test email sending in development environment

11. **Deployment Considerations**:

    - Ensure migrations run in correct order (V15, V16)
    - Add database migration rollback scripts
    - Configure environment variables for production
    - Set up monitoring for payment endpoints
    - Configure alerts for payment failures
    - Plan for zero-downtime deployment

12. **Future Extensibility**:
    - Design payment gateway interface for easy integration
    - Use strategy pattern for different payment methods
    - Keep payment method logic pluggable
    - Design for multi-currency support (future)
    - Consider webhook handling for async payment updates

---

## Appendix D: Database Indexes Performance Guide

### Recommended Index Strategy

```sql
-- High Priority Indexes (Create immediately)
CREATE INDEX idx_payment_request_tenant_status ON payment_request(tenant_id, status);
CREATE INDEX idx_payment_request_token_hash ON payment_request(payment_token) WHERE status NOT IN ('CANCELLED', 'COMPLETED');
CREATE INDEX idx_payment_transaction_request_type ON payment_transaction(payment_request_id, transaction_type);

-- Medium Priority Indexes (Create if performance issues arise)
CREATE INDEX idx_payment_request_payer_email_tenant ON payment_request(payer_email, tenant_id) WHERE payer_email IS NOT NULL;
CREATE INDEX idx_payment_request_created_at_tenant ON payment_request(tenant_id, created_at DESC);
CREATE INDEX idx_payment_audit_log_request_created ON payment_audit_log(payment_request_id, created_at DESC);

-- Low Priority Indexes (Monitor and create if needed)
CREATE INDEX idx_payment_request_expires_pending ON payment_request(expires_at) WHERE status = 'PENDING' AND expires_at IS NOT NULL;
```

### Index Maintenance

```sql
-- Monitor index usage
SELECT
    schemaname,
    tablename,
    indexname,
    idx_scan,
    idx_tup_read,
    idx_tup_fetch
FROM pg_stat_user_indexes
WHERE schemaname = 'public'
AND tablename LIKE 'payment%'
ORDER BY idx_scan ASC;

-- Find unused indexes
SELECT
    schemaname || '.' || tablename AS table,
    indexname AS index,
    pg_size_pretty(pg_relation_size(indexrelid)) AS index_size
FROM pg_stat_user_indexes
WHERE idx_scan = 0
AND schemaname = 'public'
AND tablename LIKE 'payment%';
```

---

## Appendix E: Payment Method Details Schema

### Credit Card Payment Method Details

```json
{
  "paymentMethodDetails": {
    "last4": "1111",
    "cardBrand": "VISA",
    "expiryMonth": "12",
    "expiryYear": "2026",
    "cardHolderName": "John Doe",
    "billingAddress": {
      "line1": "123 Main St",
      "city": "New York",
      "state": "NY",
      "postalCode": "10001",
      "country": "US"
    }
  }
}
```

### Bank Transfer Payment Method Details

```json
{
  "paymentMethodDetails": {
    "bankName": "Chase Bank",
    "accountNumberLast4": "5678",
    "routingNumber": "021000021",
    "accountHolderName": "John Doe",
    "transferReference": "PAY-2025-001234"
  }
}
```

### Digital Wallet Payment Method Details

```json
{
  "paymentMethodDetails": {
    "walletType": "PAYPAL",
    "walletEmail": "john.doe@example.com",
    "walletTransactionId": "PAYPAL-TXN-123456"
  }
}
```

---

## Appendix F: API Response Examples

### Success Response Format

```json
{
  "data": {
    "id": 1,
    "requestCode": "PR-2025-001234",
    "title": "Invoice Payment",
    "status": "COMPLETED"
  },
  "message": "Operation completed successfully",
  "success": true,
  "timestamp": "2025-10-15T10:30:00Z"
}
```

### Error Response Format

```json
{
  "data": null,
  "message": "Payment request not found",
  "success": false,
  "error": {
    "code": "PAY-001",
    "details": "No payment request found with ID: 999",
    "field": null
  },
  "timestamp": "2025-10-15T10:30:00Z"
}
```

### Validation Error Response Format

```json
{
  "data": null,
  "message": "Validation failed",
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "details": "Invalid request data",
    "validationErrors": [
      {
        "field": "amount",
        "message": "Amount must be positive"
      },
      {
        "field": "allowedPaymentMethods",
        "message": "At least one payment method must be selected"
      }
    ]
  },
  "timestamp": "2025-10-15T10:30:00Z"
}
```

---

## Appendix G: Email Template Examples

### Payment Request Created Email

```html
<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8" />
    <style>
      body {
        font-family: Arial, sans-serif;
        line-height: 1.6;
        color: #333;
      }
      .container {
        max-width: 600px;
        margin: 0 auto;
        padding: 20px;
      }
      .header {
        background: #3b82f6;
        color: white;
        padding: 20px;
        text-align: center;
      }
      .content {
        padding: 20px;
        background: #f9fafb;
      }
      .button {
        display: inline-block;
        padding: 12px 24px;
        background: #3b82f6;
        color: white;
        text-decoration: none;
        border-radius: 4px;
      }
      .footer {
        padding: 20px;
        text-align: center;
        font-size: 12px;
        color: #6b7280;
      }
      .amount {
        font-size: 32px;
        font-weight: bold;
        color: #3b82f6;
        text-align: center;
        margin: 20px 0;
      }
    </style>
  </head>
  <body>
    <div class="container">
      <div class="header">
        <h1>Payment Request</h1>
      </div>
      <div class="content">
        <p>Hello {{payerName}},</p>
        <p>You have received a payment request:</p>

        <h2>{{title}}</h2>
        <p>{{description}}</p>

        <div class="amount">{{currency}} {{amount}}</div>

        <p style="text-align: center; margin: 30px 0;">
          <a href="{{paymentLink}}" class="button">Pay Now</a>
        </p>

        <p><strong>Request Code:</strong> {{requestCode}}</p>
        <p><strong>Expires:</strong> {{expiresAt}}</p>

        <p>If you have any questions, please contact our support team.</p>
      </div>
      <div class="footer">
        <p>This is an automated email. Please do not reply to this message.</p>
        <p>&copy; 2025 AHSS Shared Services. All rights reserved.</p>
      </div>
    </div>
  </body>
</html>
```

### Payment Completed Email

```html
<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8" />
    <style>
      body {
        font-family: Arial, sans-serif;
        line-height: 1.6;
        color: #333;
      }
      .container {
        max-width: 600px;
        margin: 0 auto;
        padding: 20px;
      }
      .header {
        background: #10b981;
        color: white;
        padding: 20px;
        text-align: center;
      }
      .content {
        padding: 20px;
        background: #f9fafb;
      }
      .success-icon {
        font-size: 48px;
        text-align: center;
        color: #10b981;
      }
      .receipt {
        background: white;
        padding: 20px;
        border: 1px solid #e5e7eb;
        border-radius: 4px;
        margin: 20px 0;
      }
      .receipt-row {
        display: flex;
        justify-content: space-between;
        padding: 10px 0;
        border-bottom: 1px solid #e5e7eb;
      }
      .receipt-row:last-child {
        border-bottom: none;
        font-weight: bold;
      }
      .footer {
        padding: 20px;
        text-align: center;
        font-size: 12px;
        color: #6b7280;
      }
    </style>
  </head>
  <body>
    <div class="container">
      <div class="header">
        <h1>Payment Successful</h1>
      </div>
      <div class="content">
        <div class="success-icon">✓</div>

        <p>Hello {{payerName}},</p>
        <p>Your payment has been successfully processed.</p>

        <div class="receipt">
          <h3>Payment Receipt</h3>
          <div class="receipt-row">
            <span>Transaction ID:</span>
            <span>{{transactionCode}}</span>
          </div>
          <div class="receipt-row">
            <span>Date:</span>
            <span>{{processedAt}}</span>
          </div>
          <div class="receipt-row">
            <span>Description:</span>
            <span>{{title}}</span>
          </div>
          <div class="receipt-row">
            <span>Payment Method:</span>
            <span>{{paymentMethod}}</span>
          </div>
          <div class="receipt-row">
            <span>Amount Paid:</span>
            <span>{{currency}} {{amount}}</span>
          </div>
        </div>

        <p>
          Thank you for your payment. A copy of this receipt has been sent to
          your email.
        </p>
      </div>
      <div class="footer">
        <p>This is an automated email. Please do not reply to this message.</p>
        <p>&copy; 2025 AHSS Shared Services. All rights reserved.</p>
      </div>
    </div>
  </body>
</html>
```

---

## Appendix H: Frontend Type Definitions

### Complete TypeScript Interfaces

```typescript
// src/types/payment.types.ts

export enum PaymentRequestStatus {
  DRAFT = "DRAFT",
  PENDING = "PENDING",
  PROCESSING = "PROCESSING",
  COMPLETED = "COMPLETED",
  FAILED = "FAILED",
  CANCELLED = "CANCELLED",
  VOIDED = "VOIDED",
  REFUNDED = "REFUNDED",
  PARTIAL_REFUND = "PARTIAL_REFUND",
}

export enum PaymentMethodType {
  CREDIT_CARD = "CREDIT_CARD",
  DEBIT_CARD = "DEBIT_CARD",
  BANK_TRANSFER = "BANK_TRANSFER",
  DIGITAL_WALLET = "DIGITAL_WALLET",
  PAYPAL = "PAYPAL",
  STRIPE = "STRIPE",
  MANUAL = "MANUAL",
}

export enum PaymentTransactionType {
  PAYMENT = "PAYMENT",
  REFUND = "REFUND",
  VOID = "VOID",
  CHARGEBACK = "CHARGEBACK",
}

export enum PaymentTransactionStatus {
  PENDING = "PENDING",
  SUCCESS = "SUCCESS",
  FAILED = "FAILED",
  CANCELLED = "CANCELLED",
}

export interface PaymentRequest {
  id: string;
  requestCode: string;
  paymentToken: string;
  paymentLink: string;
  title: string;
  description?: string;
  amount: number;
  currency: string;
  payerName?: string;
  payerEmail?: string;
  payerPhone?: string;
  allowedPaymentMethods: PaymentMethodType[];
  preSelectedPaymentMethod?: PaymentMethodType;
  status: PaymentRequestStatus;
  expiresAt?: string;
  paidAt?: string;
  tenantId: number;
  metadata?: Record<string, any>;
  createdAt: string;
  updatedAt: string;
  createdBy?: number;
  updatedBy?: number;
}

export interface PaymentTransaction {
  id: number;
  transactionCode: string;
  externalTransactionId?: string;
  paymentRequestId: string;
  transactionType: PaymentTransactionType;
  transactionStatus: PaymentTransactionStatus;
  amount: number;
  currency: string;
  paymentMethod: PaymentMethodType;
  paymentMethodDetails?: Record<string, any>;
  gatewayName?: string;
  gatewayResponse?: Record<string, any>;
  processedAt?: string;
  errorCode?: string;
  errorMessage?: string;
  metadata?: Record<string, any>;
  createdAt: string;
  updatedAt: string;
  createdBy?: number;
}

export interface PaymentRefund {
  id: number;
  refundCode: string;
  paymentTransactionId: number;
  refundAmount: number;
  refundReason: string;
  refundStatus: PaymentTransactionStatus;
  externalRefundId?: string;
  gatewayResponse?: Record<string, any>;
  processedAt?: string;
  metadata?: Record<string, any>;
  createdAt: string;
  updatedAt: string;
  createdBy: number;
}

export interface PaymentAuditLog {
  id: number;
  paymentRequestId?: string;
  paymentTransactionId?: number;
  paymentRefundId?: number;
  action: string;
  entityType: string;
  oldStatus?: string;
  newStatus?: string;
  changes?: Record<string, any>;
  reason?: string;
  ipAddress?: string;
  userAgent?: string;
  createdAt: string;
  createdBy?: number;
  createdByEmail?: string;
}

export interface CreatePaymentRequestDto {
  title: string;
  description?: string;
  amount: number;
  currency: string;
  payerName?: string;
  payerEmail?: string;
  payerPhone?: string;
  allowedPaymentMethods: PaymentMethodType[];
  preSelectedPaymentMethod?: PaymentMethodType;
  expiresAt?: string;
  metadata?: Record<string, any>;
}

export interface UpdatePaymentRequestDto {
  title?: string;
  description?: string;
  amount?: number;
  expiresAt?: string;
  metadata?: Record<string, any>;
}

export interface ProcessPaymentDto {
  paymentMethod: PaymentMethodType;
  paymentMethodDetails: Record<string, any>;
}

export interface RefundRequestDto {
  refundAmount: number;
  refundReason: string;
  refundType: "FULL" | "PARTIAL";
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface ApiResponse<T> {
  data: T;
  message: string;
  success: boolean;
  timestamp?: string;
  error?: {
    code: string;
    details: string;
    field?: string;
    validationErrors?: Array<{
      field: string;
      message: string;
    }>;
  };
}
```

---

## Appendix I: Monitoring and Alerting

### Key Metrics to Monitor

1. **Payment Processing Metrics**

   - Payment success rate (target: >95%)
   - Average payment processing time (target: <2s)
   - Payment failure rate by reason
   - Payment method distribution

2. **API Performance Metrics**

   - API response time (p50, p95, p99)
   - API error rate (target: <1%)
   - Request rate (requests per second)
   - Database query performance

3. **Security Metrics**

   - Failed authentication attempts
   - Rate limit violations
   - Suspicious payment patterns
   - Token validation failures

4. **Business Metrics**
   - Total payment volume (daily/weekly/monthly)
   - Average payment amount
   - Payment request conversion rate
   - Refund rate (target: <5%)

### Alert Configuration

```yaml
# Example alert configuration (Prometheus/Grafana format)
alerts:
  - name: HighPaymentFailureRate
    condition: payment_failure_rate > 0.10
    duration: 5m
    severity: critical
    message: "Payment failure rate exceeded 10% in the last 5 minutes"

  - name: SlowPaymentProcessing
    condition: payment_processing_time_p95 > 5s
    duration: 10m
    severity: warning
    message: "95th percentile payment processing time exceeded 5 seconds"

  - name: DatabaseConnectionPoolExhaustion
    condition: hikari_connections_active / hikari_connections_max > 0.9
    duration: 2m
    severity: critical
    message: "Database connection pool utilization above 90%"

  - name: HighAPIErrorRate
    condition: api_error_rate > 0.05
    duration: 5m
    severity: warning
    message: "API error rate exceeded 5% in the last 5 minutes"
```

---

## Appendix J: Troubleshooting Guide

### Common Issues and Solutions

#### Issue 1: Payment Link Not Accessible

**Symptoms**: 404 error when accessing payment link

**Possible Causes**:

- Payment token invalid or expired
- Payment request deleted
- Payment request status is CANCELLED or COMPLETED

**Solution**:

```sql
-- Check payment request status
SELECT
    payment_request_id,
    request_code,
    payment_token,
    status,
    expires_at
FROM payment_request
WHERE payment_token = 'YOUR_TOKEN_HERE';

-- Check if expired
SELECT
    CASE
        WHEN expires_at < CURRENT_TIMESTAMP THEN 'EXPIRED'
        ELSE 'VALID'
    END as token_status
FROM payment_request
WHERE payment_token = 'YOUR_TOKEN_HERE';
```

#### Issue 2: Permission Denied Errors

**Symptoms**: 403 Forbidden when trying to perform payment operations

**Possible Causes**:

- User doesn't have required permission
- Permission not assigned to user's role
- JWT token expired or invalid

**Solution**:

```sql
-- Check user permissions
SELECT
    u.email,
    r.name as role_name,
    p.name as permission_name
FROM "user" u
JOIN user_role ur ON u.user_id = ur.user_id
JOIN role r ON ur.role_id = r.role_id
JOIN role_permission rp ON r.role_id = rp.role_id
JOIN permission p ON rp.permission_id = p.permission_id
WHERE u.user_id = YOUR_USER_ID
AND p.resource_type = 'PAYMENT_MGMT';
```

#### Issue 3: Payment Processing Failures

**Symptoms**: Payment gets stuck in PROCESSING status

**Possible Causes**:

- Payment gateway timeout
- Database transaction rollback
- Network connectivity issues

**Solution**:

```sql
-- Check transaction status
SELECT
    pt.transaction_code,
    pt.transaction_status,
    pt.error_code,
    pt.error_message,
    pt.created_at,
    pt.processed_at
FROM payment_transaction pt
JOIN payment_request pr ON pt.payment_request_id = pr.payment_request_id
WHERE pr.request_code = 'YOUR_REQUEST_CODE';

-- Check audit log for errors
SELECT
    action,
    old_status,
    new_status,
    reason,
    created_at
FROM payment_audit_log
WHERE payment_request_id = YOUR_REQUEST_ID
ORDER BY created_at DESC;
```

#### Issue 4: Database Connection Pool Exhaustion

**Symptoms**: Timeout errors, slow response times

**Possible Causes**:

- Connection leaks (transactions not closed)
- Too many concurrent requests
- Long-running queries

**Solution**:

```sql
-- Check active connections
SELECT
    pid,
    usename,
    application_name,
    state,
    query_start,
    state_change,
    query
FROM pg_stat_activity
WHERE datname = 'sharedservices'
AND state = 'active';

-- Kill long-running queries (if necessary)
SELECT pg_terminate_backend(pid)
FROM pg_stat_activity
WHERE datname = 'sharedservices'
AND state = 'active'
AND query_start < NOW() - INTERVAL '10 minutes';
```

---

## Appendix K: Migration Rollback Scripts

### Rollback V16: Remove Payment Permissions

```sql
-- V16_rollback__remove_payment_permissions.sql

-- Remove role-permission mappings
DELETE FROM role_permission
WHERE permission_id IN (
    SELECT permission_id
    FROM permission
    WHERE resource_type = 'PAYMENT_MGMT'
);

-- Remove permissions
DELETE FROM permission
WHERE resource_type = 'PAYMENT_MGMT';

-- Verification
SELECT COUNT(*) as remaining_payment_permissions
FROM permission
WHERE resource_type = 'PAYMENT_MGMT';
-- Should return 0
```

### Rollback V15: Drop Payment Tables

```sql
-- V15_rollback__drop_payment_tables.sql

-- Drop tables in reverse order (respecting foreign keys)
DROP TABLE IF EXISTS payment_audit_log CASCADE;
DROP TABLE IF EXISTS payment_refund CASCADE;
DROP TABLE IF EXISTS payment_transaction CASCADE;
DROP TABLE IF EXISTS payment_request CASCADE;

-- Drop enum types
DROP TYPE IF EXISTS payment_transaction_status CASCADE;
DROP TYPE IF EXISTS payment_transaction_type CASCADE;
DROP TYPE IF EXISTS payment_method_type CASCADE;
DROP TYPE IF EXISTS payment_request_status CASCADE;

-- Drop functions
DROP FUNCTION IF EXISTS update_payment_updated_at_column() CASCADE;

-- Verification
SELECT
    table_name
FROM information_schema.tables
WHERE table_schema = 'public'
AND table_name LIKE 'payment%';
-- Should return empty result
```

---

## Final Recommendations

### Priority Implementation Order

**Week 1-2: Backend Foundation**

1. Create database migrations (V15, V16)
2. Create entity classes
3. Create repository interfaces
4. Implement basic CRUD service layer
5. Create REST controllers with basic endpoints
6. Write unit tests for services

**Week 3-4: Backend Advanced Features** 7. Implement payment processing logic 8. Add audit logging 9. Implement refund/void/cancel operations 10. Add rate limiting 11. Write integration tests 12. Security audit and testing

**Week 5-6: Frontend Foundation** 13. Create API service layer 14. Create basic list page 15. Create payment request create page 16. Create payment detail page 17. Add routing and navigation

**Week 7-8: Frontend Advanced Features** 18. Create public payment link page 19. Create payment form components 20. Add success/failure pages 21. Implement all dialogs (refund, void, cancel) 22. Add comprehensive form validation 23. Write component tests

**Week 9: Integration & Testing** 24. End-to-end testing 25. Security testing 26. Performance testing 27. Cross-browser testing 28. Bug fixes and refinements

**Week 10: Documentation & Deployment** 29. Complete API documentation 30. Write user guides 31. Deploy to staging 32. User acceptance testing 33. Production deployment 34. Post-deployment monitoring

### Success Metrics Timeline

**Month 1**: Core functionality complete

- ✓ Basic payment request creation
- ✓ Payment link generation
- ✓ Payment processing (mock)
- ✓ Admin management interfaces

**Month 2**: Advanced features complete

- ✓ Refund/void/cancel operations
- ✓ Audit logging
- ✓ Email notifications
- ✓ Comprehensive testing

**Month 3**: Production ready

- ✓ Security hardened
- ✓ Performance optimized
- ✓ Documentation complete
- ✓ Deployed and monitored

---

## Contact and Support

For questions or clarifications during implementation:

1. **Technical Questions**: Refer to existing codebase patterns
2. **Security Concerns**: Follow PCI DSS guidelines strictly
3. **Performance Issues**: Consult the monitoring appendix
4. **Database Issues**: Review the troubleshooting guide

---

**END OF PRD**
