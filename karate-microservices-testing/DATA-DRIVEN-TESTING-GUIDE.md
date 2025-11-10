# Data-Driven Testing Guide for Payment E2E Tests

## Overview

This guide explains how to use data-driven testing for payment end-to-end scenarios. We provide three different approaches to suit different needs.

## Approaches

### 1. Scenario Outline with Inline Examples (Simplest)

**File:** `payment-end-to-end-data-driven.feature`

**Best for:**
- Small number of test cases (3-10)
- Test data that doesn't change often
- Quick visibility of test cases in the feature file

**Usage:**
```bash
./gradlew test --tests "*CustomRunnerTest" \
  -Dkarate.options="classpath:integration/payment-end-to-end-data-driven.feature"
```

**Example:**
```gherkin
Scenario Outline: <testCase> - Create payment request with <paymentMethod> via <gateway>
  # Test steps here...

  Examples:
    | testCase           | paymentMethod | gateway      | allowedPaymentMethods | paymentMethodDetails      | expectedStatus |
    | stripe_credit_card | CREDIT_CARD   | Stripe       | ["STRIPE"]            | {"stripeToken":"tok_visa"}| COMPLETED      |
    | paypal_credit_card | CREDIT_CARD   | PayPal       | ["PAYPAL"]            | {"paypalOrderId":"ORD123"}| COMPLETED      |
    | bank_transfer      | BANK_TRANSFER | BankTransfer | ["BANK_TRANSFER"]     | {"accountNumber":"12345"} | COMPLETED      |
```

**Pros:**
- ✅ All test data visible in the feature file
- ✅ Easy to add/modify test cases
- ✅ Native Karate syntax
- ✅ Each scenario runs independently

**Cons:**
- ❌ Can become verbose with many test cases
- ❌ Not ideal for dynamic test data

---

### 2. JSON Data-Driven (Recommended)

**Files:**
- `payment-end-to-end-json-driven.feature` (driver)
- `payment-end-to-end-scenario.feature` (reusable scenario)
- `data/payment-scenarios.json` (test data)

**Best for:**
- Medium to large number of test cases (10+)
- Test data that needs to be maintained separately
- Complex paymentMethodDetails objects
- Sharing test data across multiple features

**Usage:**
```bash
./gradlew test --tests "*CustomRunnerTest" \
  -Dkarate.options="classpath:integration/payment-end-to-end-json-driven.feature"
```

**Test Data Structure (`payment-scenarios.json`):**
```json
[
  {
    "testCase": "stripe_credit_card",
    "paymentMethod": "CREDIT_CARD",
    "gateway": "Stripe",
    "allowedPaymentMethods": ["STRIPE"],
    "paymentMethodDetails": {
      "stripeToken": "tok_visa"
    },
    "expectedStatus": "COMPLETED",
    "description": "Stripe credit card payment with valid Visa token"
  }
]
```

**Pros:**
- ✅ Clean separation of test data and test logic
- ✅ Easy to maintain large datasets
- ✅ Supports complex nested objects
- ✅ Can be generated programmatically
- ✅ Reusable across multiple test suites

**Cons:**
- ❌ Test data not immediately visible in feature file
- ❌ All scenarios run in a single test execution

---

### 3. CSV Data-Driven

**Files:**
- `payment-end-to-end-csv-driven.feature` (driver)
- `payment-end-to-end-scenario.feature` (reusable scenario)
- `data/payment-scenarios.csv` (test data)

**Best for:**
- Non-technical users managing test data
- Integration with Excel or Google Sheets
- Simple test data structures

**Usage:**
```bash
./gradlew test --tests "*CustomRunnerTest" \
  -Dkarate.options="classpath:integration/payment-end-to-end-csv-driven.feature"
```

**Test Data Structure (`payment-scenarios.csv`):**
```csv
testCase,paymentMethod,gateway,allowedPaymentMethods,paymentMethodDetails,expectedStatus,description
stripe_credit_card,CREDIT_CARD,Stripe,"[""STRIPE""]","{""stripeToken"":""tok_visa""}",COMPLETED,Stripe credit card payment
```

**Pros:**
- ✅ Can be edited in Excel/Google Sheets
- ✅ Easy for non-technical users
- ✅ Simple format

**Cons:**
- ❌ Difficult to handle complex nested JSON
- ❌ Requires JSON string escaping for complex objects
- ❌ Less readable for complex data

---

## Comparison Matrix

| Feature | Scenario Outline | JSON-Driven | CSV-Driven |
|---------|-----------------|-------------|------------|
| **Ease of Setup** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ |
| **Maintainability** | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| **Complex Data Support** | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐ |
| **Non-Technical Friendly** | ⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **Scalability** | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| **Test Independence** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ |
| **Debugging** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ |

---

## Supported Payment Scenarios

### Credit Card via Stripe
```json
{
  "testCase": "stripe_credit_card",
  "paymentMethod": "CREDIT_CARD",
  "gateway": "Stripe",
  "allowedPaymentMethods": ["STRIPE"],
  "paymentMethodDetails": {
    "stripeToken": "tok_visa"
  },
  "expectedStatus": "COMPLETED"
}
```

