package com.nutrifit.backend.comida.service;

import com.nutrifit.backend.comida.dto.ComidaRequest;
import com.nutrifit.backend.comida.dto.ComidaResponse;
import com.nutrifit.backend.comida.model.Comida;
import com.nutrifit.backend.comida.repository.ComidaRepository;
import org.springframework.stereotype.Service;
import com.nutrifit.backend.comida.dto.ComidaAlimentoRequest;
import com.nutrifit.backend.comida.model.ComidaAlimento;
import com.nutrifit.backend.common.exception.ResourceNotFoundException;
import com.nutrifit.backend.alimento.repository.AlimentoRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Implementación de la lógica de negocio del módulo de comidas.
 */
@Service
public class ComidaServiceImpl implements ComidaService {

    private final ComidaRepository comidaRepository;
    private final AlimentoRepository alimentoRepository;

    public ComidaServiceImpl(ComidaRepository comidaRepository, AlimentoRepository alimentoRepository) {
        this.comidaRepository = comidaRepository;
        this.alimentoRepository = alimentoRepository;
    }

    @Override
    public void addAlimentoToComida(Long comidaId, ComidaAlimentoRequest request) {
        comidaRepository.findById(comidaId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe una comida con id " + comidaId));

        alimentoRepository.findById(request.getAlimentoId())
                .orElseThrow(() -> new ResourceNotFoundException("No existe un alimento con id " + request.getAlimentoId()));

        comidaRepository.addAlimentoToComida(comidaId, request.getAlimentoId(), request.getGramos());
    }

    @Override
    public List<ComidaAlimento> findItemsByComidaId(Long comidaId) {
        comidaRepository.findById(comidaId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe una comida con id " + comidaId));

        return comidaRepository.findItemsByComidaId(comidaId);
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