env ?= dev
threads ?= 5

test:
	./karate-microservices-testing/gradlew -q -p karate-microservices-testing test -Dkarate.env=$(env) -Dthreads=$(threads)

test-smoke:
	./karate-microservices-testing/gradlew -q -p karate-microservices-testing testSmoke -Dkarate.env=$(env) -Dthreads=$(threads)

test-contract:
	./karate-microservices-testing/gradlew -q -p karate-microservices-testing testContract -Dkarate.env=$(env)

test-perf:
	./karate-microservices-testing/gradlew -q -p karate-microservices-testing testPerf -Dkarate.env=$(env)

mock:
	./karate-microservices-testing/gradlew -q -p karate-microservices-testing mockStart -Dmock.block.ms=600000