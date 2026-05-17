package com.nutrifit.backend.comida.controller;

import com.nutrifit.backend.comida.dto.ComidaRequest;
import com.nutrifit.backend.comida.dto.ComidaResponse;
import com.nutrifit.backend.comida.service.ComidaService;
import com.nutrifit.backend.common.exception.UnauthorizedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.nutrifit.backend.comida.dto.ComidaAlimentoRequest;
import com.nutrifit.backend.comida.dto.ComidaItemDetalleResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * Endpoints para gestionar las comidas del día y sus alimentos asociados.
 *
 * <p>Una "comida" es un contenedor tipado (DESAYUNO, ALMUERZO…) que agrupa
 * alimentos con sus gramos consumidos. Los macros se calculan en el momento
 * de la consulta, no se almacenan precalculados.</p>
 */
@Tag(name = "Comidas", description = "Gestión de comidas y sus alimentos asociados")
@RestController
@RequestMapping("/api/comidas")
public class ComidaController {

    private final ComidaService comidaService;

    public ComidaController(ComidaService comidaService) {
        this.comidaService = comidaService;
    }

    /**
     * Devuelve las comidas de un usuario para el día indicado.
     *
     * <p>El cliente usa esto para construir la vista de registro diario antes
     * de cargar los detalles de cada comida por separado.</p>
     *
     * @param usuarioId identificador del usuario autenticado
     * @param fecha     día del que se quieren las comidas (ISO-8601)
     * @return lista de comidas del día, vacía si no hay ninguna
     */
    @Operation(summary = "Obtener comidas del día")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comidas recuperadas exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos")
    })
    @GetMapping
    public List<ComidaResponse> getByUsuarioAndFecha(
            @Parameter(description = "ID del usuario autenticado")
            @RequestParam Long usuarioId,
            @Parameter(description = "Fecha en formato ISO-8601 (yyyy-MM-dd)")
            @RequestParam LocalDate fecha,
            HttpServletRequest request
    ) {
        Long authId = (Long) request.getAttribute("authenticatedUserId");
        if (!usuarioId.equals(authId)) {
            throw new UnauthorizedException("Acceso denegado");
        }
        return comidaService.findByUsuarioAndFecha(usuarioId, fecha);
    }

    /**
     * Crea una comida nueva (p.ej. "DESAYUNO") para el día indicado.
     *
     * <p>El tipo llega en libre, pero el servicio lo normaliza a mayúsculas
     * para evitar duplicados por capitalización inconsistente.</p>
     *
     * @param usuarioId identificador del usuario al que pertenece la comida
     * @param request   fecha y tipo de comida
     * @return la comida creada con su id asignado
     */
    @Operation(summary = "Crear nueva comida")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Comida creada exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ComidaResponse create(
            @Parameter(description = "ID del usuario autenticado")
            @RequestParam Long usuarioId,
            @Valid @RequestBody ComidaRequest request,
            HttpServletRequest httpRequest
    ) {
        Long authId = (Long) httpRequest.getAttribute("authenticatedUserId");
        if (!usuarioId.equals(authId)) {
            throw new UnauthorizedException("Acceso denegado");
        }
        return comidaService.save(usuarioId, request);
    }

    /**
     * Añade un alimento a una comida especificando los gramos consumidos.
     *
     * <p>El servicio valida que tanto la comida como el alimento existan antes
     * de insertar, para no dejar filas huérfanas en {@code comida_alimentos}.</p>
     *
     * @param comidaId id de la comida receptora
     * @param request  id del alimento y gramos a registrar
     */
    @Operation(summary = "Añadir alimento a comida")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Alimento añadido exitosamente"),
            @ApiResponse(responseCode = "404", description = "Comida o alimento no encontrado"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida")
    })
    @PostMapping("/{comidaId}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public void addAlimento(
            @Parameter(description = "ID de la comida")
            @PathVariable Long comidaId,
            @Valid @RequestBody ComidaAlimentoRequest request
    ) {
        comidaService.addAlimentoToComida(comidaId, request);
    }

    /**
     * Elimina una comida junto con todos sus alimentos asociados.
     *
     * <p>El borrado en cascada lo gestiona la FK en base de datos, así que
     * con eliminar la comida es suficiente.</p>
     *
     * @param id id de la comida a eliminar
     */
    @Operation(summary = "Eliminar comida")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Comida eliminada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Comida no encontrada")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Parameter(description = "ID de la comida a eliminar")
            @PathVariable Long id) {
        comidaService.deleteById(id);
    }

    /**
     * Devuelve los alimentos de una comida con sus macros estimados en función
     * de los gramos registrados.
     *
     * <p>Los cálculos se hacen en SQL: {@code (kcal_por_100g * gramos) / 100}.</p>
     *
     * @param comidaId id de la comida cuyos items se quieren consultar
     * @return lista de items con nombre del alimento, gramos y macros estimados
     */
    @Operation(summary = "Obtener alimentos de comida con macros")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Alimentos recuperados exitosamente"),
            @ApiResponse(responseCode = "404", description = "Comida no encontrada")
    })
    @GetMapping("/{comidaId}/items")
    public List<ComidaItemDetalleResponse> getItems(
            @Parameter(description = "ID de la comida")
            @PathVariable Long comidaId) {
        return comidaService.findDetalleItemsByComidaId(comidaId);
    }

    /**
     * Elimina un único alimento de una comida sin borrar la comida entera.
     *
     * <p>El servicio verifica que el item pertenece a la comida indicada para
     * que no sea posible borrar items de otras comidas con solo conocer su id.</p>
     *
     * @param comidaId id de la comida contenedora
     * @param itemId   id del item de {@code comida_alimentos} a eliminar
     */
    @Operation(summary = "Eliminar alimento de comida")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Alimento eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Comida o alimento no encontrado")
    })
    @DeleteMapping("/{comidaId}/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItem(
            @Parameter(description = "ID de la comida")
            @PathVariable Long comidaId,
            @Parameter(description = "ID del item a eliminar")
            @PathVariable Long itemId) {
        comidaService.deleteItem(comidaId, itemId);
    }
}