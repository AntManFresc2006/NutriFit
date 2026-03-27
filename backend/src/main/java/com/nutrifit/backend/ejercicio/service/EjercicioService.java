package com.nutrifit.backend.ejercicio.service;

import com.nutrifit.backend.ejercicio.dto.EjercicioRequest;
import com.nutrifit.backend.ejercicio.dto.EjercicioResponse;

import java.util.List;

public interface EjercicioService {

    List<EjercicioResponse> findAll(String query);

    EjercicioResponse findById(Long id);

    EjercicioResponse save(EjercicioRequest request);
}
