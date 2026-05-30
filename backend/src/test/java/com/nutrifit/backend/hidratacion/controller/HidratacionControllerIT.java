package com.nutrifit.backend.hidratacion.controller;

import com.nutrifit.backend.BaseIntegrationTest;
import com.nutrifit.backend.auth.dto.AuthResponse;
import com.nutrifit.backend.auth.dto.RegisterRequest;
import com.nutrifit.backend.hidratacion.dto.AguaRequest;
import com.nutrifit.backend.hidratacion.dto.AguaResponse;
import com.nutrifit.backend.hidratacion.dto.HidratacionDiariaResponse;
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
class HidratacionControllerIT extends BaseIntegrationTest {

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

    private HttpEntity<AguaRequest> createAguaRequest(String token, AguaRequest body) {
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
    void testRegistrarYConsultarHidratacion() {
        // Register user
        ResponseEntity<AuthResponse> registerResponse = restTemplate.postForEntity(
                baseUrl() + "/api/auth/register",
                buildRegisterRequest("hidratacion_test@example.com"),
                AuthResponse.class
        );
        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        AuthResponse auth = registerResponse.getBody();
        assertThat(auth).isNotNull();
        Long usuarioId = auth.getUsuarioId();
        String token = auth.getToken();

        // Register agua
        AguaRequest aguaReq = new AguaRequest();
        aguaReq.setFecha(LocalDate.now());
        aguaReq.setCantidadMl(250);

        ResponseEntity<AguaResponse> registerAguaResponse = restTemplate.exchange(
                baseUrl() + "/api/hidratacion?usuarioId=" + usuarioId,
                HttpMethod.POST,
                createAguaRequest(token, aguaReq),
                AguaResponse.class
        );

        assertThat(registerAguaResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        AguaResponse registeredAgua = registerAguaResponse.getBody();
        assertThat(registeredAgua).isNotNull();
        assertThat(registeredAgua.getCantidadMl()).isEqualTo(250);

        // Register another batch
        AguaRequest aguaReq2 = new AguaRequest();
        aguaReq2.setFecha(LocalDate.now());
        aguaReq2.setCantidadMl(250);

        restTemplate.exchange(
                baseUrl() + "/api/hidratacion?usuarioId=" + usuarioId,
                HttpMethod.POST,
                createAguaRequest(token, aguaReq2),
                AguaResponse.class
        );

        // Get daily summary
        ResponseEntity<HidratacionDiariaResponse> getDiarioResponse = restTemplate.exchange(
                baseUrl() + "/api/hidratacion?usuarioId=" + usuarioId + "&fecha=" + LocalDate.now(),
                HttpMethod.GET,
                createAuthenticatedHeaders(token),
                HidratacionDiariaResponse.class
        );

        assertThat(getDiarioResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        HidratacionDiariaResponse diario = getDiarioResponse.getBody();
        assertThat(diario).isNotNull();
        assertThat(diario.getFecha()).isEqualTo(LocalDate.now());
        assertThat(diario.getTotalMl()).isEqualTo(500);
        assertThat(diario.getRegistros()).hasSize(2);
    }
}
