Feature: Load test — create 100 users

@perf
Scenario: burst
  * def createUser = function(){ return call read('../api/users.feature@smoke') }
  * def result = karate.repeat(100, createUser)
  * match each result contains { status: 201 }