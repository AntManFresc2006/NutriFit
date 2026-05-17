package com.nutrifit.backend.auth.controller;

import com.nutrifit.backend.auth.dto.AuthResponse;
import com.nutrifit.backend.auth.dto.LoginRequest;
import com.nutrifit.backend.auth.dto.RegisterRequest;
import com.nutrifit.backend.auth.security.LoginRateLimiter;
import com.nutrifit.backend.auth.service.AuthService;
import com.nutrifit.backend.common.exception.TooManyRequestsException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final LoginRateLimiter rateLimiter;

    public AuthController(AuthService authService, LoginRateLimiter rateLimiter) {
        this.authService = authService;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest, HttpServletResponse response) {
        checkRateLimit(httpRequest);
        AuthResponse authResponse = authService.register(request);
        setAuthCookie(response, authResponse.getToken());
        return authResponse;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse response) {
        checkRateLimit(httpRequest);
        AuthResponse authResponse = authService.login(request);
        setAuthCookie(response, authResponse.getToken());
        return authResponse;
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestHeader("Authorization") String authorizationHeader, HttpServletResponse response) {
        String token = authorizationHeader.replace("Bearer ", "").trim();
        authService.logout(token);
        clearAuthCookie(response);
    }

    private void checkRateLimit(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        if (!rateLimiter.permitir(ip)) {
            throw new TooManyRequestsException("Demasiados intentos. Espera un minuto antes de volver a intentarlo.");
        }
    }

    private void setAuthCookie(HttpServletResponse response, String token) {
        String cookie = "nf_session=" + token
            + "; HttpOnly; Secure; SameSite=None; Path=/; Max-Age=86400";
        response.addHeader("Set-Cookie", cookie);
    }

    private void clearAuthCookie(HttpServletResponse response) {
        String cookie = "nf_session=; HttpOnly; Secure; SameSite=None; Path=/; Max-Age=0";
        response.addHeader("Set-Cookie", cookie);
    }
}
