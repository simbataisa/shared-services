Feature: User creation emits Kafka event

Background:
  * def KafkaHelper = Java.type('com.ahss.automation.helpers.KafkaHelper')
  * def kafka = new KafkaHelper()

@async
Scenario: Create user -> consume event
  Given url baseUrl + '/users'
  And request { name: 'Jane Doe', email: 'jane@example.com' }
  When method post
  Then status 201
  * def userId = response.id
  * def event = kafka.consumeMessage('user-events-topic', 10000)
  * match event.userId == userId
  * match event.eventType == 'USER_CREATED'