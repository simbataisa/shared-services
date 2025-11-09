Feature: Tenants API

  Background:
    * def base = karate.get('baseUrl') || java.lang.System.getenv('BASE_URL') || 'http://localhost:8080'
    * url base
    * def login = callonce read('classpath:common/auth/login.feature')
    * def auth = { token: #(login.token) }
    * configure headers = read('classpath:common/headers/common-headers.js')

  Scenario: Get all tenants
    Given path '/api/v1/tenants'
    When method get
    Then status 200
    And match response.success == true

  Scenario: Create, read, update, status patch, and delete a tenant
    * def code = 'TEN_' + java.util.UUID.randomUUID()
    * def payload = { code: #(code), name: #('Tenant ' + code), type: 'BUSINESS_IN', status: 'ACTIVE' }
    Given path '/api/v1/tenants'
    And request payload
    * print 'Create tenant request payload:', payload
    When method post
    * print 'Create tenant response:', response
    Then status 201
    And match response.success == true
    * def tenantId = response.data.id

    # Read
    Given path '/api/v1/tenants', tenantId
    When method get
    Then status 200
    And match response.data.id == tenantId

    # Update
    * def updated = { code: #(code), name: #('Tenant Updated ' + code), type: 'BUSINESS_IN', status: 'ACTIVE' }
    Given path '/api/v1/tenants', tenantId
    And request updated
    When method put
    Then status 200
    And match response.success == true

    # Patch status
    Given path '/api/v1/tenants', tenantId, 'status'
    And request { status: 'INACTIVE' }
    When method patch
    Then status 200
    And match response.success == true

    # Delete
    Given path '/api/v1/tenants', tenantId
    When method delete
    Then status 200
    And match response.success == true