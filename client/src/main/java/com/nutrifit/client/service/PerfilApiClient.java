package com.nutrifit.client.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutrifit.client.model.PerfilDto;
import com.nutrifit.client.session.SessionManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

/**
 * Cliente HTTP del módulo de perfil.
 * Permite consultar y actualizar los datos biométricos del usuario.
 */
public class PerfilApiClient {

    private static final String BASE_URL = "http://localhost:8080/api/perfil";
    private static final String AUTH_HEADER = "Authorization";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static String bearerToken() {
        return "Bearer " + SessionManager.getToken();
    }

    /**
     * Obtiene el perfil completo del usuario con TMB y TDEE calculados.
     */
    public PerfilDto getPerfil(Long id) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id))
                .header(AUTH_HEADER, bearerToken())
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al obtener el perfil");

        return objectMapper.readValue(response.body(), PerfilDto.class);
    }

    /**
     * Actualiza los datos biométricos del usuario y devuelve el perfil recalculado.
     */
    public PerfilDto updatePerfil(Long id, Map<String, Object> datos) throws IOException, InterruptedException {
        String json = objectMapper.writeValueAsString(datos);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id))
                .header(AUTH_HEADER, bearerToken())
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al actualizar el perfil");

        return objectMapper.readValue(response.body(), PerfilDto.class);
    }

    private void validarRespuesta(HttpResponse<String> response, String prefijo) throws IOException {
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException(prefijo + ". Código HTTP: " + response.statusCode() + " - " + response.body());
        }
    }
}
