package com.nutrifit.client.service;

import com.nutrifit.client.model.PesoHistorialDto;
import com.nutrifit.client.model.PesoHistorialRequestDto;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/** Cliente HTTP para gestionar el historial de peso. */
public class PesoHistorialApiClient extends BaseApiClient {

    private static final String BASE_URL = BACKEND_URL + "/api/peso-historial";

    public List<PesoHistorialDto> obtenerHistorial(Long usuarioId, int limit) throws IOException, InterruptedException {
        String url = BASE_URL
                + "?usuarioId=" + URLEncoder.encode(String.valueOf(usuarioId), StandardCharsets.UTF_8)
                + "&limit=" + limit;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header(AUTH_HEADER, bearerToken())
                .GET().build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al obtener historial de peso");
        return Arrays.asList(objectMapper.readValue(response.body(), PesoHistorialDto[].class));
    }

    public PesoHistorialDto registrarPeso(Long usuarioId, PesoHistorialRequestDto request) throws IOException, InterruptedException {
        String body = objectMapper.writeValueAsString(request);
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "?usuarioId=" + URLEncoder.encode(String.valueOf(usuarioId), StandardCharsets.UTF_8)))
                .header(AUTH_HEADER, bearerToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al registrar peso");
        return objectMapper.readValue(response.body(), PesoHistorialDto.class);
    }
}
