# Payment Integrator Factory Guide

## Overview

The `PaymentIntegratorFactory` has been refactored to support **gateway-aware payment method selection** with a **configurable default gateway**. This addresses the challenge where multiple payment gateways support the same payment method types (e.g., both Stripe and PayPal support CREDIT_CARD and DEBIT_CARD).

## Problem Statement

**Before Refactoring:**
```java
// Old code - only checks payment method type
PaymentIntegrator integrator = factory.getIntegrator(PaymentMethodType.CREDIT_CARD);
// Problem: Which gateway should process credit cards? Stripe or PayPal?
```

When both `StripeIntegrator` and `PayPalIntegrator` support `CREDIT_CARD`, the factory would just return the first match, leading to unpredictable behavior.

## Solution

**After Refactoring:**
```java
// Option 1: Let factory use default gateway (Stripe)
PaymentIntegrator integrator = factory.getIntegrator(PaymentMethodType.CREDIT_CARD);

// Option 2: Explicitly specify gateway
PaymentIntegrator integrator = factory.getIntegrator(PaymentMethodType.CREDIT_CARD, "PayPal");
```

## Key Changes

### 1. PaymentIntegrator Interface

Added `getGatewayName()` method to identify each integrator:

```java
public interface PaymentIntegrator {
    /**
     * Gets the gateway name for this integrator.
     * @return the gateway name (e.g., "Stripe", "PayPal", "BankTransfer")
     */
    String getGatewayName();

    boolean supports(PaymentMethodType type);
    // ... other methods
}
```

### 2. PaymentIntegratorFactory

Enhanced with gateway-aware selection logic:

```java
@Component
public class PaymentIntegratorFactory {
    private final String defaultGateway; // Default: "Stripe"

    // Method 1: Use default gateway
    public PaymentIntegrator getIntegrator(PaymentMethodType type);

    // Method 2: Specify gateway
    public PaymentIntegrator getIntegrator(PaymentMethodType type, String gateway);
}
```

### 3. Integrator Implementations

Each integrator now implements `getGatewayName()`:

**StripeIntegrator:**
```java
@Override
public String getGatewayName() {
    return "Stripe";
}

@Override
public boolean supports(PaymentMethodType type) {
    return type == PaymentMethodType.STRIPE
        || type == PaymentMethodType.CREDIT_CARD
        || type == PaymentMethodType.DEBIT_CARD;
}
```

**PayPalIntegrator:**
```java
@Override
public String getGatewayName() {
    return "PayPal";
}

@Override
public boolean supports(PaymentMethodType type) {
    return type == PaymentMethodType.PAYPAL
        || type == PaymentMethodType.CREDIT_CARD
        || type == PaymentMethodType.DEBIT_CARD;
}
```

**BankTransferIntegrator:**
```java
@Override
public String getGatewayName() {
    return "BankTransfer";
}

@Override
public boolean supports(PaymentMethodType type) {
    return type == PaymentMethodType.BANK_TRANSFER;
}
```

## Selection Logic

The factory follows this decision flow:

```
1. Find all integrators that support the payment method type
   ↓
2. If no integrators found → throw exception
   ↓
3. If gateway specified → find exact match or throw exception
   ↓
4. If only ONE integrator supports the type → use it
   ↓
5. If MULTIPLE integrators support the type:
   - Try to use default gateway (configurable)
   - If default not found, log warning and use first available
```

## Configuration

### Default Gateway Setting

Configure in `application.properties` or `application.yml`:

**Properties:**
```properties
payment.default-gateway=Stripe
```

**YAML:**
```yaml
payment:
  default-gateway: Stripe
```

**Default Value:** If not configured, defaults to `"Stripe"`

### Changing Default Gateway

To use PayPal as default:

```yaml
payment:
  default-gateway: PayPal
```

## Usage Examples

### Example 1: Generic Method with Default Gateway

```java
// Use CREDIT_CARD with default gateway (Stripe)
PaymentIntegrator integrator = factory.getIntegrator(PaymentMethodType.CREDIT_CARD);
// Returns: StripeIntegrator (because Stripe is default)

integrator.initiatePayment(request, transaction);
```

