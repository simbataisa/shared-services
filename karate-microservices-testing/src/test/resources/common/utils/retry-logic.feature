Feature: Shared retry logic example

  Background:
    * configure retry = { count: 5, interval: 1000 }

    @name=retryableGet
  Scenario: Retry GET on transient failures
    Given url baseUrl + '/health'
    When method get
    Then status 200