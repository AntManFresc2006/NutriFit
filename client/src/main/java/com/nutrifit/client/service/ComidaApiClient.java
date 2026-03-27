package com.nutrifit.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutrifit.client.model.ComidaDto;
import com.nutrifit.client.model.ComidaItemDto;
import com.nutrifit.client.session.SessionManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * Cliente HTTP del módulo de comidas.
 * Se encarga de comunicar la interfaz JavaFX con la API REST del backend.
 */
public class ComidaApiClient {

    private static final String BASE_URL = "http://localhost:8080/api/comidas";
    private static final String AUTH_HEADER = "Authorization";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static String bearerToken() {
        return "Bearer " + SessionManager.getToken();
    }

    /**
     * Lista las comidas de un usuario para una fecha concreta (formato yyyy-MM-dd).
     */
    public List<ComidaDto> getByFecha(Long usuarioId, String fecha) throws IOException, InterruptedException {
        String url = BASE_URL + "?usuarioId=" + usuarioId + "&fecha=" + fecha;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header(AUTH_HEADER, bearerToken())
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al obtener comidas");

        return objectMapper.readValue(response.body(), new TypeReference<List<ComidaDto>>() {});
    }

    /**
     * Crea una nueva comida para un usuario.
     * @param fecha en formato yyyy-MM-dd
     * @param tipo p.ej. "DESAYUNO"
     */
    public ComidaDto crear(Long usuarioId, String fecha, String tipo) throws IOException, InterruptedException {
        String body = objectMapper.writeValueAsString(new ComidaRequestBody(fecha, tipo));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "?usuarioId=" + usuarioId))
                .header(AUTH_HEADER, bearerToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al crear comida");

        return objectMapper.readValue(response.body(), ComidaDto.class);
    }

    /**
     * Elimina una comida por su id.
     */
    public void eliminar(Long comidaId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + comidaId))
                .header(AUTH_HEADER, bearerToken())
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al eliminar comida");
    }

    /**
     * Lista los items (alimentos) de una comida con su detalle nutricional.
     */
    public List<ComidaItemDto> getItems(Long comidaId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + comidaId + "/items"))
                .header(AUTH_HEADER, bearerToken())
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al obtener items de la comida");

        return objectMapper.readValue(response.body(), new TypeReference<List<ComidaItemDto>>() {});
    }

    /**
     * Añade un alimento a una comida existente.
     */
    public void addItem(Long comidaId, Long alimentoId, double gramos) throws IOException, InterruptedException {
        String body = objectMapper.writeValueAsString(new ComidaItemRequestBody(alimentoId, gramos));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + comidaId + "/items"))
                .header(AUTH_HEADER, bearerToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al añadir alimento a la comida");
    }

    /**
     * Elimina un item concreto de una comida.
     */
    public void eliminarItem(Long comidaId, Long itemId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + comidaId + "/items/" + itemId))
                .header(AUTH_HEADER, bearerToken())
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al eliminar item de la comida");
    }

    private void validarRespuesta(HttpResponse<String> response, String prefijo) throws IOException {
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException(prefijo + ". Código HTTP: " + response.statusCode() + " - " + response.body());
        }
    }

    // DTOs internos para serializar el cuerpo de las peticiones POST
    private static class ComidaRequestBody {
        public final String fecha;
        public final String tipo;

        ComidaRequestBody(String fecha, String tipo) {
            this.fecha = fecha;
            this.tipo = tipo;
        }
    }

    private static class ComidaItemRequestBody {
        public final Long alimentoId;
        public final double gramos;

        ComidaItemRequestBody(Long alimentoId, double gramos) {
            this.alimentoId = alimentoId;
            this.gramos = gramos;
        }
    }
}
