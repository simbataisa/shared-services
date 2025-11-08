package com.ahss.integration.webhook.parser;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Arrays;
import java.util.List;

/**
 * Simple factory to select a parser implementation based on payload structure.
 */
public class WebhookMessageParserFactory {

    private static final List<WebhookMessageParser> PARSERS = Arrays.asList(
            new StripeWebhookMessageParser(),
            new PayPalWebhookMessageParser());

    public static WebhookMessageParser forPayload(JsonNode root) {
        for (WebhookMessageParser parser : PARSERS) {
            if (parser.supports(root)) {
                return parser;
            }
        }
        return null;
    }
}