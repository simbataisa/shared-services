package com.ahss.tracing;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TracingAspect {

    private final ObservationRegistry observationRegistry;

    public TracingAspect(ObservationRegistry observationRegistry) {
        this.observationRegistry = observationRegistry;
    }

    @Around("@within(org.springframework.stereotype.Service)")
    public Object observeService(ProceedingJoinPoint pjp) throws Throwable {
        return observe("service", pjp);
    }

    @Around("@within(org.springframework.stereotype.Repository)")
    public Object observeRepository(ProceedingJoinPoint pjp) throws Throwable {
        return observe("repository", pjp);
    }

    private Object observe(String layer, ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature ms = (MethodSignature) pjp.getSignature();
        String className = ms.getDeclaringType().getSimpleName();
        String methodName = ms.getMethod().getName();
        String name = layer + "." + className + "." + methodName;

        Observation observation = Observation.start(name, observationRegistry)
            .lowCardinalityKeyValue("class", className)
            .lowCardinalityKeyValue("method", methodName)
            .lowCardinalityKeyValue("layer", layer);
        try (Observation.Scope scope = observation.openScope()) {
            return pjp.proceed();
        } catch (Throwable t) {
            observation.error(t);
            throw t;
        } finally {
            observation.stop();
        }
    }
}