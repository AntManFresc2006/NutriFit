package com.nutrifit.backend.auth.security;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginRateLimiter {

    private static final int MAX_INTENTOS = 10;
    private static final long VENTANA_MS = 60_000;

    private final Map<String, Deque<Long>> intentosPorIp = new ConcurrentHashMap<>();

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
