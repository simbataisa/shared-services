# Payment UUID Implementation Technical Guide

## Overview

This document provides technical details about the UUID implementation for payment entities in the shared-services application. It covers the changes made to resolve Hibernate schema validation errors and ensure proper UUID handling across the payment system.

## Background

The payment system was originally designed with `BIGINT` primary keys for payment requests. To improve security, scalability, and follow modern best practices, the system was migrated to use UUID (Universally Unique Identifier) for payment request IDs.

## Changes Made

### 1. Payment Transaction ID UUID Migration (V20)

A significant migration was implemented to convert the `payment_transaction` table's primary key from `BIGINT` to `UUID`. This migration (V20) involved:

#### Migration Process
1. **Constraint Removal**: Dropped existing foreign key and primary key constraints
2. **Temporary Mapping**: Created a temporary table to map old `BIGINT` IDs to new `UUID` values
3. **Column Addition**: Added temporary `UUID` columns to affected tables
4. **Data Migration**: Populated new `UUID` columns with generated values while maintaining referential integrity
5. **Column Replacement**: Dropped old `BIGINT` columns and renamed temporary `UUID` columns
6. **Constraint Recreation**: Re-added primary key and foreign key constraints using `UUID` types
7. **Index Recreation**: Created new indexes for optimal performance

#### Affected Tables
- `payment_transaction`: Primary key changed from `id` (BIGINT) to `payment_transaction_id` (UUID)
- `payment_refund`: Foreign key `payment_transaction_id` changed from BIGINT to UUID
- `payment_audit_log`: Foreign key `payment_transaction_id` changed from BIGINT to UUID

### 2. Database Schema Updates

The database schema was updated to use UUID types for payment-related tables:

#### payment_request Table
```sql
CREATE TABLE payment_request (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    request_code VARCHAR(255) NOT NULL UNIQUE,
    payment_token VARCHAR(255) NOT NULL UNIQUE,
    -- ... other fields
);
```

#### payment_transaction Table
```sql
CREATE TABLE payment_transaction (
    payment_transaction_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_code VARCHAR(255) NOT NULL UNIQUE,
    payment_request_id UUID NOT NULL,
    -- ... other fields
    FOREIGN KEY (payment_request_id) REFERENCES payment_request(id)
);
```

#### payment_audit_log Table
```sql
CREATE TABLE payment_audit_log (
    payment_audit_log_id BIGSERIAL PRIMARY KEY,
    payment_request_id UUID,
    payment_transaction_id UUID,
    -- ... other fields
    FOREIGN KEY (payment_request_id) REFERENCES payment_request(id),
    FOREIGN KEY (payment_transaction_id) REFERENCES payment_transaction(payment_transaction_id)
);
```

### 3. JPA Entity Updates

#### PaymentRequest.java
```java
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.UUID;

@Entity
@Table(name = "payment_request")
public class PaymentRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.OTHER)
    @Column(name = "id")
    private UUID id;
    
    // ... other fields
}
```

#### PaymentTransaction.java
```java
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.UUID;

@Entity
@Table(name = "payment_transaction")
public class PaymentTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.OTHER)
    @Column(name = "payment_transaction_id")
    private UUID id;
    
    @JdbcTypeCode(SqlTypes.OTHER)
    @Column(name = "payment_request_id")
    private UUID paymentRequestId;
    
    // ... other fields
}
```

#### PaymentAuditLog.java
```java
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.UUID;

@Entity
@Table(name = "payment_audit_log")
public class PaymentAuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_audit_log_id")
    private Long id;
    
    @JdbcTypeCode(SqlTypes.OTHER)
    @Column(name = "payment_request_id")
    private UUID paymentRequestId;
    
    @JdbcTypeCode(SqlTypes.OTHER)
    @Column(name = "payment_transaction_id")
    private UUID paymentTransactionId;
    
    // ... other fields
}
```

### 4. Hibernate Configuration Updates

#### application.yml
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        type:
          preferred_uuid_jdbc_type: OTHER  # Changed from VARCHAR
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

### 5. Dependency Updates

Updated Hibernate Types Library to support UUID handling:

#### build.gradle
```gradle
dependencies {
    implementation 'io.hypersistence:hypersistence-utils-hibernate-63:3.8.3'
    // ... other dependencies
}
```

## Key Technical Considerations

### UUID Type Mapping

The `@JdbcTypeCode(SqlTypes.OTHER)` annotation is crucial for proper UUID mapping:
- It tells Hibernate to use the database's native UUID type
- Without this annotation, Hibernate defaults to VARCHAR(36) representation
- This prevents schema validation errors when the database uses actual UUID columns

