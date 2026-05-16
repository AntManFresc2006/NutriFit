package com.nutrifit.client.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutrifit.client.model.EvaluacionIaRequest;
import com.nutrifit.client.model.ResumenDiarioDto;
import com.nutrifit.client.session.SessionManager;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * Cliente HTTP para obtener el resumen diario nutricional.
 */
public class ResumenDiarioApiClient {

    private static final String BASE_URL = "http://localhost:8080/api/resumen-diario";
    private static final String AUTH_HEADER = "Authorization";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static String bearerToken() {
        return "Bearer " + SessionManager.getToken();
    }

    public ResumenDiarioDto obtenerResumen(Long usuarioId, String fecha) throws IOException, InterruptedException {
        String url = BASE_URL
                + "?usuarioId=" + URLEncoder.encode(String.valueOf(usuarioId), StandardCharsets.UTF_8)
                + "&fecha=" + URLEncoder.encode(fecha, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header(AUTH_HEADER, bearerToken())
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al obtener el resumen diario");

        return objectMapper.readValue(response.body(), ResumenDiarioDto.class);
    }

    public String evaluarConIa(EvaluacionIaRequest request) throws IOException, InterruptedException {
        String url = "http://localhost:8080/api/resumen/evaluacion-ia";
        String body = objectMapper.writeValueAsString(request);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header(AUTH_HEADER, bearerToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al evaluar con IA");

        return objectMapper.readTree(response.body()).path("evaluacion").asText();
    }

    private void validarRespuesta(HttpResponse<String> response, String prefijo) throws IOException {
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException(prefijo + ". Código HTTP: " + response.statusCode() + " - " + response.body());
        }
    }
}
