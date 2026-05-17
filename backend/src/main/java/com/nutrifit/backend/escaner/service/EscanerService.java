package com.nutrifit.backend.escaner.service;

import com.nutrifit.backend.escaner.dto.EscanerResponse;

public interface EscanerService {
    EscanerResponse buscarPorBarcode(String barcode);
}
