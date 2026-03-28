package com.nutrifit.backend.ejercicio.controller;

import com.nutrifit.backend.ejercicio.dto.RegistroEjercicioRequest;
import com.nutrifit.backend.ejercicio.dto.RegistroEjercicioResponse;
import com.nutrifit.backend.ejercicio.service.RegistroEjercicioService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Endpoints para registrar y consultar la actividad física diaria del usuario.
 *
 * <p>Cada registro vincula un ejercicio del catálogo con una duración; el servicio
 * calcula automáticamente las kcal quemadas usando la fórmula MET × peso × horas.</p>
 */
@RestController
@RequestMapping("/api/ejercicios-registro")
public class RegistroEjercicioController {

    private final RegistroEjercicioService registroService;

    public RegistroEjercicioController(RegistroEjercicioService registroService) {
        this.registroService = registroService;
    }

    /**
     * Devuelve todos los ejercicios registrados por un usuario en una fecha concreta.
     *
     * <p>{@code @DateTimeFormat} es necesario porque Spring no convierte
     * automáticamente parámetros de query a {@code LocalDate} sin esa anotación.</p>
     *
     * @param usuarioId id del usuario autenticado
     * @param fecha     día a consultar en formato ISO-8601 (yyyy-MM-dd)
     * @return lista de registros con nombre del ejercicio y kcal quemadas
     */
    @GetMapping
    public List<RegistroEjercicioResponse> getByFecha(
            @RequestParam Long usuarioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return registroService.findByUsuarioAndFecha(usuarioId, fecha);
    }

    /**
     * Registra un ejercicio realizado por el usuario.
     *
     * <p>Las kcal quemadas se calculan en el servicio a partir del MET del ejercicio
     * y el peso actual del perfil del usuario; no las envía el cliente.</p>
     *
     * @param usuarioId id del usuario autenticado
     * @param request   ejercicio elegido, fecha y duración en minutos
     * @return registro creado con las kcal quemadas ya calculadas
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RegistroEjercicioResponse registrar(
            @RequestParam Long usuarioId,
            @Valid @RequestBody RegistroEjercicioRequest request) {
        return registroService.registrar(usuarioId, request);
    }

    /**
     * Elimina un registro de actividad del usuario.
     *
     * <p>Se recibe {@code usuarioId} como parámetro para que el servicio pueda
     * comprobar que el registro pertenece al usuario antes de borrarlo,
     * evitando que un usuario elimine registros ajenos con solo conocer el id.</p>
     *
     * @param usuarioId id del usuario que debe ser propietario del registro
     * @param id        id del registro a eliminar
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @RequestParam Long usuarioId,
            @PathVariable Long id) {
        registroService.deleteById(usuarioId, id);
    }
}
