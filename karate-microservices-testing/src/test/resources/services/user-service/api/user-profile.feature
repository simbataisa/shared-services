Feature: User Profile API

Background:
  * def token = call read('classpath:common/auth/oauth2.feature@getAccessToken')
  * header Authorization = 'Bearer ' + token.accessToken

Scenario: Get user profile
  Given url baseUrl + '/users/1/profile'
  When method get
  Then status 200
  And match response contains { id: 1 }