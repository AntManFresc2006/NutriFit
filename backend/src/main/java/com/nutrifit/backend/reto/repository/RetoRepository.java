package com.nutrifit.backend.reto.repository;

import com.nutrifit.backend.reto.dto.RetoResponse;
import com.nutrifit.backend.reto.model.Reto;
import com.nutrifit.backend.reto.model.UsuarioReto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RetoRepository {
    List<RetoResponse> findAllWithUserStatus(Long usuarioId);
    Optional<Reto> findById(Long id);
    void aceptarReto(Long usuarioId, Long retoId, LocalDate hoy);
    void actualizarProgreso(Long usuarioRetoId, int progreso, boolean completado);
    List<UsuarioReto> findActiveByUsuario(Long usuarioId);
    boolean abandonarReto(Long usuarioId, Long usuarioRetoId);
    Optional<UsuarioReto> findUsuarioRetoById(Long usuarioRetoId);
    boolean existeUsuarioRetoActivo(Long usuarioId, Long retoId, LocalDate fecha);
}
