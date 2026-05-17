package com.nutrifit.client.service;

import com.nutrifit.client.model.HidratacionDto;
import com.nutrifit.client.model.RegistroHidratacionDto;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class HidratacionApiClient extends BaseApiClient {

    private static final String BASE_URL = BACKEND_URL + "/api/usuarios";

    public HidratacionDto obtenerResumen(Long usuarioId, String fecha) throws IOException, InterruptedException {
        String url = BASE_URL + "/" + usuarioId + "/hidratacion?fecha=" + URLEncoder.encode(fecha, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header(AUTH_HEADER, bearerToken())
                .GET().build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al obtener resumen de hidratación");
        return objectMapper.readValue(response.body(), HidratacionDto.class);
    }

    public void registrarHidratacion(Long usuarioId, int cantidadMl, String fuente, String fecha) throws IOException, InterruptedException {
        String url = BASE_URL + "/" + usuarioId + "/hidratacion";

        RegistroHidratacionDto registro = new RegistroHidratacionDto(cantidadMl, fuente, fecha);
        String body = objectMapper.writeValueAsString(registro);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header(AUTH_HEADER, bearerToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al registrar hidratación");
    }
}
