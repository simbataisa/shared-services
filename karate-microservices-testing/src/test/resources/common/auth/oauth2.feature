# common/auth/oauth2.feature
Feature: OAuth2 Client Credentials

Background:
  * url karate.get('authBaseUrl', karate.get('baseUrl') + '/auth')

@name=getAccessToken
Scenario: Obtain access token
  Given path '/oauth/token'
  And form field grant_type = 'client_credentials'
  And form field client_id = karate.get('clientId', karate.properties['CLIENT_ID'] || java.lang.System.getenv('CLIENT_ID'))
  And form field client_secret = karate.get('clientSecret', karate.properties['CLIENT_SECRET'] || java.lang.System.getenv('CLIENT_SECRET'))
  When method post
  Then status 200
  * def accessToken = response.access_token