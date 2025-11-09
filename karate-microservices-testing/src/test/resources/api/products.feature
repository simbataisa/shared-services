Feature: Products API

  Background:
    * def base = karate.get('baseUrl') || java.lang.System.getenv('BASE_URL') || 'http://localhost:8080'
    * url base
    * def login = callonce read('classpath:common/auth/login.feature')
    * def auth = { token: #(login.token) }
    * configure headers = read('classpath:common/headers/common-headers.js')

  Scenario: Get all active products
    Given path '/api/v1/products'
    When method get
    Then status 200
    And match response.success == true

  Scenario: Get product by ID (if one exists)
    Given path '/api/v1/products'
    When method get
    Then status 200
    * def items = response.data.content ? response.data.content : response.data
    * def firstId = items && items.length > 0 ? items[0].id : null
    * print 'First product id:', firstId
    * if (!firstId) karate.abort()
    Given path '/api/v1/products', firstId
    When method get
    Then status 200
    And match response.success == true