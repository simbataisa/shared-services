package com.ahss.kafka;

import com.ahss.kafka.event.PaymentDomainEvent;
import com.ahss.kafka.producer.PaymentEventProducer;
import io.qameta.allure.Allure;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@EmbeddedKafka(
    partitions = 3,
    topics = {"payment-events"})
@TestPropertySource(
    properties = {
      "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
      "app.kafka.topics.payment-events=payment-events"
    })
class PaymentEventProducerIntegrationTest {

  @Autowired private PaymentEventProducer eventProducer;

  @Autowired private EmbeddedKafkaBroker embeddedKafka;

  @Test
  void sends_domain_event_to_payment_events_topic() {
    // Create a consumer bound to the embedded Kafka
    Map<String, Object> props =
        Allure.step(
            "Create consumer properties",
            () -> KafkaTestUtils.consumerProps("eventProducerTestGroup", "true", embeddedKafka));
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    Consumer<String, String> consumer =
        Allure.step(
            "Create consumer",
            () -> new DefaultKafkaConsumerFactory<String, String>(props).createConsumer());
    Allure.step(
        "Consume from embedded topic",
        () -> embeddedKafka.consumeFromAnEmbeddedTopic(consumer, "payment-events"));

    // Send a domain event
    String correlationId = "corr-123";
    PaymentDomainEvent evt =
        Allure.step(
            "Create domain event",
            () -> new PaymentDomainEvent("test.type", correlationId, Map.of("foo", "bar")));
    Allure.step(
        "Send domain event",
        () -> eventProducer.send(evt));

    // Verify the record was produced
    var record =
        Allure.step(
            "Get single record from consumer",
            () -> KafkaTestUtils.getSingleRecord(consumer, "payment-events"));
    Allure.step(
        "Verify record key",
        () -> assertEquals(correlationId, record.key()));
    Allure.step(
        "Verify record value",
        () -> {
          String payload = record.value();
          assertTrue(payload.contains("\"type\":\"test.type\""));
          assertTrue(payload.contains("\"correlationId\":\"" + correlationId + "\""));
        });
  }
}