**Log Output:**
```
INFO - Multiple gateways support CREDIT_CARD, using default gateway: Stripe
```

### Example 2: Generic Method with Specific Gateway

```java
// Use CREDIT_CARD but explicitly request PayPal
PaymentIntegrator integrator = factory.getIntegrator(PaymentMethodType.CREDIT_CARD, "PayPal");
// Returns: PayPalIntegrator

integrator.initiatePayment(request, transaction);
```

**Log Output:**
```
INFO - Using specified gateway PayPal for payment method CREDIT_CARD
```

### Example 3: Gateway-Specific Method

```java
// Use STRIPE method (gateway-specific)
PaymentIntegrator integrator = factory.getIntegrator(PaymentMethodType.STRIPE);
// Returns: StripeIntegrator (only one supports STRIPE)

integrator.initiatePayment(request, transaction);
```

**Log Output:**
```
INFO - Using single available integrator Stripe for payment method STRIPE
```

### Example 4: Single-Gateway Method

```java
// Use BANK_TRANSFER (only BankTransferIntegrator supports it)
PaymentIntegrator integrator = factory.getIntegrator(PaymentMethodType.BANK_TRANSFER);
// Returns: BankTransferIntegrator (automatic, only one option)

integrator.initiatePayment(request, transaction);
```

**Log Output:**
```
INFO - Using single available integrator BankTransfer for payment method BANK_TRANSFER
```

## Payment Method Support Matrix

| Payment Method Type | Stripe | PayPal | BankTransfer | Default Gateway |
|---------------------|--------|--------|--------------|-----------------|
| CREDIT_CARD         | ✅     | ✅     | ❌           | Stripe          |
| DEBIT_CARD          | ✅     | ✅     | ❌           | Stripe          |
| STRIPE              | ✅     | ❌     | ❌           | N/A (only one)  |
| PAYPAL              | ❌     | ✅     | ❌           | N/A (only one)  |
| BANK_TRANSFER       | ❌     | ❌     | ✅           | N/A (only one)  |
| DIGITAL_WALLET      | ❌     | ❌     | ❌           | N/A (none)      |
| MANUAL              | ❌     | ❌     | ❌           | N/A (none)      |

## Integration with Service Layer

Typical usage in payment processing service:

```java
@Service
public class PaymentProcessingService {

    @Autowired
    private PaymentIntegratorFactory integratorFactory;

    public PaymentResponseDto processPayment(
            PaymentMethodType paymentMethod,
            String gateway,  // Optional, can be null
            PaymentRequestDto request,
            PaymentTransactionDto transaction) {

        // Get the appropriate integrator
        PaymentIntegrator integrator = integratorFactory.getIntegrator(paymentMethod, gateway);

        // Process payment through selected gateway
        return integrator.initiatePayment(request, transaction);
    }
}
```

## Error Handling

### Case 1: Unsupported Payment Method

```java
try {
    PaymentIntegrator integrator = factory.getIntegrator(PaymentMethodType.DIGITAL_WALLET);
} catch (NoSuchElementException e) {
    // Exception: "No integrator found for payment method type: DIGITAL_WALLET"
}
```

### Case 2: Invalid Gateway Combination

```java
try {
    // BANK_TRANSFER only supported by BankTransferIntegrator
    PaymentIntegrator integrator = factory.getIntegrator(
        PaymentMethodType.BANK_TRANSFER, "Stripe");
} catch (NoSuchElementException e) {
    // Exception: "No integrator found for payment method type: BANK_TRANSFER with gateway: Stripe"
}
```

### Case 3: Missing Default Gateway

If default gateway is misconfigured:

```yaml
payment:
  default-gateway: UnknownGateway
```

```java
PaymentIntegrator integrator = factory.getIntegrator(PaymentMethodType.CREDIT_CARD);
// Falls back to first available (StripeIntegrator or PayPalIntegrator)
```

**Log Output:**
```
WARN - Default gateway UnknownGateway not found for type CREDIT_CARD, falling back to: Stripe
```

## Testing

### Unit Test Example

