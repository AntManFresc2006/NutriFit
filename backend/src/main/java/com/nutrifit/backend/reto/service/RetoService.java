package com.nutrifit.backend.reto.service;

import com.nutrifit.backend.reto.dto.AceptarRetoRequest;
import com.nutrifit.backend.reto.dto.RetoResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio para gestionar retos y el progreso del usuario en los mismos.
 */
public interface RetoService {
    /**
     * Obtiene todos los retos disponibles con el estado del usuario.
     * @param usuarioId ID del usuario.
     * @return Lista de retos con estado.
     */
    List<RetoResponse> getRetos(Long usuarioId);

    /**
     * Registra la aceptación de un reto por parte de un usuario.
     * @param usuarioId ID del usuario.
     * @param req Solicitud con ID del reto.
     * @return Reto aceptado con estado inicial.
     * @throws com.nutrifit.backend.common.exception.ResourceNotFoundException si el reto no existe.
     */
    RetoResponse aceptarReto(Long usuarioId, AceptarRetoRequest req);

    /**
     * Sincroniza el progreso de los retos activos de un usuario basándose en su actividad del día.
     * @param usuarioId ID del usuario.
     * @param fecha Fecha a sincronizar.
     * @return Lista de retos completados en esa fecha.
     */
    List<RetoResponse> sincronizarProgreso(Long usuarioId, LocalDate fecha);

    /**
     * Elimina la participación de un usuario en un reto.
     * @param usuarioId ID del usuario.
     * @param usuarioRetoId ID del registro usuario-reto.
     * @throws com.nutrifit.backend.common.exception.ResourceNotFoundException si el reto no existe.
     */
    void abandonarReto(Long usuarioId, Long usuarioRetoId);
}
