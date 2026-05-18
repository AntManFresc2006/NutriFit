package com.nutrifit.backend.escaner.service;

import com.nutrifit.backend.escaner.dto.EscanerResponse;

/**
 * Servicio para búsqueda de productos por código de barras.
 * Orquesta consultas a bases de datos externas de códigos de barras.
 */
public interface EscanerService {

    /**
     * Busca un producto alimentario a partir de su código de barras.
     *
     * @param barcode código de barras del producto
     * @return información del producto encontrado
     */
    EscanerResponse buscarPorBarcode(String barcode);
}
