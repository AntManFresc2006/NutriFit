package com.nutrifit.client.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("BaseApiClient.validarRespuesta")
class BaseApiClientTest {

    private static class StubApiClient extends BaseApiClient {}

    private final StubApiClient client = new StubApiClient();

    @SuppressWarnings("unchecked")
    private HttpResponse<String> mockResponse(int statusCode, String body) {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(statusCode);
        when(response.body()).thenReturn(body);
        return response;
    }

    @Test
    @DisplayName("no lanza excepción en respuesta 200 OK")
    void no_lanza_en_200() {
        HttpResponse<String> response = mockResponse(200, "{\"token\":\"abc\"}");
        assertThatNoException().isThrownBy(() -> client.validarRespuesta(response, "Error"));
    }

    @Test
    @DisplayName("no lanza excepción en respuesta 201 Created")
    void no_lanza_en_201() {
        HttpResponse<String> response = mockResponse(201, "{}");
        assertThatNoException().isThrownBy(() -> client.validarRespuesta(response, "Error"));
    }

    @Test
    @DisplayName("lanza IOException con código HTTP en respuesta 401")
    void lanza_excepcion_en_401() {
        HttpResponse<String> response = mockResponse(401, "Credenciales inválidas");
        assertThatThrownBy(() -> client.validarRespuesta(response, "Error de sesión"))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("401");
    }

    @Test
    @DisplayName("lanza IOException con código HTTP en respuesta 404")
    void lanza_excepcion_en_404() {
        HttpResponse<String> response = mockResponse(404, "Not Found");
        assertThatThrownBy(() -> client.validarRespuesta(response, "Recurso no encontrado"))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("404");
    }

    @Test
    @DisplayName("lanza IOException con código HTTP en respuesta 500")
    void lanza_excepcion_en_500() {
        HttpResponse<String> response = mockResponse(500, "Internal Server Error");
        assertThatThrownBy(() -> client.validarRespuesta(response, "Error de servidor"))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("500");
    }
}
