package com.nutrifit.backend.listacompra.repository;

import com.nutrifit.backend.listacompra.model.ListaCompraItem;
import com.nutrifit.backend.listacompra.dto.ListaCompraItemRequest;
import com.nutrifit.backend.listacompra.dto.ListaCompraItemResponse;

import java.util.List;

public interface ListaCompraRepository {
    List<ListaCompraItem> findByUsuario(Long usuarioId);
    ListaCompraItemResponse save(Long usuarioId, ListaCompraItemRequest req);
    ListaCompraItemResponse toggleCompletado(Long id, Long usuarioId);
    boolean deleteById(Long id, Long usuarioId);
    void deleteCompletados(Long usuarioId);
    List<String> findAlimentosMasUsados(Long usuarioId, int limit);
}
