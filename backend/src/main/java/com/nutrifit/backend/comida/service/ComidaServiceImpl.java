package com.nutrifit.backend.comida.service;

import com.nutrifit.backend.comida.dto.ComidaRequest;
import com.nutrifit.backend.comida.dto.ComidaResponse;
import com.nutrifit.backend.comida.model.Comida;
import com.nutrifit.backend.comida.repository.ComidaRepository;
import org.springframework.stereotype.Service;
import com.nutrifit.backend.comida.dto.ComidaAlimentoRequest;
import com.nutrifit.backend.comida.model.ComidaAlimento;
import com.nutrifit.backend.common.exception.ResourceNotFoundException;
import com.nutrifit.backend.alimento.repository.AlimentoRepository;
import com.nutrifit.backend.comida.dto.ComidaItemDetalleResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * Lógica de negocio del módulo de comidas.
 *
 * <p>Se inyecta {@code AlimentoRepository} además del propio repositorio de comidas
 * para poder validar que el alimento existe antes de añadirlo a una comida,
 * sin depender de que la FK de la base de datos lance un error poco descriptivo.</p>
 */
@Service
public class ComidaServiceImpl implements ComidaService {

    private static final String COMIDA_NO_ENCONTRADA = "No existe una comida con id ";

    private final ComidaRepository comidaRepository;
    private final AlimentoRepository alimentoRepository;

    public ComidaServiceImpl(ComidaRepository comidaRepository, AlimentoRepository alimentoRepository) {
        this.comidaRepository = comidaRepository;
        this.alimentoRepository = alimentoRepository;
    }

    @Override
    public void addAlimentoToComida(Long comidaId, ComidaAlimentoRequest request) {
        // Validar existencia antes de insertar para devolver 404 claro en lugar de error SQL
        comidaRepository.findById(comidaId)
                .orElseThrow(() -> new ResourceNotFoundException(COMIDA_NO_ENCONTRADA + comidaId));

        alimentoRepository.findById(request.getAlimentoId())
                .orElseThrow(() -> new ResourceNotFoundException("No existe un alimento con id " + request.getAlimentoId()));

        comidaRepository.addAlimentoToComida(comidaId, request.getAlimentoId(), request.getGramos());
    }

    @Override
    public List<ComidaItemDetalleResponse> findDetalleItemsByComidaId(Long comidaId) {
        comidaRepository.findById(comidaId)
                .orElseThrow(() -> new ResourceNotFoundException(COMIDA_NO_ENCONTRADA + comidaId));

        return comidaRepository.findDetalleItemsByComidaId(comidaId);
    }

    @Override
    public List<ComidaResponse> findByUsuarioAndFecha(Long usuarioId, LocalDate fecha) {
        return comidaRepository.findByUsuarioAndFecha(usuarioId, fecha)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ComidaResponse save(Long usuarioId, ComidaRequest request) {
        Comida comida = new Comida();
        comida.setUsuarioId(usuarioId);
        comida.setFecha(request.getFecha());
        // Normalizar a mayúsculas para evitar duplicados como "desayuno" y "DESAYUNO"
        comida.setTipo(request.getTipo().trim().toUpperCase());

        Comida guardada = comidaRepository.save(comida);
        return toResponse(guardada);
    }

    @Override
    public void deleteById(Long id) {
        comidaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(COMIDA_NO_ENCONTRADA + id));
        comidaRepository.deleteById(id);
    }

    @Override
    public void deleteItem(Long comidaId, Long itemId) {
        ComidaAlimento item = comidaRepository.findItemById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un item con id " + itemId));

        // Verificar que el item pertenece a la comida indicada para evitar borrados cruzados
        if (!item.getComidaId().equals(comidaId)) {
            throw new ResourceNotFoundException("El item " + itemId + " no pertenece a la comida " + comidaId);
        }

        comidaRepository.deleteItemById(itemId);
    }

    private ComidaResponse toResponse(Comida comida) {
        return new ComidaResponse(
                comida.getId(),
                comida.getUsuarioId(),
                comida.getFecha(),
                comida.getTipo()
        );
    }
}