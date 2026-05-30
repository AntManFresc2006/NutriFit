package com.nutrifit.backend.gamificacion.controller;

import com.nutrifit.backend.BaseIntegrationTest;
import com.nutrifit.backend.auth.dto.AuthResponse;
import com.nutrifit.backend.auth.dto.RegisterRequest;
import com.nutrifit.backend.gamificacion.dto.GamificacionResponse;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@TestPropertySource(properties = {
    "openrouter.gemma.api.key=test-key",
    "openrouter.deepseek.api.key=test-key"
})
class GamificacionControllerIT extends BaseIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    private RegisterRequest buildRegisterRequest(String email) {
        RegisterRequest req = new RegisterRequest();
        req.setNombre("Test User");
        req.setEmail(email);
        req.setPassword("password123");
        return req;
    }

    private HttpEntity<?> createAuthenticatedHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return new HttpEntity<>(headers);
    }

    @Test
    void testGetGamificacion() {
        // Register user
        ResponseEntity<AuthResponse> registerResponse = restTemplate.postForEntity(
                baseUrl() + "/api/auth/register",
                buildRegisterRequest("gamificacion_test@example.com"),
                AuthResponse.class
        );
        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        AuthResponse auth = registerResponse.getBody();
        assertThat(auth).isNotNull();
        Long usuarioId = auth.getUsuarioId();
        String token = auth.getToken();

        // Get gamificacion stats
        ResponseEntity<GamificacionResponse> getResponse = restTemplate.exchange(
                baseUrl() + "/api/gamificacion?usuarioId=" + usuarioId + "&fecha=" + LocalDate.now(),
                HttpMethod.GET,
                createAuthenticatedHeaders(token),
                GamificacionResponse.class
        );

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        GamificacionResponse gamificacion = getResponse.getBody();
        assertThat(gamificacion).isNotNull();
        assertThat(gamificacion.getRacha()).isNotNull();
        assertThat(gamificacion.getNutriScore()).isNotNull();
        assertThat(gamificacion.getNutriGrade()).isNotBlank();
    }

    @Test
    void testGetGamificacionSinToken() {
        // Try to access gamificacion without token
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/gamificacion?usuarioId=1&fecha=" + LocalDate.now(),
                HttpMethod.GET,
                new HttpEntity<>(new HttpHeaders()),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
