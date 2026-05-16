package com.nutrifit.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nutrifit.client.model.RegistroEjercicioDto;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/** Cliente HTTP para registrar y consultar ejercicios realizados. */
public class RegistroEjercicioApiClient extends BaseApiClient {

    private static final String BASE_URL = BACKEND_URL + "/api/ejercicios-registro";
    private static final String PARAM_USUARIO = "?usuarioId=";

    public List<RegistroEjercicioDto> getByFecha(Long usuarioId, String fecha)
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + PARAM_USUARIO + usuarioId + "&fecha=" + fecha))
                .header(AUTH_HEADER, bearerToken())
                .GET().build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al obtener registros de ejercicio");
        return objectMapper.readValue(response.body(), new TypeReference<List<RegistroEjercicioDto>>() {});
    }

    public RegistroEjercicioDto registrar(Long usuarioId, Long ejercicioId, String fecha, int duracionMin)
            throws IOException, InterruptedException {
        String body = objectMapper.writeValueAsString(new RegistroRequestBody(ejercicioId, fecha, duracionMin));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + PARAM_USUARIO + usuarioId))
                .header(AUTH_HEADER, bearerToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al registrar ejercicio");
        return objectMapper.readValue(response.body(), RegistroEjercicioDto.class);
    }

    public void eliminar(Long usuarioId, Long registroId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + registroId + PARAM_USUARIO + usuarioId))
                .header(AUTH_HEADER, bearerToken())
                .DELETE().build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al eliminar registro de ejercicio");
    }

    private record RegistroRequestBody(Long ejercicioId, String fecha, int duracionMin) {}
}