```java
@Test
void testGetIntegratorWithSpecificGateway() {
    // Arrange
    StripeIntegrator stripeIntegrator = new StripeIntegrator(restTemplate);
    PayPalIntegrator paypalIntegrator = new PayPalIntegrator(restTemplate);

    PaymentIntegratorFactory factory = new PaymentIntegratorFactory(
        Arrays.asList(stripeIntegrator, paypalIntegrator),
        "Stripe"  // default gateway
    );

    // Act - Request PayPal explicitly
    PaymentIntegrator result = factory.getIntegrator(
        PaymentMethodType.CREDIT_CARD, "PayPal");

    // Assert
    assertEquals("PayPal", result.getGatewayName());
}

@Test
void testGetIntegratorWithDefaultGateway() {
    // Arrange
    StripeIntegrator stripeIntegrator = new StripeIntegrator(restTemplate);
    PayPalIntegrator paypalIntegrator = new PayPalIntegrator(restTemplate);

    PaymentIntegratorFactory factory = new PaymentIntegratorFactory(
        Arrays.asList(stripeIntegrator, paypalIntegrator),
        "Stripe"  // default gateway
    );

    // Act - Don't specify gateway, should use default (Stripe)
    PaymentIntegrator result = factory.getIntegrator(PaymentMethodType.CREDIT_CARD);

    // Assert
    assertEquals("Stripe", result.getGatewayName());
}
```

## Logging

The factory provides detailed logging at startup and during runtime:

**Startup Logs:**
```
INFO - PaymentIntegratorFactory initialized with 3 integrators, default gateway: Stripe
INFO -   - Stripe supports: STRIPE, CREDIT_CARD, DEBIT_CARD
INFO -   - PayPal supports: PAYPAL, CREDIT_CARD, DEBIT_CARD
INFO -   - BankTransfer supports: BANK_TRANSFER
```

**Runtime Logs:**
```
DEBUG - Finding integrator for type: CREDIT_CARD, gateway: null
INFO  - Multiple gateways support CREDIT_CARD, using default gateway: Stripe

DEBUG - Finding integrator for type: CREDIT_CARD, gateway: PayPal
INFO  - Using specified gateway PayPal for payment method CREDIT_CARD

DEBUG - Finding integrator for type: BANK_TRANSFER, gateway: null
INFO  - Using single available integrator BankTransfer for payment method BANK_TRANSFER
```

## Migration Guide

### For Existing Code

**Before:**
```java
PaymentIntegrator integrator = factory.getIntegrator(PaymentMethodType.CREDIT_CARD);
```

**After (no change required):**
```java
// Still works! Uses default gateway (Stripe)
PaymentIntegrator integrator = factory.getIntegrator(PaymentMethodType.CREDIT_CARD);
```

**Optional Enhancement:**
```java
// Now you can specify gateway if needed
PaymentIntegrator integrator = factory.getIntegrator(
    PaymentMethodType.CREDIT_CARD,
    requestedGateway  // "Stripe" or "PayPal"
);
```

## Best Practices

1. **Use default gateway for most cases:**
   ```java
   factory.getIntegrator(PaymentMethodType.CREDIT_CARD)
   ```

2. **Specify gateway when user explicitly chooses:**
   ```java
   factory.getIntegrator(PaymentMethodType.CREDIT_CARD, userSelectedGateway)
   ```

3. **Use gateway-specific methods when locked to one provider:**
   ```java
   factory.getIntegrator(PaymentMethodType.STRIPE)  // Always Stripe
   ```

4. **Configure default gateway based on business preference:**
   - Lower fees → use that gateway as default
   - Better reliability → use that gateway as default
   - Business partnership → use that gateway as default

5. **Log gateway selection for auditing:**
   - Factory automatically logs which gateway is selected
   - Useful for troubleshooting and analytics

## Summary

The refactored `PaymentIntegratorFactory`:

✅ Supports multiple gateways for the same payment method
✅ Configurable default gateway via `payment.default-gateway`
✅ Explicit gateway selection when needed
✅ Backward compatible with existing code
✅ Comprehensive logging for debugging
✅ Clear error messages for unsupported combinations
✅ Fallback mechanism if default gateway not available

This design provides **flexibility** while maintaining **simplicity** for common use cases.
