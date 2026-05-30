package com.nutrifit.backend.perfil.controller;

import com.nutrifit.backend.BaseIntegrationTest;
import com.nutrifit.backend.auth.dto.AuthResponse;
import com.nutrifit.backend.auth.dto.RegisterRequest;
import com.nutrifit.backend.perfil.dto.PerfilResponse;
import com.nutrifit.backend.perfil.dto.PerfilUpdateRequest;
import com.nutrifit.backend.perfil.model.NivelActividad;
import com.nutrifit.backend.perfil.model.Sexo;
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
class PerfilControllerIT extends BaseIntegrationTest {

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

    private HttpEntity<PerfilUpdateRequest> createPerfilUpdateRequest(String token, PerfilUpdateRequest body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return new HttpEntity<>(body, headers);
    }

    private HttpEntity<?> createAuthenticatedHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return new HttpEntity<>(headers);
    }

    @Test
    void testGetPerfilDefault() {
        // Register user
        ResponseEntity<AuthResponse> registerResponse = restTemplate.postForEntity(
                baseUrl() + "/api/auth/register",
                buildRegisterRequest("perfil_get@example.com"),
                AuthResponse.class
        );
        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        AuthResponse auth = registerResponse.getBody();
        assertThat(auth).isNotNull();
        Long usuarioId = auth.getUsuarioId();
        String token = auth.getToken();

        // Get perfil
        ResponseEntity<PerfilResponse> getResponse = restTemplate.exchange(
                baseUrl() + "/api/perfil/" + usuarioId,
                HttpMethod.GET,
                createAuthenticatedHeaders(token),
                PerfilResponse.class
        );

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        PerfilResponse perfil = getResponse.getBody();
        assertThat(perfil).isNotNull();
        assertThat(perfil.getId()).isEqualTo(usuarioId);
        assertThat(perfil.getNombre()).isEqualTo("Test User");
        assertThat(perfil.getEmail()).isEqualTo("perfil_get@example.com");
    }

    @Test
    void testActualizarPerfil() {
        // Register user
        ResponseEntity<AuthResponse> registerResponse = restTemplate.postForEntity(
                baseUrl() + "/api/auth/register",
                buildRegisterRequest("perfil_update@example.com"),
                AuthResponse.class
        );
        AuthResponse auth = registerResponse.getBody();
        assertThat(auth).isNotNull();
        Long usuarioId = auth.getUsuarioId();
        String token = auth.getToken();

        // Update perfil with biometric data
        PerfilUpdateRequest updateReq = new PerfilUpdateRequest();
        updateReq.setSexo(Sexo.H);
        updateReq.setFechaNacimiento(LocalDate.of(1990, 5, 15));
        updateReq.setAlturaCm(180);
        updateReq.setPesoKgActual(75.0);
        updateReq.setPesoObjetivo(70.0);
        updateReq.setNivelActividad(NivelActividad.MODERADO);

        ResponseEntity<PerfilResponse> updateResponse = restTemplate.exchange(
                baseUrl() + "/api/perfil/" + usuarioId,
                HttpMethod.PUT,
                createPerfilUpdateRequest(token, updateReq),
                PerfilResponse.class
        );

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        PerfilResponse updatedPerfil = updateResponse.getBody();
        assertThat(updatedPerfil).isNotNull();
        assertThat(updatedPerfil.getSexo()).isEqualTo(Sexo.H);
        assertThat(updatedPerfil.getAlturaCm()).isEqualTo(180);
        assertThat(updatedPerfil.getPesoKgActual()).isEqualTo(75.0);
        assertThat(updatedPerfil.getTmb()).isGreaterThan(0);
        assertThat(updatedPerfil.getTdee()).isGreaterThan(0);

        // Get and verify persisted
        ResponseEntity<PerfilResponse> getResponse = restTemplate.exchange(
                baseUrl() + "/api/perfil/" + usuarioId,
                HttpMethod.GET,
                createAuthenticatedHeaders(token),
                PerfilResponse.class
        );

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        PerfilResponse retrievedPerfil = getResponse.getBody();
        assertThat(retrievedPerfil).isNotNull();
        assertThat(retrievedPerfil.getTmb()).isGreaterThan(0);
    }
}
