package com.nutrifit.backend.ejercicio.service;

import com.nutrifit.backend.common.exception.ResourceNotFoundException;
import com.nutrifit.backend.ejercicio.dto.EjercicioRequest;
import com.nutrifit.backend.ejercicio.dto.EjercicioResponse;
import com.nutrifit.backend.ejercicio.model.Ejercicio;
import com.nutrifit.backend.ejercicio.repository.EjercicioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EjercicioServiceImpl implements EjercicioService {

    private final EjercicioRepository ejercicioRepository;

    public EjercicioServiceImpl(EjercicioRepository ejercicioRepository) {
        this.ejercicioRepository = ejercicioRepository;
    }

    @Override
    public List<EjercicioResponse> findAll(String query) {
        List<Ejercicio> ejercicios;
        if (query == null || query.isBlank()) {
            ejercicios = ejercicioRepository.findAll();
        } else {
            ejercicios = ejercicioRepository.searchByNombre(query.trim());
        }
        return ejercicios.stream().map(this::toResponse).toList();
    }

    @Override
    public EjercicioResponse findById(Long id) {
        Ejercicio ejercicio = ejercicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un ejercicio con id " + id));
        return toResponse(ejercicio);
    }

    @Override
    public EjercicioResponse save(EjercicioRequest request) {
        Ejercicio ejercicio = new Ejercicio();
        ejercicio.setNombre(request.getNombre().trim());
        ejercicio.setMet(request.getMet());
        ejercicio.setCategoria(request.getCategoria());
        return toResponse(ejercicioRepository.save(ejercicio));
    }

    private EjercicioResponse toResponse(Ejercicio e) {
        return new EjercicioResponse(e.getId(), e.getNombre(), e.getMet(), e.getCategoria());
    }
}
