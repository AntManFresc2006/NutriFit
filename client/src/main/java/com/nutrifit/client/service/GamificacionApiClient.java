package com.nutrifit.client.service;

import com.nutrifit.client.model.GamificacionDto;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

/** Cliente HTTP para obtener estadísticas de gamificación. */
public class GamificacionApiClient extends BaseApiClient {

    private static final String BASE_URL = BACKEND_URL + "/api/gamificacion";

    public GamificacionDto obtenerGamificacion(Long usuarioId, LocalDate fecha) throws IOException, InterruptedException {
        String url = BASE_URL
                + "?usuarioId=" + URLEncoder.encode(String.valueOf(usuarioId), StandardCharsets.UTF_8)
                + "&fecha=" + URLEncoder.encode(fecha.toString(), StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header(AUTH_HEADER, bearerToken())
                .GET().build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al obtener gamificación");
        return objectMapper.readValue(response.body(), GamificacionDto.class);
    }
}
