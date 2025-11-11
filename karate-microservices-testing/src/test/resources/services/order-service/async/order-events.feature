Feature: Order creation emits Kafka event

Background:
  * def KafkaHelper = Java.type('com.ahss.automation.helpers.KafkaHelper')
  * def kafka = new KafkaHelper()

@async
Scenario: Create order -> consume event
  Given url baseUrl + '/orders'
  And request { userId: 1, sku: 'ABC-123', quantity: 1 }
  When method post
  Then status 201
  * def orderId = response.id
  * def event = kafka.consumeMessage('order-events-topic', 10000)
  * match event.orderId == orderId
  * match event.eventType == 'ORDER_CREATED'