### Credit Card via PayPal
```json
{
  "testCase": "paypal_credit_card",
  "paymentMethod": "CREDIT_CARD",
  "gateway": "PayPal",
  "allowedPaymentMethods": ["PAYPAL"],
  "paymentMethodDetails": {
    "paypalOrderId": "ORDER123"
  },
  "expectedStatus": "COMPLETED"
}
```

### Bank Transfer
```json
{
  "testCase": "bank_transfer",
  "paymentMethod": "BANK_TRANSFER",
  "gateway": "BankTransfer",
  "allowedPaymentMethods": ["BANK_TRANSFER"],
  "paymentMethodDetails": {
    "accountNumber": "1234567890",
    "routingNumber": "987654321",
    "accountHolderName": "John Doe",
    "accountType": "CHECKING"
  },
  "expectedStatus": "COMPLETED"
}
```

---

## Adding New Test Scenarios

### For Scenario Outline Approach

Edit `payment-end-to-end-data-driven.feature` and add a new row to the Examples table:

```gherkin
Examples:
  | testCase              | paymentMethod | gateway | allowedPaymentMethods | paymentMethodDetails | expectedStatus |
  | new_test_case         | CREDIT_CARD   | Stripe  | ["STRIPE"]            | {"token":"tok_new"}  | COMPLETED      |
```

### For JSON-Driven Approach

Edit `data/payment-scenarios.json` and add a new object:

```json
[
  {
    "testCase": "new_test_case",
    "paymentMethod": "CREDIT_CARD",
    "gateway": "Stripe",
    "allowedPaymentMethods": ["STRIPE"],
    "paymentMethodDetails": {
      "stripeToken": "tok_new"
    },
    "expectedStatus": "COMPLETED",
    "description": "New test scenario description"
  }
]
```

### For CSV-Driven Approach

Edit `data/payment-scenarios.csv` and add a new row:

```csv
new_test_case,CREDIT_CARD,Stripe,"[""STRIPE""]","{""stripeToken"":""tok_new""}",COMPLETED,New test scenario
```

---

## Running Specific Test Cases

### Run Single Scenario from JSON/CSV

You can filter which scenarios to run by modifying the driver feature:

```gherkin
# In payment-end-to-end-json-driven.feature
* def testData = read('classpath:integration/data/payment-scenarios.json')
* def filteredData = karate.filter(testData, function(x){ return x.gateway == 'Stripe' })
* def results = karate.map(filteredData, executeTest)
```

### Run with Tags

```bash
# Run only data-driven tests
./gradlew test --tests "*CustomRunnerTest" \
  -Dkarate.options="--tags @data-driven"

# Run only JSON-driven tests
./gradlew test --tests "*CustomRunnerTest" \
  -Dkarate.options="--tags @json-driven"
```

---

## Best Practices

1. **Use descriptive testCase names**: `stripe_visa_success` instead of `test1`

2. **Keep paymentMethodDetails consistent**: Use the same structure for the same gateway

3. **Add descriptions**: Always include a human-readable description

4. **Version your test data**: Commit test data files to git

5. **Validate test data**: Consider adding a schema validation step

6. **Use environment-specific data**: Different data for QA/Staging/Prod

7. **Handle failures gracefully**: Add retry logic for flaky scenarios

---

## Troubleshooting

### Scenario Outline Not Running

**Problem:** Scenarios with Examples not executing

**Solution:** Ensure the Examples table is properly formatted with `|` separators

### JSON Parsing Errors

**Problem:** `paymentMethodDetails` not parsing correctly

**Solution:** Verify JSON is valid and properly escaped in CSV files

### All Scenarios Failing

**Problem:** Authentication or setup issues

**Solution:** Check Background section is executing correctly and auth token is valid

### Mock Server Not Responding

**Problem:** Gateway URLs returning 404

**Solution:** Ensure mock server is running and URLs match the gateway prefixes

---

## Advanced Usage

### Dynamic Test Data Generation

```javascript
// Generate test data programmatically
* def generateTestData =
  """
  function() {
    var gateways = ['Stripe', 'PayPal'];
    var cards = ['tok_visa', 'tok_mastercard', 'tok_amex'];
    var scenarios = [];

    gateways.forEach(function(gateway) {
      cards.forEach(function(card) {
        scenarios.push({
          testCase: gateway.toLowerCase() + '_' + card,
          paymentMethod: 'CREDIT_CARD',
          gateway: gateway,
          allowedGateways: gateway.toUpperCase(),
          paymentMethodDetails: { stripeToken: card },
          expectedStatus: 'COMPLETED'
        });
      });
    });

    return scenarios;
  }
  """
* def testData = generateTestData()
```

### Conditional Test Execution

```javascript
// Skip certain tests based on environment
* def testData = read('classpath:integration/data/payment-scenarios.json')
* def env = karate.env
* def filteredData = karate.filter(testData, function(x){
    // Skip PayPal tests in 'qa' environment
    if (env == 'qa' && x.gateway == 'PayPal') return false;
    return true;
  })
```

---

## Recommendation

**For most use cases, we recommend the JSON-Driven approach** because it provides:
- Clean separation of concerns
- Easy maintenance
- Good scalability
- Support for complex data structures
- Reusability across test suites

Start with JSON-Driven unless you have specific reasons to use Scenario Outline (few test cases) or CSV (non-technical users managing data).
