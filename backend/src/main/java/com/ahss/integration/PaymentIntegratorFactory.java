package com.ahss.integration;

import com.ahss.enums.PaymentMethodType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Factory for selecting the appropriate PaymentIntegrator based on the payment method type and gateway.
 * Supports configurable default gateway for payment methods that are supported by multiple gateways
 * (e.g., CREDIT_CARD can be processed by both Stripe and PayPal).
 */
@Component
public class PaymentIntegratorFactory {

    private static final Logger log = LoggerFactory.getLogger(PaymentIntegratorFactory.class);

    private final List<PaymentIntegrator> integrators;
    private final String defaultGateway;

    @Autowired
    public PaymentIntegratorFactory(
            List<PaymentIntegrator> integrators,
            @Value("${payment.default-gateway:Stripe}") String defaultGateway) {
        this.integrators = integrators;
        this.defaultGateway = defaultGateway;
        log.info("PaymentIntegratorFactory initialized with {} integrators, default gateway: {}",
                integrators.size(), defaultGateway);
        integrators.forEach(i -> log.info("  - {} supports: {}",
                i.getGatewayName(), getSupportedMethods(i)));
    }

    /**
     * Gets the integrator that supports the given payment method type.
     * Uses the default gateway if multiple integrators support the type.
     *
     * @param type the payment method type
     * @return the supporting PaymentIntegrator
     * @throws NoSuchElementException if no integrator supports the type
     */
    public PaymentIntegrator getIntegrator(PaymentMethodType type) {
        return getIntegrator(type, null);
    }

    /**
     * Gets the integrator that supports the given payment method type and gateway.
     * If gateway is null, uses the default gateway for payment methods supported by multiple gateways.
     * If gateway is specified, returns the integrator for that specific gateway.
     *
     * @param type the payment method type
     * @param gateway the gateway name (optional, e.g., "Stripe", "PayPal")
     * @return the supporting PaymentIntegrator
     * @throws NoSuchElementException if no integrator supports the type and gateway combination
     */
    public PaymentIntegrator getIntegrator(PaymentMethodType type, String gateway) {
        log.debug("Finding integrator for type: {}, gateway: {}", type, gateway);

        // Find all integrators that support this payment method type
        List<PaymentIntegrator> supportingIntegrators = integrators.stream()
                .filter(integrator -> integrator.supports(type))
                .toList();

        if (supportingIntegrators.isEmpty()) {
            throw new NoSuchElementException("No integrator found for payment method type: " + type);
        }

        // If gateway is specified, find the integrator for that gateway
        if (gateway != null && !gateway.isEmpty()) {
            Optional<PaymentIntegrator> specificIntegrator = supportingIntegrators.stream()
                    .filter(integrator -> integrator.getGatewayName().equalsIgnoreCase(gateway))
                    .findFirst();

            if (specificIntegrator.isPresent()) {
                log.info("Using specified gateway {} for payment method {}", gateway, type);
                return specificIntegrator.get();
            } else {
                throw new NoSuchElementException(
                        String.format("No integrator found for payment method type: %s with gateway: %s",
                                type, gateway));
            }
        }

        // If only one integrator supports this type, use it
        if (supportingIntegrators.size() == 1) {
            PaymentIntegrator integrator = supportingIntegrators.get(0);
            log.info("Using single available integrator {} for payment method {}",
                    integrator.getGatewayName(), type);
            return integrator;
        }

        // Multiple integrators support this type - use default gateway
        Optional<PaymentIntegrator> defaultIntegrator = supportingIntegrators.stream()
                .filter(integrator -> integrator.getGatewayName().equalsIgnoreCase(defaultGateway))
                .findFirst();

        if (defaultIntegrator.isPresent()) {
            log.info("Multiple gateways support {}, using default gateway: {}", type, defaultGateway);
            return defaultIntegrator.get();
        }

        // Fallback: use the first available integrator
        PaymentIntegrator fallbackIntegrator = supportingIntegrators.get(0);
        log.warn("Default gateway {} not found for type {}, falling back to: {}",
                defaultGateway, type, fallbackIntegrator.getGatewayName());
        return fallbackIntegrator;
    }

    /**
     * Helper method to get supported payment method types for logging
     */
    private String getSupportedMethods(PaymentIntegrator integrator) {
        return java.util.stream.Stream.of(PaymentMethodType.values())
                .filter(integrator::supports)
                .map(PaymentMethodType::name)
                .collect(java.util.stream.Collectors.joining(", "));
    }
}