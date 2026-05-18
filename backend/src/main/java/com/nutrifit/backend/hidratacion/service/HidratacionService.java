package com.nutrifit.backend.hidratacion.service;

import com.nutrifit.backend.hidratacion.dto.AguaRequest;
import com.nutrifit.backend.hidratacion.dto.AguaResponse;
import com.nutrifit.backend.hidratacion.dto.HidratacionDiariaResponse;
import java.time.LocalDate;

/**
 * Servicio de lógica de negocio para la gestión de hidratación del usuario.
 */
public interface HidratacionService {

    /**
     * Registra una ingesta de agua del usuario.
     *
     * @param usuarioId identificador del usuario
     * @param request fecha y cantidad de agua en ml
     * @return registro guardado con hora registrada
     */
    AguaResponse registrar(Long usuarioId, AguaRequest request);

    /**
     * Obtiene el resumen diario de hidratación con el total y porcentaje respecto al objetivo.
     *
     * @param usuarioId identificador del usuario
     * @param fecha fecha a consultar
     * @return resumen con listado de registros y estadísticas
     */
    HidratacionDiariaResponse getDiario(Long usuarioId, LocalDate fecha);

    /**
     * Elimina un registro de ingesta de agua.
     *
     * @param usuarioId identificador del usuario propietario (para validación)
     * @param registroId identificador del registro a eliminar
     * @throws com.nutrifit.backend.common.exception.ResourceNotFoundException si no existe
     * @throws com.nutrifit.backend.common.exception.UnauthorizedException si no pertenece al usuario
     */
    void eliminar(Long usuarioId, Long registroId);
}
