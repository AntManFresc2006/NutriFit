package com.nutrifit.client.service;

import com.nutrifit.client.model.EvaluacionIaRequest;
import com.nutrifit.client.model.ResumenDiarioDto;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/** Cliente HTTP para el resumen diario nutricional y evaluación IA. */
public class ResumenDiarioApiClient extends BaseApiClient {

    private static final String BASE_URL      = BACKEND_URL + "/api/resumen-diario";
    private static final String URL_EVALUACION = BACKEND_URL + "/api/resumen/evaluacion-ia";

    public ResumenDiarioDto obtenerResumen(Long usuarioId, String fecha) throws IOException, InterruptedException {
        String url = BASE_URL
                + "?usuarioId=" + URLEncoder.encode(String.valueOf(usuarioId), StandardCharsets.UTF_8)
                + "&fecha="     + URLEncoder.encode(fecha, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header(AUTH_HEADER, bearerToken())
                .GET().build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al obtener el resumen diario");
        return objectMapper.readValue(response.body(), ResumenDiarioDto.class);
    }

    public String evaluarConIa(EvaluacionIaRequest iaRequest) throws IOException, InterruptedException {
        String body = objectMapper.writeValueAsString(iaRequest);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL_EVALUACION))
                .header(AUTH_HEADER, bearerToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al evaluar con IA");
        return objectMapper.readTree(response.body()).path("evaluacion").asText();
    }
}
