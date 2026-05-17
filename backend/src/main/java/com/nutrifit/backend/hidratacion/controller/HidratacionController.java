package com.nutrifit.backend.hidratacion.controller;

import com.nutrifit.backend.hidratacion.dto.AguaRequest;
import com.nutrifit.backend.hidratacion.dto.AguaResponse;
import com.nutrifit.backend.hidratacion.dto.HidratacionDiariaResponse;
import com.nutrifit.backend.hidratacion.service.HidratacionService;
import com.nutrifit.backend.common.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/hidratacion")
public class HidratacionController {

    private final HidratacionService service;

    public HidratacionController(HidratacionService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AguaResponse registrar(
            @RequestParam Long usuarioId,
            @Valid @RequestBody AguaRequest request,
            HttpServletRequest httpRequest) {
        Long authId = (Long) httpRequest.getAttribute("authenticatedUserId");
        if (!usuarioId.equals(authId)) {
            throw new UnauthorizedException("Acceso denegado");
        }
        return service.registrar(usuarioId, request);
    }

    @GetMapping
    public HidratacionDiariaResponse getDiario(
            @RequestParam Long usuarioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            HttpServletRequest request) {
        Long authId = (Long) request.getAttribute("authenticatedUserId");
        if (!usuarioId.equals(authId)) {
            throw new UnauthorizedException("Acceso denegado");
        }
        return service.getDiario(usuarioId, fecha);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(
            @PathVariable Long id,
            @RequestParam Long usuarioId,
            HttpServletRequest request) {
        Long authId = (Long) request.getAttribute("authenticatedUserId");
        if (!usuarioId.equals(authId)) {
            throw new UnauthorizedException("Acceso denegado");
        }
        service.eliminar(usuarioId, id);
    }
}
