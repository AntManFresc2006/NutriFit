package com.nutrifit.client.service;

import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

/** Cliente HTTP para registro, login y logout. */
public class AuthApiClient extends BaseApiClient {

    private static final String BASE_URL = BACKEND_URL + "/api/auth";

    public Map<String, Object> register(String nombre, String email, String password)
            throws IOException, InterruptedException {
        String json = objectMapper.writeValueAsString(Map.of(
                "nombre", nombre, "email", email, "password", password));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al registrar usuario");
        return objectMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
    }

    public Map<String, Object> login(String email, String password)
            throws IOException, InterruptedException {
        String json = objectMapper.writeValueAsString(Map.of("email", email, "password", password));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al iniciar sesión");
        return objectMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
    }

    public void logout(String token) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/logout"))
                .header(AUTH_HEADER, "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al cerrar sesión");
    }
}
