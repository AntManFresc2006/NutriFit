package com.nutrifit.client.service;

import com.nutrifit.client.model.RetoDto;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/** Cliente HTTP para obtener retos del usuario. */
public class RetosApiClient extends BaseApiClient {

    private static final String BASE_URL = BACKEND_URL + "/api/retos";

    public List<RetoDto> obtenerRetos(Long usuarioId) throws IOException, InterruptedException {
        String url = BASE_URL
                + "?usuarioId=" + URLEncoder.encode(String.valueOf(usuarioId), StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header(AUTH_HEADER, bearerToken())
                .GET().build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al obtener retos");
        return Arrays.asList(objectMapper.readValue(response.body(), RetoDto[].class));
    }
}
