package com.nutrifit.backend.auth.security;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limitador de llamadas a endpoints de IA por usuario.
 * Máximo 5 peticiones por minuto para evitar abuso de APIs externas de pago.
 */
@Component
public class IaRateLimiter {

    private static final int MAX_PETICIONES = 5;
    private static final long VENTANA_MS = 60_000;

    private final Map<Long, Deque<Long>> intentosPorUsuario = new ConcurrentHashMap<>();

    /**
     * Verifica si un usuario puede llamar a un endpoint de IA.
     *
     * @param usuarioId identificador del usuario autenticado
     * @return {@code true} si la petición es permitida, {@code false} si se excedió el límite
     */
    public boolean permitir(Long usuarioId) {
        long ahora = Instant.now().toEpochMilli();
        Deque<Long> intentos = intentosPorUsuario.computeIfAbsent(usuarioId, k -> new ArrayDeque<>());

        synchronized (intentos) {
            while (!intentos.isEmpty() && ahora - intentos.peekFirst() > VENTANA_MS) {
                intentos.pollFirst();
            }
            if (intentos.size() >= MAX_PETICIONES) {
                return false;
            }
            intentos.addLast(ahora);
            return true;
        }
    }

    /**
     * Limpia cada hora las entradas de usuarios cuya ventana ya expiró.
     */
    @Scheduled(fixedRate = 3_600_000)
    public void limpiarUsuariosAntiguos() {
        long ahora = Instant.now().toEpochMilli();
        intentosPorUsuario.entrySet().removeIf(entry -> {
            synchronized (entry.getValue()) {
                while (!entry.getValue().isEmpty() && ahora - entry.getValue().peekFirst() > VENTANA_MS) {
                    entry.getValue().pollFirst();
                }
                return entry.getValue().isEmpty();
            }
        });
    }
}
