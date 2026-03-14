package com.nutrifit.backend.comida.service;

import com.nutrifit.backend.comida.dto.ComidaRequest;
import com.nutrifit.backend.comida.dto.ComidaResponse;
import com.nutrifit.backend.comida.model.Comida;
import com.nutrifit.backend.comida.repository.ComidaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Implementación de la lógica de negocio del módulo de comidas.
 */
@Service
public class ComidaServiceImpl implements ComidaService {

    private final ComidaRepository comidaRepository;

    public ComidaServiceImpl(ComidaRepository comidaRepository) {
        this.comidaRepository = comidaRepository;
    }

    @Override
    public List<ComidaResponse> findByUsuarioAndFecha(Long usuarioId, LocalDate fecha) {
        return comidaRepository.findByUsuarioAndFecha(usuarioId, fecha)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ComidaResponse save(Long usuarioId, ComidaRequest request) {
        Comida comida = new Comida();
        comida.setUsuarioId(usuarioId);
        comida.setFecha(request.getFecha());
        comida.setTipo(request.getTipo().trim().toUpperCase());

        Comida guardada = comidaRepository.save(comida);
        return toResponse(guardada);
    }

    private ComidaResponse toResponse(Comida comida) {
        return new ComidaResponse(
                comida.getId(),
                comida.getUsuarioId(),
                comida.getFecha(),
                comida.getTipo()
        );
    }
}