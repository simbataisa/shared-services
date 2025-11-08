package com.ahss.config;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;

import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
@EmbeddedKafka(partitions = 3)
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "app.kafka.topics.payment-callbacks=payment-callbacks",
        "app.kafka.topics.payment-events=payment-events"
})
@Epic("Saga")
@Feature("Configuration")
class KafkaConfigIntegrationTest {

    @org.springframework.beans.factory.annotation.Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Test
    @DisplayName("Topics are created by admin client")
    @Story("Kafka Configuration")
    void topics_are_created_by_admin() throws Exception {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafka.getBrokersAsString());
        try (AdminClient admin = AdminClient.create(props)) {
            Set<String> names = admin.listTopics().names().get(10, TimeUnit.SECONDS);
            assertTrue(names.contains("payment-callbacks"));
            assertTrue(names.contains("payment-events"));
        }
    }
}