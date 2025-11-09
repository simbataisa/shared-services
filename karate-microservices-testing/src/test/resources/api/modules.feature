Feature: Modules API

  Background:
    * def base = karate.get('baseUrl') || java.lang.System.getenv('BASE_URL') || 'http://localhost:8080'
    * url base
    * def login = callonce read('classpath:common/auth/login.feature')
    * def auth = { token: #(login.token) }
    * configure headers = read('classpath:common/headers/common-headers.js')

  Scenario: Get all modules
    Given path '/api/v1/modules'
    When method get
    Then status 200
    And match response.success == true

  Scenario: Get modules by product ID (if product exists)
    Given path '/api/v1/products'
    When method get
    Then status 200
    * def items = response.data.content ? response.data.content : response.data
    * def productId = items && items.length > 0 ? items[0].id : null
    * print 'Product id for modules:', productId
    * if (!productId) karate.abort()
    Given path '/api/v1/modules/product', productId
    When method get
    Then status 200
    And match response.success == true