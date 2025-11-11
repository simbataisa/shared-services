Feature: Roles API

  Background:
    * def base = karate.get('baseUrl') || java.lang.System.getenv('BASE_URL') || 'http://localhost:8080'
    * url base
    * def login = callonce read('classpath:common/auth/login.feature')
    * def auth = { token: #(login.token) }
    * configure headers = read('classpath:common/headers/common-headers.js')

  Scenario: Get all roles
    Given path '/api/v1/roles'
    When method get
    Then status 200
    And match response.success == true