### Hibernate Configuration

The `preferred_uuid_jdbc_type: OTHER` setting ensures:
- Hibernate treats UUID fields as native database UUID types
- Consistency across all UUID fields in the application
- Proper schema validation against PostgreSQL UUID columns

### Generation Strategy

For UUID primary keys, we use `GenerationType.UUID`:
- Automatically generates UUIDs for new entities
- Ensures uniqueness across distributed systems
- Provides better security than sequential IDs

## Troubleshooting

### Common Issues

1. **Schema Validation Error**: `wrong column type encountered in column [payment_request_id] in table [payment_audit_log]; found [uuid (Types#OTHER)] but expecting [varchar(36) (Types#VARCHAR)]`
   - **Solution**: Add `@JdbcTypeCode(SqlTypes.OTHER)` to UUID fields
   - **Solution**: Set `preferred_uuid_jdbc_type: OTHER` in application.yml

2. **ClassNotFoundException for Hibernate Types**
   - **Solution**: Update to `io.hypersistence:hypersistence-utils-hibernate-63:3.8.3`

3. **UUID Generation Issues**
   - **Solution**: Use `GenerationType.UUID` for UUID primary keys
   - **Solution**: Ensure database supports `gen_random_uuid()` function

4. **Entity Column Mapping Mismatch**: Application fails to start after UUID migration with JPA mapping errors
   - **Root Cause**: Entity `@Column` annotations still reference old column names after database migration
   - **Example**: `PaymentTransaction` entity mapping `id` field to `"id"` column when database uses `payment_transaction_id`
   - **Solution**: Update `@Column(name = "...")` annotations to match actual database column names
   - **Solution**: Verify all entity mappings align with migrated database schema

### Verification Steps

1. Check entity annotations are correct
2. Verify Hibernate configuration in application.yml
3. Ensure database schema matches entity definitions
4. Verify entity column mappings align with actual database column names after migrations
5. Test application startup with schema validation enabled

## Best Practices

1. **Consistent UUID Handling**: Use `@JdbcTypeCode(SqlTypes.OTHER)` for all UUID fields
2. **Configuration Alignment**: Set `preferred_uuid_jdbc_type: OTHER` globally
3. **Dependency Management**: Keep Hibernate Types Library updated
4. **Schema Validation**: Always test with `ddl-auto: validate` in production-like environments
5. **Documentation**: Update API documentation to reflect UUID types in interfaces

## Impact on Frontend

Frontend TypeScript interfaces were updated to reflect UUID changes:

```typescript
export interface PaymentRequest {
  id: string;  // Changed from number
  // ... other fields
}

export interface PaymentTransaction {
  id: string;  // Changed from number - now UUID primary key
  paymentRequestId: string;  // Changed from number
  // ... other fields
}

export interface PaymentRefund {
  paymentTransactionId: string;  // Changed from number
  // ... other fields
}

export interface PaymentAuditLog {
  paymentRequestId?: string;  // Changed from number
  paymentTransactionId?: string;  // Added - new UUID foreign key
  // ... other fields
}
```

## Migration Considerations

When migrating existing data:
1. Backup existing data before migration
2. Update foreign key references consistently
3. Test thoroughly in staging environment
4. Consider gradual rollout for production systems

### V20 Migration Specifics

The V20 migration (`V20__alter_payment_transaction_id_to_uuid.sql`) is a complex migration that:

1. **Preserves Data Integrity**: Uses a temporary mapping table to maintain relationships during the migration
2. **Handles Multiple Tables**: Updates `payment_transaction`, `payment_refund`, and `payment_audit_log` tables simultaneously
3. **Maintains Performance**: Recreates indexes after column changes to ensure optimal query performance
4. **Zero Data Loss**: Ensures all existing relationships are preserved through the UUID conversion

#### Post-Migration Steps
After running the V20 migration:
1. **Update Entity Classes**: Ensure all `@Column` annotations match the new database schema
2. **Verify Foreign Keys**: Confirm all UUID foreign key relationships are working correctly
3. **Test Application Startup**: Run the application with `ddl-auto: validate` to catch any mapping issues
4. **Performance Testing**: Verify that UUID-based queries perform as expected

## Conclusion

The UUID implementation provides better security, scalability, and follows modern best practices. The key to successful implementation is ensuring consistency between database schema, JPA entity annotations, and Hibernate configuration.