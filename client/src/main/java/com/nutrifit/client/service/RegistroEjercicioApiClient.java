package com.nutrifit.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutrifit.client.model.RegistroEjercicioDto;
import com.nutrifit.client.session.SessionManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class RegistroEjercicioApiClient {

    private static final String BASE_URL = "http://localhost:8080/api/ejercicios-registro";
    private static final String AUTH_HEADER = "Authorization";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules();

    private static String bearerToken() {
        return "Bearer " + SessionManager.getToken();
    }

    public List<RegistroEjercicioDto> getByFecha(Long usuarioId, String fecha) throws IOException, InterruptedException {
        String url = BASE_URL + "?usuarioId=" + usuarioId + "&fecha=" + fecha;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header(AUTH_HEADER, bearerToken())
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al obtener registros de ejercicio");

        return objectMapper.readValue(response.body(), new TypeReference<List<RegistroEjercicioDto>>() {});
    }

    public RegistroEjercicioDto registrar(Long usuarioId, Long ejercicioId, String fecha, int duracionMin)
            throws IOException, InterruptedException {
        String body = objectMapper.writeValueAsString(
                new RegistroRequestBody(ejercicioId, fecha, duracionMin));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "?usuarioId=" + usuarioId))
                .header(AUTH_HEADER, bearerToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al registrar ejercicio");

        return objectMapper.readValue(response.body(), RegistroEjercicioDto.class);
    }

    public void eliminar(Long usuarioId, Long registroId) throws IOException, InterruptedException {
        String url = BASE_URL + "/" + registroId + "?usuarioId=" + usuarioId;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header(AUTH_HEADER, bearerToken())
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al eliminar registro de ejercicio");
    }

    private void validarRespuesta(HttpResponse<String> response, String prefijo) throws IOException {
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException(prefijo + ". Código HTTP: " + response.statusCode() + " - " + response.body());
        }
    }

    private static class RegistroRequestBody {
        public final Long ejercicioId;
        public final String fecha;
        public final int duracionMin;

        RegistroRequestBody(Long ejercicioId, String fecha, int duracionMin) {
            this.ejercicioId = ejercicioId;
            this.fecha = fecha;
            this.duracionMin = duracionMin;
        }
    }
}
