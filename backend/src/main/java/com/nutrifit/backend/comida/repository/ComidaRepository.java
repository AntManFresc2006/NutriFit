package com.nutrifit.backend.comida.repository;

import com.nutrifit.backend.comida.dto.ComidaItemDetalleResponse;
import com.nutrifit.backend.comida.model.Comida;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Contrato de acceso a datos para comidas.
 */
public interface ComidaRepository {

    List<Comida> findByUsuarioAndFecha(Long usuarioId, LocalDate fecha);

    Optional<Comida> findById(Long id);

    Comida save(Comida comida);

    void addAlimentoToComida(Long comidaId, Long alimentoId, double gramos);

    List<ComidaItemDetalleResponse> findDetalleItemsByComidaId(Long comidaId);
}