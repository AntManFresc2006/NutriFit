package com.nutrifit.backend.reto.repository;

import com.nutrifit.backend.reto.dto.RetoResponse;
import com.nutrifit.backend.reto.model.Reto;
import com.nutrifit.backend.reto.model.UsuarioReto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz de repositorio para gestionar retos y participación de usuarios en retos.
 */
public interface RetoRepository {
    /**
     * Obtiene todos los retos con su estado de aceptación y progreso para un usuario.
     * @param usuarioId ID del usuario.
     * @return Lista de retos con estado.
     */
    List<RetoResponse> findAllWithUserStatus(Long usuarioId);

    /**
     * Busca un reto por ID.
     * @param id ID del reto.
     * @return Reto si existe.
     */
    Optional<Reto> findById(Long id);

    /**
     * Registra la aceptación de un reto por parte de un usuario.
     * @param usuarioId ID del usuario.
     * @param retoId ID del reto.
     * @param hoy Fecha de inicio del reto.
     */
    void aceptarReto(Long usuarioId, Long retoId, LocalDate hoy);

    /**
     * Actualiza el progreso de un usuario en un reto.
     * @param usuarioRetoId ID del registro usuario-reto.
     * @param progreso Valor actual del progreso.
     * @param completado Si el reto está completado.
     */
    void actualizarProgreso(Long usuarioRetoId, int progreso, boolean completado);

    /**
     * Obtiene los retos activos (no completados) de un usuario.
     * @param usuarioId ID del usuario.
     * @return Lista de retos activos.
     */
    List<UsuarioReto> findActiveByUsuario(Long usuarioId);

    /**
     * Elimina la participación de un usuario en un reto.
     * @param usuarioId ID del usuario.
     * @param usuarioRetoId ID del registro usuario-reto.
     * @return true si se eliminó correctamente.
     */
    boolean abandonarReto(Long usuarioId, Long usuarioRetoId);

    /**
     * Busca un registro de participación usuario-reto por ID.
     * @param usuarioRetoId ID del registro usuario-reto.
     * @return Participación si existe.
     */
    Optional<UsuarioReto> findUsuarioRetoById(Long usuarioRetoId);

    /**
     * Verifica si un usuario tiene un reto activo en una fecha específica.
     * @param usuarioId ID del usuario.
     * @param retoId ID del reto.
     * @param fecha Fecha a verificar.
     * @return true si existe un reto activo en esa fecha.
     */
    boolean existeUsuarioRetoActivo(Long usuarioId, Long retoId, LocalDate fecha);
}
