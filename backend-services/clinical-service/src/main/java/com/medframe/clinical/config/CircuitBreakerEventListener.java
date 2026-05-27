package com.medframe.clinical.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerOnStateTransitionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Listener de eventos del Circuit Breaker para logging estructurado.
 * 
 * <p>Este componente escucha las transiciones de estado del circuit breaker
 * y registra logs estructurados para facilitar el monitoreo y troubleshooting.</p>
 * 
 * <p><strong>Estados del Circuit Breaker:</strong></p>
 * <ul>
 *   <li><strong>CLOSED:</strong> Funcionamiento normal, todas las llamadas pasan</li>
 *   <li><strong>OPEN:</strong> Demasiados fallos, llamadas fallan rápido sin intentar</li>
 *   <li><strong>HALF_OPEN:</strong> Probando recuperación, permite algunas llamadas</li>
 * </ul>
 * 
 * <p><strong>Requisitos relacionados:</strong></p>
 * <ul>
 *   <li>REQ-9.8: Logs estructurados para troubleshooting</li>
 * </ul>
 * 
 * @author MedFlow Development Team
 * @version 1.0
 * @since 2024-04-24
 */
@Configuration
@Slf4j
public class CircuitBreakerEventListener {

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public CircuitBreakerEventListener(CircuitBreakerRegistry circuitBreakerRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    /**
     * Registra listeners para todos los circuit breakers después de la inicialización.
     */
    @PostConstruct
    public void registerEventListeners() {
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(circuitBreaker -> {
            circuitBreaker.getEventPublisher()
                    .onStateTransition(this::logStateTransition);
        });
    }

    /**
     * Registra log estructurado cuando el circuit breaker cambia de estado.
     * 
     * <p><strong>Logs generados:</strong></p>
     * <ul>
     *   <li>ERROR cuando transiciona a OPEN (servicio no disponible)</li>
     *   <li>WARN cuando transiciona a HALF_OPEN (probando recuperación)</li>
     *   <li>INFO cuando transiciona a CLOSED (servicio recuperado)</li>
     * </ul>
     * 
     * @param event Evento de transición de estado
     */
    private void logStateTransition(CircuitBreakerOnStateTransitionEvent event) {
        String circuitBreakerName = event.getCircuitBreakerName();
        CircuitBreaker.State fromState = event.getStateTransition().getFromState();
        CircuitBreaker.State toState = event.getStateTransition().getToState();

        switch (toState) {
            case OPEN:
                // REQ-9.8: Log ERROR cuando circuit breaker se abre
                log.error("Circuit breaker OPEN para {}. Transición: {} -> {}. " +
                                "El servicio no está disponible y las llamadas fallarán rápido.",
                        circuitBreakerName, fromState, toState);
                break;

            case HALF_OPEN:
                log.warn("Circuit breaker HALF_OPEN para {}. Transición: {} -> {}. " +
                                "Probando recuperación del servicio.",
                        circuitBreakerName, fromState, toState);
                break;

            case CLOSED:
                log.info("Circuit breaker CLOSED para {}. Transición: {} -> {}. " +
                                "El servicio se ha recuperado y está funcionando normalmente.",
                        circuitBreakerName, fromState, toState);
                break;

            default:
                log.debug("Circuit breaker {} transicionó de {} a {}",
                        circuitBreakerName, fromState, toState);
        }
    }
}
