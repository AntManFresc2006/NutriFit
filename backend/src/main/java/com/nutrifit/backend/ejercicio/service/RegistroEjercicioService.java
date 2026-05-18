package com.nutrifit.backend.ejercicio.service;

import com.nutrifit.backend.ejercicio.dto.RegistroEjercicioRequest;
import com.nutrifit.backend.ejercicio.dto.RegistroEjercicioResponse;
import com.nutrifit.backend.ejercicio.dto.RecuperacionResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de lógica de negocio para registros de actividad física del usuario.
 */
public interface RegistroEjercicioService {

    /**
     * Obtiene los registros de ejercicio de un usuario en una fecha.
     *
     * @param usuarioId identificador del usuario
     * @param fecha fecha a consultar
     * @return lista de registros con nombre del ejercicio y kcal calculadas
     */
    List<RegistroEjercicioResponse> findByUsuarioAndFecha(Long usuarioId, LocalDate fecha);

    /**
     * Registra un ejercicio realizado, calculando automáticamente las kcal quemadas.
     *
     * @param usuarioId identificador del usuario propietario
     * @param request ejercicio, duración e intensidad
     * @return registro creado con kcal quemadas ya calculadas
     */
    RegistroEjercicioResponse registrar(Long usuarioId, RegistroEjercicioRequest request);

    /**
     * Elimina un registro de ejercicio del usuario.
     *
     * @param usuarioId identificador del usuario propietario (para validación)
     * @param registroId identificador del registro a eliminar
     * @throws com.nutrifit.backend.common.exception.ResourceNotFoundException si no existe
     * @throws com.nutrifit.backend.common.exception.UnauthorizedException si no pertenece al usuario
     */
    void deleteById(Long usuarioId, Long registroId);

    /**
     * Obtiene el último ejercicio intensivo registrado en una fecha (MET > 5).
     *
     * @param usuarioId identificador del usuario
     * @param fecha fecha a consultar
     * @return datos con sugerencias de recuperación si existe
     */
    Optional<RecuperacionResponse> findUltimoIntensivoHoy(Long usuarioId, LocalDate fecha);
}
