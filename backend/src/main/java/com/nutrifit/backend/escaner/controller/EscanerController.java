package com.nutrifit.backend.escaner.controller;

import com.nutrifit.backend.escaner.dto.EscanerResponse;
import com.nutrifit.backend.escaner.service.EscanerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/escaner")
public class EscanerController {

    private final EscanerService escanerService;

    public EscanerController(EscanerService escanerService) {
        this.escanerService = escanerService;
    }

    @GetMapping("/{barcode}")
    public EscanerResponse buscarPorBarcode(@PathVariable String barcode) {
        return escanerService.buscarPorBarcode(barcode);
    }
}
