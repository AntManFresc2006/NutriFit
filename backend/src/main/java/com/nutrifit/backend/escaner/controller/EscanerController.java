package com.nutrifit.backend.escaner.controller;

import com.nutrifit.backend.escaner.dto.EscanerResponse;
import com.nutrifit.backend.escaner.service.EscanerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Escáner", description = "Búsqueda de alimentos por código de barras")
@RestController
@RequestMapping("/api/escaner")
public class EscanerController {

    private final EscanerService escanerService;

    public EscanerController(EscanerService escanerService) {
        this.escanerService = escanerService;
    }

    @Operation(summary = "Buscar alimento por código de barras")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Alimento encontrado"),
        @ApiResponse(responseCode = "404", description = "Alimento no encontrado")
    })
    @GetMapping("/{barcode}")
    public EscanerResponse buscarPorBarcode(@PathVariable String barcode) {
        return escanerService.buscarPorBarcode(barcode);
    }
}
