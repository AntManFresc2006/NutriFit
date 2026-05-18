package com.nutrifit.backend.auth.security;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limitador de intentos de login por IP usando ventana deslizante.
 * Máximo 10 intentos por minuto por dirección IP; previene ataques de fuerza bruta.
 */
@Component
public class LoginRateLimiter {

    private static final int MAX_INTENTOS = 10;
    private static final long VENTANA_MS = 60_000;

    private final Map<String, Deque<Long>> intentosPorIp = new ConcurrentHashMap<>();

    /**
     * Verifica si una IP puede hacer un intento. Usa ventana deslizante de 60 segundos:
     * elimina intentos antiguos y rechaza si se superan 10 en la ventana actual.
     *
     * @param ip dirección IP de la petición
     * @return {@code true} si el intento es permitido, {@code false} si se excedió el límite
     */
    public boolean permitir(String ip) {
        long ahora = Instant.now().toEpochMilli();
        Deque<Long> intentos = intentosPorIp.computeIfAbsent(ip, k -> new ArrayDeque<>());

        synchronized (intentos) {
            // Eliminar intentos fuera de la ventana
            while (!intentos.isEmpty() && ahora - intentos.peekFirst() > VENTANA_MS) {
                intentos.pollFirst();
            }
            if (intentos.size() >= MAX_INTENTOS) {
                return false;
            }
            intentos.addLast(ahora);
            return true;
        }
    }
}
