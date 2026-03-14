package com.nutrifit.backend.comida.service;

import com.nutrifit.backend.comida.dto.ComidaRequest;
import com.nutrifit.backend.comida.dto.ComidaResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * Contrato de la lógica de negocio del módulo de comidas.
 */
public interface ComidaService {

    List<ComidaResponse> findByUsuarioAndFecha(Long usuarioId, LocalDate fecha);

    ComidaResponse save(Long usuarioId, ComidaRequest request);
}