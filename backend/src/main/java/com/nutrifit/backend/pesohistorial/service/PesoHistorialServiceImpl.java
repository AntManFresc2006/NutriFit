package com.nutrifit.backend.pesohistorial.service;

import com.nutrifit.backend.pesohistorial.dto.PesoHistorialResponse;
import com.nutrifit.backend.pesohistorial.repository.PesoHistorialRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PesoHistorialServiceImpl implements PesoHistorialService {

    private final PesoHistorialRepository repository;

    public PesoHistorialServiceImpl(PesoHistorialRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<PesoHistorialResponse> findByUsuario(Long usuarioId, int limit) {
        return repository.findByUsuario(usuarioId, limit);
    }

    @Override
    public PesoHistorialResponse upsert(Long usuarioId, LocalDate fecha, double pesoKg) {
        return repository.upsert(usuarioId, fecha, pesoKg);
    }

    @Override
    public void deleteByUsuarioAndFecha(Long usuarioId, LocalDate fecha) {
        repository.deleteByUsuarioAndFecha(usuarioId, fecha);
    }
}
