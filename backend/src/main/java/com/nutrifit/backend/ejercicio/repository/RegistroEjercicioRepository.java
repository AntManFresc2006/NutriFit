package com.nutrifit.backend.ejercicio.repository;

import com.nutrifit.backend.ejercicio.dto.RegistroEjercicioResponse;
import com.nutrifit.backend.ejercicio.model.RegistroEjercicio;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RegistroEjercicioRepository {

    List<RegistroEjercicioResponse> findByUsuarioAndFecha(Long usuarioId, LocalDate fecha);

    Optional<RegistroEjercicio> findById(Long id);

    RegistroEjercicio save(RegistroEjercicio registro);

    boolean deleteById(Long id);
}
