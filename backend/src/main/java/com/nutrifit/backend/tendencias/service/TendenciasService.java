package com.nutrifit.backend.tendencias.service;

import com.nutrifit.backend.tendencias.dto.TendenciasResponse;

public interface TendenciasService {
    TendenciasResponse getTendencias(Long usuarioId, int dias);
}
