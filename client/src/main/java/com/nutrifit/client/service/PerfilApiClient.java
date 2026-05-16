package com.nutrifit.client.service;

import com.nutrifit.client.model.PerfilDto;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

/** Cliente HTTP del módulo de perfil. */
public class PerfilApiClient extends BaseApiClient {

    private static final String BASE_URL = BACKEND_URL + "/api/perfil";

    public PerfilDto getPerfil(Long id) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id))
                .header(AUTH_HEADER, bearerToken())
                .GET().build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al obtener el perfil");
        return objectMapper.readValue(response.body(), PerfilDto.class);
    }

    public PerfilDto updatePerfil(Long id, Map<String, Object> datos) throws IOException, InterruptedException {
        String json = objectMapper.writeValueAsString(datos);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id))
                .header(AUTH_HEADER, bearerToken())
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json)).build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al actualizar el perfil");
        return objectMapper.readValue(response.body(), PerfilDto.class);
    }
}
