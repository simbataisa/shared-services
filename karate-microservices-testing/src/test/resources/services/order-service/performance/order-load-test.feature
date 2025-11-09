Feature: Order load test — create 50 orders

@perf
Scenario: burst orders
  * def createOrder = function(){ return call read('../api/orders.feature@smoke') }
  * def result = karate.repeat(50, createOrder)
  * match each result contains { status: 201 }