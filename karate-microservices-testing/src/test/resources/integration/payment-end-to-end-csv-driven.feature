Feature: End-to-end payment success flow - CSV Data Driven

  Background:
    * def base = karate.get('baseUrl') || java.lang.System.getenv('BASE_URL') || 'http://localhost:8080'
    * url base
    * def login = callonce read('classpath:common/auth/login.feature')
    * def auth = { token: #(login.token) }
    * configure headers = read('classpath:common/headers/common-headers.js')
    * def headersFn = read('classpath:common/headers/common-headers.js')
    * def headersPreview = headersFn()
    * print 'Computed headers preview:', headersPreview
    * def utils = karate.get('utils')
    * configure retry = { count: 20, interval: 1000 }

    # Read test data from CSV file
    * def testData = read('classpath:integration/data/payment-scenarios.csv')
    * print 'Loaded test scenarios:', testData.length, 'scenarios'

    @e2e @payments @success @csv-driven
  Scenario: Execute all payment scenarios from CSV
    # Loop through each test scenario
    * def executeTest =
      """
      function(scenario) {
        var result = karate.call('classpath:common/helpers/payment-end-to-end-scenario.feature', {
          testCase: scenario.testCase,
          paymentMethod: scenario.paymentMethod,
          gateway: scenario.gateway,
          allowedPaymentMethods: JSON.parse(scenario.allowedPaymentMethods),
          paymentMethodDetails: JSON.parse(scenario.paymentMethodDetails),
          expectedStatus: scenario.expectedStatus,
          description: scenario.description,
          auth: auth,
          headers: headersPreview
        });
        return result;
      }
      """
    * def results = karate.map(testData, executeTest)
    * print 'All scenarios executed:', results.length
    * match each results contains { response: '#notnull' }
