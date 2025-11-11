Feature: Dashboard API

  Background:
    * def base = karate.get('baseUrl') || java.lang.System.getenv('BASE_URL') || 'http://localhost:8080'
    * url base
    * def login = callonce read('classpath:common/auth/login.feature')
    * def auth = { token: #(login.token) }
    * configure headers = read('classpath:common/headers/common-headers.js')
    # Log the headers that will be sent to the backend
    * def headersFn = read('classpath:common/headers/common-headers.js')
    * def headersPreview = headersFn()
    * print 'Dashboard headers before request:', headersPreview

  Scenario: Get dashboard stats
    Given path '/api/v1/dashboard/stats'
    When method get
    Then status 200
    And match response.success == true