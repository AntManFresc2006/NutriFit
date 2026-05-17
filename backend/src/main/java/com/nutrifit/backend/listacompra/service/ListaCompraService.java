package com.nutrifit.backend.listacompra.service;

import com.nutrifit.backend.listacompra.dto.ListaCompraItemResponse;
import com.nutrifit.backend.listacompra.dto.ListaCompraItemRequest;
import com.nutrifit.backend.listacompra.dto.SugerenciasResponse;

import java.util.Map;
import java.util.List;

public interface ListaCompraService {
    Map<String, List<ListaCompraItemResponse>> getItems(Long usuarioId);
    ListaCompraItemResponse addItem(Long usuarioId, ListaCompraItemRequest req);
    ListaCompraItemResponse toggle(Long usuarioId, Long itemId);
    boolean delete(Long usuarioId, Long itemId);
    void clearCompletados(Long usuarioId);
    SugerenciasResponse getSugerencias(Long usuarioId);
}
