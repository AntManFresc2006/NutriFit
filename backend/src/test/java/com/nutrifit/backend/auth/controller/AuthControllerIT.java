package com.nutrifit.backend.auth.controller;

import com.nutrifit.backend.BaseIntegrationTest;
import com.nutrifit.backend.auth.dto.AuthResponse;
import com.nutrifit.backend.auth.dto.LoginRequest;
import com.nutrifit.backend.auth.dto.RegisterRequest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@TestPropertySource(properties = {
    "openrouter.gemma.api.key=test-key",
    "openrouter.deepseek.api.key=test-key"
})
class AuthControllerIT extends BaseIntegrationTest {

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

    @Test
    void testRegistroYLogin() {
        ResponseEntity<AuthResponse> registerResponse = restTemplate.postForEntity(
                baseUrl() + "/api/auth/register",
                buildRegisterRequest("register_login@example.com"),
                AuthResponse.class
        );

        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        AuthResponse auth = registerResponse.getBody();
        assertThat(auth).isNotNull();
        assertThat(auth.getToken()).isNotBlank();
        assertThat(auth.getEmail()).isEqualTo("register_login@example.com");

        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail("register_login@example.com");
        loginReq.setPassword("password123");

        ResponseEntity<AuthResponse> loginResponse = restTemplate.postForEntity(
                baseUrl() + "/api/auth/login",
                loginReq,
                AuthResponse.class
        );

        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResponse.getBody()).isNotNull();
        assertThat(loginResponse.getBody().getToken()).isNotBlank();
        assertThat(loginResponse.getBody().getUsuarioId()).isEqualTo(auth.getUsuarioId());
    }

    @Test
    void testLoginCredencialesInvalidas() {
        restTemplate.postForEntity(
                baseUrl() + "/api/auth/register",
                buildRegisterRequest("bad_creds@example.com"),
                AuthResponse.class
        );

        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail("bad_creds@example.com");
        loginReq.setPassword("wrongpassword");

        ResponseEntity<String> loginResponse = restTemplate.postForEntity(
                baseUrl() + "/api/auth/login",
                loginReq,
                String.class
        );

        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void testAccesoEndpointSinToken() {
        ResponseEntity<AuthResponse> registerResponse = restTemplate.postForEntity(
                baseUrl() + "/api/auth/register",
                buildRegisterRequest("no_token@example.com"),
                AuthResponse.class
        );
        Long usuarioId = registerResponse.getBody().getUsuarioId();

        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl() + "/api/perfil/" + usuarioId,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void testLogout() {
        ResponseEntity<AuthResponse> registerResponse = restTemplate.postForEntity(
                baseUrl() + "/api/auth/register",
                buildRegisterRequest("logout_test@example.com"),
                AuthResponse.class
        );
        String token = registerResponse.getBody().getToken();
        Long usuarioId = registerResponse.getBody().getUsuarioId();

        HttpHeaders logoutHeaders = new HttpHeaders();
        logoutHeaders.setBearerAuth(token);
        ResponseEntity<Void> logoutResponse = restTemplate.exchange(
                baseUrl() + "/api/auth/logout",
                HttpMethod.POST,
                new HttpEntity<>(logoutHeaders),
                Void.class
        );

        assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth(token);
        ResponseEntity<String> protectedResponse = restTemplate.exchange(
                baseUrl() + "/api/perfil/" + usuarioId,
                HttpMethod.GET,
                new HttpEntity<>(authHeaders),
                String.class
        );
        assertThat(protectedResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
