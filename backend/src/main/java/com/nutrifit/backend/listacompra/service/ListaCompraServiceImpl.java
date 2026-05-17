package com.nutrifit.backend.listacompra.service;

import com.nutrifit.backend.listacompra.dto.ListaCompraItemResponse;
import com.nutrifit.backend.listacompra.dto.ListaCompraItemRequest;
import com.nutrifit.backend.listacompra.dto.SugerenciasResponse;
import com.nutrifit.backend.listacompra.model.ListaCompraItem;
import com.nutrifit.backend.listacompra.repository.ListaCompraRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

@Service
public class ListaCompraServiceImpl implements ListaCompraService {

    private final ListaCompraRepository repository;

    public ListaCompraServiceImpl(ListaCompraRepository repository) {
        this.repository = repository;
    }

    @Override
    public Map<String, List<ListaCompraItemResponse>> getItems(Long usuarioId) {
        List<ListaCompraItem> items = repository.findByUsuario(usuarioId);

        Map<String, List<ListaCompraItemResponse>> grouped = items.stream()
                .collect(Collectors.groupingBy(
                        ListaCompraItem::getCategoria,
                        LinkedHashMap::new,
                        Collectors.mapping(this::toResponse, Collectors.toList())
                ));

        return grouped;
    }

    @Override
    public ListaCompraItemResponse addItem(Long usuarioId, ListaCompraItemRequest req) {
        if (req.getCategoria() == null) {
            req.setCategoria("OTROS");
        }
        return repository.save(usuarioId, req);
    }

    @Override
    public ListaCompraItemResponse toggle(Long usuarioId, Long itemId) {
        return repository.toggleCompletado(itemId, usuarioId);
    }

    @Override
    public boolean delete(Long usuarioId, Long itemId) {
        return repository.deleteById(itemId, usuarioId);
    }

    @Override
    public void clearCompletados(Long usuarioId) {
        repository.deleteCompletados(usuarioId);
    }

    @Override
    public SugerenciasResponse getSugerencias(Long usuarioId) {
        List<String> sugerencias = repository.findAlimentosMasUsados(usuarioId, 10);
        return new SugerenciasResponse(sugerencias);
    }

    private ListaCompraItemResponse toResponse(ListaCompraItem item) {
        return new ListaCompraItemResponse(
                item.getId(),
                item.getNombre(),
                item.getCantidad(),
                item.getCategoria(),
                item.isCompletado(),
                item.getCreatedAt()
        );
    }
}
