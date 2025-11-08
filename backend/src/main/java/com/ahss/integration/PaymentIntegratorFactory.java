package com.ahss.integration;

import com.ahss.enums.PaymentMethodType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Factory for selecting the appropriate PaymentIntegrator based on the payment method type.
 */
@Component
public class PaymentIntegratorFactory {

    private final List<PaymentIntegrator> integrators;

    @Autowired
    public PaymentIntegratorFactory(List<PaymentIntegrator> integrators) {
        this.integrators = integrators;
    }

    /**
     * Gets the integrator that supports the given payment method type.
     *
     * @param type the payment method type
     * @return the supporting PaymentIntegrator
     * @throws NoSuchElementException if no integrator supports the type
     */
    public PaymentIntegrator getIntegrator(PaymentMethodType type) {
        return integrators.stream()
                .filter(integrator -> integrator.supports(type))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No integrator found for type: " + type));
    }
}