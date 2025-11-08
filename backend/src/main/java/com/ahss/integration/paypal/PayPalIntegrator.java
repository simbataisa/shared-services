package com.ahss.integration.paypal;

import com.ahss.dto.response.PaymentRequestDto;
import com.ahss.dto.response.PaymentResponseDto;
import com.ahss.dto.response.PaymentTransactionDto;
import com.ahss.enums.PaymentMethodType;
import com.ahss.integration.PaymentIntegrator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

/**
 * Implementation of PaymentIntegrator for PayPal processor.
 * Handles conversion and outbound calls to PayPal REST APIs.
 */
@Component
public class PayPalIntegrator implements PaymentIntegrator {

    private final RestTemplate restTemplate;
    private final String paymentApiUrl; // Configurable for tests
    private final String refundApiUrl; // Configurable for tests

    @org.springframework.beans.factory.annotation.Autowired
    public PayPalIntegrator(
            RestTemplate restTemplate,
            @Value("${paypal.paymentApiUrl:https://api-m.paypal.com/v2/checkout/orders}") String paymentApiUrl,
            @Value("${paypal.refundApiUrl:https://api-m.paypal.com/v2/payments/captures/{capture_id}/refund}") String refundApiUrl
    ) {
        this.restTemplate = restTemplate;
        this.paymentApiUrl = paymentApiUrl;
        this.refundApiUrl = refundApiUrl;
    }

    // Backwards-compatible constructor for existing unit tests
    public PayPalIntegrator(RestTemplate restTemplate) {
        this(restTemplate,
                "https://api-m.paypal.com/v2/checkout/orders",
                "https://api-m.paypal.com/v2/payments/captures/{capture_id}/refund");
    }

    @Override
    public boolean supports(PaymentMethodType type) {
        return type == PaymentMethodType.PAYPAL;
    }

    @Override
    public PaymentResponseDto initiatePayment(PaymentRequestDto request, PaymentTransactionDto transaction) {
        PayPalOrderRequest externalRequest = convertToPayPalOrderRequest(request, transaction);
        PayPalOrderResponse externalResponse = restTemplate.postForObject(paymentApiUrl, externalRequest, PayPalOrderResponse.class);
        return convertToPaymentResponse(externalResponse, request, transaction);
    }

    @Override
    public PaymentResponseDto processRefund(PaymentTransactionDto transaction, BigDecimal refundAmount) {
        // Placeholder refund flow
        PayPalRefundRequest refundRequest = convertToRefundRequest(transaction, refundAmount);
        // Expand capture_id from transaction external ID when present
        PayPalRefundResponse refundResponse = restTemplate.postForObject(
                refundApiUrl,
                refundRequest,
                PayPalRefundResponse.class,
                transaction.getExternalTransactionId()
        );
        return convertRefundToPaymentResponse(refundResponse, transaction);
    }

    @Override
    public PaymentResponseDto tokenizeCard(Object cardDetails) {
        throw new UnsupportedOperationException("Tokenization not supported for PayPal");
    }

    private PayPalOrderRequest convertToPayPalOrderRequest(PaymentRequestDto request, PaymentTransactionDto transaction) {
        return new PayPalOrderRequest(); // Map fields as needed
    }

    private PaymentResponseDto convertToPaymentResponse(PayPalOrderResponse externalResponse, PaymentRequestDto request, PaymentTransactionDto transaction) {
        PaymentResponseDto resp = new PaymentResponseDto();
        resp.setSuccess(true); // Placeholder
        resp.setStatus("CREATED");
        resp.setMessage("PayPal order created");
        resp.setGatewayName("PayPal");
        resp.setExternalTransactionId(null); // Map from externalResponse when available
        resp.setExternalRefundId(null);
        resp.setPaymentRequestId(request.getId());
        resp.setPaymentTransactionId(transaction.getId());
        resp.setAmount(transaction.getAmount());
        resp.setCurrency(transaction.getCurrency());
        resp.setProcessedAt(java.time.LocalDateTime.now());
        resp.setGatewayResponse(null);
        resp.setMetadata(request.getMetadata());
        return resp;
    }

    private PayPalRefundRequest convertToRefundRequest(PaymentTransactionDto transaction, BigDecimal refundAmount) {
        return new PayPalRefundRequest();
    }

    private PaymentResponseDto convertRefundToPaymentResponse(PayPalRefundResponse refundResponse, PaymentTransactionDto transaction) {
        PaymentResponseDto resp = new PaymentResponseDto();
        resp.setSuccess(true); // Placeholder
        resp.setStatus("REFUNDED");
        resp.setMessage("PayPal refund processed");
        resp.setGatewayName("PayPal");
        resp.setExternalTransactionId(transaction.getExternalTransactionId());
        resp.setExternalRefundId(null);
        resp.setPaymentRequestId(transaction.getPaymentRequestId());
        resp.setPaymentTransactionId(transaction.getId());
        resp.setAmount(transaction.getAmount());
        resp.setCurrency(transaction.getCurrency());
        resp.setProcessedAt(java.time.LocalDateTime.now());
        resp.setGatewayResponse(null);
        return resp;
    }

    // External request/response placeholders
    private static class PayPalOrderRequest { }
    private static class PayPalOrderResponse { }
    private static class PayPalRefundRequest { }
    private static class PayPalRefundResponse { }
}