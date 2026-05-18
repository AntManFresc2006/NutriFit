package com.nutrifit.backend.pesohistorial.service;

import com.nutrifit.backend.pesohistorial.dto.PesoHistorialResponse;
import com.nutrifit.backend.pesohistorial.repository.PesoHistorialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Implementación del servicio de historial de peso con operaciones de lectura y actualización.
 */
@Service
public class PesoHistorialServiceImpl implements PesoHistorialService {

    private final PesoHistorialRepository repository;

    public PesoHistorialServiceImpl(PesoHistorialRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PesoHistorialResponse> findByUsuario(Long usuarioId, int limit) {
        return repository.findByUsuario(usuarioId, limit);
    }

    @Override
    @Transactional
    public PesoHistorialResponse upsert(Long usuarioId, LocalDate fecha, double pesoKg) {
        return repository.upsert(usuarioId, fecha, pesoKg);
    }

    @Override
    @Transactional
    public void deleteByUsuarioAndFecha(Long usuarioId, LocalDate fecha) {
        repository.deleteByUsuarioAndFecha(usuarioId, fecha);
    }
}
