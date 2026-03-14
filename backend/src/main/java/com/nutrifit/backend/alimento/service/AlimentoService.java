package com.nutrifit.backend.alimento.service;

import com.nutrifit.backend.alimento.dto.AlimentoRequest;
import com.nutrifit.backend.alimento.dto.AlimentoResponse;

import java.util.List;

public interface AlimentoService {

    List<AlimentoResponse> findAll(String query);

    AlimentoResponse findById(Long id);

    AlimentoResponse save(AlimentoRequest request);

    AlimentoResponse update(Long id, AlimentoRequest request);

    boolean deleteById(Long id);
}