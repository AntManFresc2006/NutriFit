package com.nutrifit.backend.alimento.repository;

import com.nutrifit.backend.alimento.model.Alimento;

import java.util.List;
import java.util.Optional;

public interface AlimentoRepository {

    List<Alimento> findAll();

    List<Alimento> searchByNombre(String query);

    Optional<Alimento> findById(Long id);

    Alimento save(Alimento alimento);

    Alimento update(Long id, Alimento alimento);

    boolean deleteById(Long id);
}