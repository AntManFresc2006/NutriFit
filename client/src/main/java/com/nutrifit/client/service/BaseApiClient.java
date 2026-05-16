package com.nutrifit.client.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutrifit.client.config.AppConfig;
import com.nutrifit.client.session.SessionManager;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Clase base para todos los clientes HTTP del módulo de cliente.
 * Centraliza: HttpClient con timeout, ObjectMapper, autenticación Bearer y validación de respuesta.
 * Sin esta clase cada ApiClient duplicaba exactamente los mismos campos y métodos.
 */
public abstract class BaseApiClient {

    protected static final String BACKEND_URL = AppConfig.getBackendUrl();
    protected static final String AUTH_HEADER = "Authorization";

    protected final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    protected final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    protected static String bearerToken() {
        return "Bearer " + SessionManager.getToken();
    }

    protected void validarRespuesta(HttpResponse<String> response, String prefijo) throws IOException {
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException(prefijo + ". Código HTTP: " + response.statusCode() + " - " + response.body());
        }
    }
}
