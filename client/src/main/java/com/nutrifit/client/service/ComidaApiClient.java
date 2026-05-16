package com.nutrifit.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nutrifit.client.model.ComidaDto;
import com.nutrifit.client.model.ComidaItemDto;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/** Cliente HTTP del módulo de comidas. */
public class ComidaApiClient extends BaseApiClient {

    private static final String BASE_URL = BACKEND_URL + "/api/comidas";

    public List<ComidaDto> getByFecha(Long usuarioId, String fecha) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "?usuarioId=" + usuarioId + "&fecha=" + fecha))
                .header(AUTH_HEADER, bearerToken())
                .GET().build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al obtener comidas");
        return objectMapper.readValue(response.body(), new TypeReference<List<ComidaDto>>() {});
    }

    public ComidaDto crear(Long usuarioId, String fecha, String tipo) throws IOException, InterruptedException {
        String body = objectMapper.writeValueAsString(new ComidaRequestBody(fecha, tipo));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "?usuarioId=" + usuarioId))
                .header(AUTH_HEADER, bearerToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al crear comida");
        return objectMapper.readValue(response.body(), ComidaDto.class);
    }

    public void eliminar(Long comidaId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + comidaId))
                .header(AUTH_HEADER, bearerToken())
                .DELETE().build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al eliminar comida");
    }

    public List<ComidaItemDto> getItems(Long comidaId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + comidaId + "/items"))
                .header(AUTH_HEADER, bearerToken())
                .GET().build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al obtener items de la comida");
        return objectMapper.readValue(response.body(), new TypeReference<List<ComidaItemDto>>() {});
    }

    public void addItem(Long comidaId, Long alimentoId, double gramos) throws IOException, InterruptedException {
        String body = objectMapper.writeValueAsString(new ComidaItemRequestBody(alimentoId, gramos));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + comidaId + "/items"))
                .header(AUTH_HEADER, bearerToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al añadir alimento a la comida");
    }

    public void eliminarItem(Long comidaId, Long itemId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + comidaId + "/items/" + itemId))
                .header(AUTH_HEADER, bearerToken())
                .DELETE().build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al eliminar item de la comida");
    }

    private record ComidaRequestBody(String fecha, String tipo) {}

    private record ComidaItemRequestBody(Long alimentoId, double gramos) {}
}
