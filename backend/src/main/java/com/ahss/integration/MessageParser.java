package com.ahss.integration;

import com.ahss.kafka.event.PaymentCallbackEvent;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Strategy interface for parsing webhook payloads into PaymentCallbackEvent.
 */
public interface MessageParser {

    /**
     * Determines whether this parser supports the given payload structure.
     */
    boolean supports(JsonNode root);

    /**
     * Parses the payload into a PaymentCallbackEvent.
     */
    PaymentCallbackEvent parse(JsonNode root);
}