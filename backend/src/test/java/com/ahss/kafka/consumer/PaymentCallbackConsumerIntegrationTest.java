package com.ahss.kafka.consumer;

import com.ahss.kafka.event.PaymentCallbackEvent;
import com.ahss.kafka.event.PaymentCallbackType;
import com.ahss.saga.PaymentSagaOrchestrator;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;

@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
@Epic("Saga")
@Feature("Payment Callback Consumer")
class PaymentCallbackConsumerIntegrationTest {

    @MockBean
    private PaymentSagaOrchestrator orchestrator;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentCallbackConsumer consumer;

    @Test
    @DisplayName("Consumes callback event and invokes orchestrator")
    @Story("Payment Callback Consumer")
    void consumes_callback_event_and_invokes_orchestrator() throws Exception {
        // Build a callback event and serialize as JSON string
        PaymentCallbackEvent event = Allure.step("Create PaymentCallbackEvent", PaymentCallbackEvent::new);
        event.setType(PaymentCallbackType.REQUEST_APPROVED);
        event.setCorrelationId("corr-consume-123");
        event.setPaymentRequestId(UUID.randomUUID());
        event.setGatewayName("TestGateway");
        event.setReceivedAt(LocalDateTime.now());
        String json = objectMapper.writeValueAsString(event);
        Allure.addAttachment("Sent Callback Event", "application/json", json);

        // Invoke the consumer handler directly (bypassing Kafka for this test)
        Allure.step("Invoke PaymentCallbackConsumer.onMessage", () -> consumer.onMessage(json));

        // Verify the orchestrator eventually handles the event
        ArgumentCaptor<PaymentCallbackEvent> captor = ArgumentCaptor.forClass(PaymentCallbackEvent.class);
        Mockito.verify(orchestrator, timeout(5000)).handle(captor.capture());
        Allure.addAttachment("Received Callback Event", "application/json", objectMapper.writeValueAsString(captor.getValue()));
    }

    @Test
    @DisplayName("Invalid JSON does not invoke orchestrator")
    @Story("Payment Callback Consumer")
    void invalid_json_is_ignored() {
        String malformed = "{not-json}";
        Allure.addAttachment("Malformed Callback Payload", "text/plain", malformed);
        Allure.step("Invoke PaymentCallbackConsumer.onMessage with malformed payload",
                () -> consumer.onMessage(malformed));
        Mockito.verify(orchestrator, timeout(500).times(0)).handle(any());
    }
}