package com.nutrifit.backend.comida.controller;

import com.nutrifit.backend.BaseIntegrationTest;
import com.nutrifit.backend.auth.dto.AuthResponse;
import com.nutrifit.backend.auth.dto.RegisterRequest;
import com.nutrifit.backend.comida.dto.ComidaRequest;
import com.nutrifit.backend.comida.dto.ComidaResponse;
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
class ComidaControllerIT extends BaseIntegrationTest {

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

    private HttpEntity<ComidaRequest> createComidaRequest(String token, ComidaRequest body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return new HttpEntity<>(body, headers);
    }

    @Test
    void testCrearYListarComida() {
        // Register user
        ResponseEntity<AuthResponse> registerResponse = restTemplate.postForEntity(
                baseUrl() + "/api/auth/register",
                buildRegisterRequest("comida_test@example.com"),
                AuthResponse.class
        );
        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        AuthResponse auth = registerResponse.getBody();
        assertThat(auth).isNotNull();
        Long usuarioId = auth.getUsuarioId();
        String token = auth.getToken();

        // Create comida
        ComidaRequest comidaReq = new ComidaRequest();
        comidaReq.setFecha(LocalDate.now());
        comidaReq.setTipo("DESAYUNO");

        ResponseEntity<ComidaResponse> createResponse = restTemplate.exchange(
                baseUrl() + "/api/comidas?usuarioId=" + usuarioId,
                HttpMethod.POST,
                createComidaRequest(token, comidaReq),
                ComidaResponse.class
        );

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        ComidaResponse createdComida = createResponse.getBody();
        assertThat(createdComida).isNotNull();
        assertThat(createdComida.getTipo()).isEqualTo("DESAYUNO");
        assertThat(createdComida.getFecha()).isEqualTo(LocalDate.now());
        assertThat(createdComida.getUsuarioId()).isEqualTo(usuarioId);

        // List comidas
        ResponseEntity<ComidaResponse[]> listResponse = restTemplate.exchange(
                baseUrl() + "/api/comidas?usuarioId=" + usuarioId + "&fecha=" + LocalDate.now(),
                HttpMethod.GET,
                createAuthenticatedHeaders(token),
                ComidaResponse[].class
        );

        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listResponse.getBody()).isNotEmpty();
        assertThat(listResponse.getBody()[0].getTipo()).isEqualTo("DESAYUNO");
    }

    @Test
    void testEliminarComida() {
        // Register user
        ResponseEntity<AuthResponse> registerResponse = restTemplate.postForEntity(
                baseUrl() + "/api/auth/register",
                buildRegisterRequest("comida_delete@example.com"),
                AuthResponse.class
        );
        AuthResponse auth = registerResponse.getBody();
        assertThat(auth).isNotNull();
        Long usuarioId = auth.getUsuarioId();
        String token = auth.getToken();

        // Create comida
        ComidaRequest comidaReq = new ComidaRequest();
        comidaReq.setFecha(LocalDate.now());
        comidaReq.setTipo("ALMUERZO");

        ResponseEntity<ComidaResponse> createResponse = restTemplate.exchange(
                baseUrl() + "/api/comidas?usuarioId=" + usuarioId,
                HttpMethod.POST,
                createComidaRequest(token, comidaReq),
                ComidaResponse.class
        );

        ComidaResponse createdComida = createResponse.getBody();
        assertThat(createdComida).isNotNull();
        Long comidaId = createdComida.getId();

        // Delete comida
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                baseUrl() + "/api/comidas/" + comidaId,
                HttpMethod.DELETE,
                createAuthenticatedHeaders(token),
                Void.class
        );

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verify comida is gone
        ResponseEntity<ComidaResponse[]> listResponse = restTemplate.exchange(
                baseUrl() + "/api/comidas?usuarioId=" + usuarioId + "&fecha=" + LocalDate.now(),
                HttpMethod.GET,
                createAuthenticatedHeaders(token),
                ComidaResponse[].class
        );

        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listResponse.getBody()).isEmpty();
    }
}
