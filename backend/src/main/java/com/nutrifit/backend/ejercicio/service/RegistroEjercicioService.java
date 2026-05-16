package com.nutrifit.backend.ejercicio.service;

import com.nutrifit.backend.ejercicio.dto.RegistroEjercicioRequest;
import com.nutrifit.backend.ejercicio.dto.RegistroEjercicioResponse;
import com.nutrifit.backend.ejercicio.dto.RecuperacionResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RegistroEjercicioService {

    List<RegistroEjercicioResponse> findByUsuarioAndFecha(Long usuarioId, LocalDate fecha);

    RegistroEjercicioResponse registrar(Long usuarioId, RegistroEjercicioRequest request);

    void deleteById(Long usuarioId, Long registroId);

    Optional<RecuperacionResponse> findUltimoIntensivoHoy(Long usuarioId, LocalDate fecha);
}
