package com.ahss.integration;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Arrays;
import java.util.List;

/**
 * Simple factory to select a parser implementation based on payload structure.
 */
public class MessageParserFactory {

    private static final List<MessageParser> PARSERS = Arrays.asList(
            new com.ahss.integration.stripe.StripeMessageParser(),
            new com.ahss.integration.paypal.PayPalMessageParser(),
            new com.ahss.integration.bank.BankTransferMessageParser());

    public static MessageParser forPayload(JsonNode root) {
        for (MessageParser parser : PARSERS) {
            if (parser.supports(root)) {
                return parser;
            }
        }
        return null;
    }
}