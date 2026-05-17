package com.nutrifit.backend.auth.service;

import com.nutrifit.backend.auth.dto.AuthResponse;
import com.nutrifit.backend.auth.dto.LoginRequest;
import com.nutrifit.backend.auth.dto.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    void logout(String token);
}