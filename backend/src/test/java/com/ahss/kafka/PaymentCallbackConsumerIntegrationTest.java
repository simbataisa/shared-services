package com.ahss.kafka;

import com.ahss.kafka.consumer.PaymentCallbackConsumer;
import com.ahss.kafka.event.PaymentCallbackEvent;
import com.ahss.kafka.event.PaymentCallbackType;
import com.ahss.saga.PaymentSagaOrchestrator;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.qameta.allure.Allure;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;

@SpringBootTest
class PaymentCallbackConsumerIntegrationTest {

    @MockBean
    private PaymentSagaOrchestrator orchestrator;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentCallbackConsumer consumer;

    @Test
    void consumes_callback_event_and_invokes_orchestrator() throws Exception {
        // Build a callback event and serialize as JSON string
        PaymentCallbackEvent event = Allure.step("Create PaymentCallbackEvent", PaymentCallbackEvent::new);
        event.setType(PaymentCallbackType.REQUEST_APPROVED);
        event.setCorrelationId("corr-consume-123");
        event.setPaymentRequestId(UUID.randomUUID());
        event.setGatewayName("TestGateway");
        event.setReceivedAt(LocalDateTime.now());
        String json = objectMapper.writeValueAsString(event);

        // Invoke the consumer handler directly (bypassing Kafka for this test)
        Allure.step("Invoke PaymentCallbackConsumer.onMessage", () -> consumer.onMessage(json));

        // Verify the orchestrator eventually handles the event
        Mockito.verify(orchestrator, timeout(5000)).handle(any(PaymentCallbackEvent.class));
    }
}