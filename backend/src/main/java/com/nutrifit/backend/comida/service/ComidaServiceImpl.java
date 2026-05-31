package com.nutrifit.backend.comida.service;

import com.nutrifit.backend.comida.dto.ComidaRequest;
import com.nutrifit.backend.comida.dto.ComidaResponse;
import com.nutrifit.backend.comida.model.Comida;
import com.nutrifit.backend.comida.repository.ComidaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nutrifit.backend.comida.dto.ComidaAlimentoRequest;
import com.nutrifit.backend.comida.model.ComidaAlimento;
import com.nutrifit.backend.common.exception.ResourceNotFoundException;
import com.nutrifit.backend.common.exception.UnauthorizedException;
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

    /**
     * Añade un alimento a una comida validando su existencia previamente.
     *
     * @param comidaId identificador de la comida
     * @param request datos del alimento y cantidad
     * @param usuarioId identificador del usuario propietario
     * @throws ResourceNotFoundException si la comida o el alimento no existen
     */
    @Override
    @Transactional
    public void addAlimentoToComida(Long comidaId, ComidaAlimentoRequest request, Long usuarioId) {
        // Verificar existencia y propiedad antes de insertar
        verificarPropiedad(comidaId, usuarioId);

        alimentoRepository.findById(request.getAlimentoId())
                .orElseThrow(() -> new ResourceNotFoundException("No existe un alimento con id " + request.getAlimentoId()));

        comidaRepository.addAlimentoToComida(comidaId, request.getAlimentoId(), request.getGramos());
    }

    /**
     * Obtiene los items de una comida con sus macros calculados.
     *
     * @param comidaId identificador de la comida
     * @param usuarioId identificador del usuario propietario
     * @return lista de items con información nutricional estimada
     * @throws ResourceNotFoundException si la comida no existe
     */
    @Override
    @Transactional(readOnly = true)
    public List<ComidaItemDetalleResponse> findDetalleItemsByComidaId(Long comidaId, Long usuarioId) {
        verificarPropiedad(comidaId, usuarioId);

        return comidaRepository.findDetalleItemsByComidaId(comidaId);
    }

    /**
     * Obtiene las comidas de un usuario para un día específico.
     *
     * @param usuarioId identificador del usuario
     * @param fecha fecha de las comidas buscadas
     * @return lista de comidas del día
     */
    @Override
    @Transactional(readOnly = true)
    public List<ComidaResponse> findByUsuarioAndFecha(Long usuarioId, LocalDate fecha) {
        return comidaRepository.findByUsuarioAndFecha(usuarioId, fecha)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Crea una nueva comida para un usuario normalizando el tipo a mayúsculas.
     *
     * @param usuarioId identificador del usuario propietario
     * @param request datos de la comida a crear
     * @return comida creada con su id asignado
     */
    @Override
    @Transactional
    public ComidaResponse save(Long usuarioId, ComidaRequest request) {
        Comida comida = new Comida();
        comida.setUsuarioId(usuarioId);
        comida.setFecha(request.getFecha());
        // Normalizar a mayúsculas para evitar duplicados como "desayuno" y "DESAYUNO"
        comida.setTipo(request.getTipo().trim().toUpperCase());

        Comida guardada = comidaRepository.save(comida);
        return toResponse(guardada);
    }

    /**
     * Elimina una comida y todos sus items asociados.
     *
     * @param id identificador de la comida a eliminar
     * @throws ResourceNotFoundException si la comida no existe
     */
    @Override
    @Transactional
    public void deleteById(Long id, Long usuarioId) {
        verificarPropiedad(id, usuarioId);
        comidaRepository.deleteById(id);
    }

    /**
     * Elimina un item de comida-alimento validando que la comida pertenece al
     * usuario y que el item pertenece a esa comida.
     *
     * @param comidaId identificador de la comida
     * @param itemId identificador del item a eliminar
     * @param usuarioId identificador del usuario propietario
     * @throws ResourceNotFoundException si el item no existe o no pertenece a la comida
     */
    @Override
    @Transactional
    public void deleteItem(Long comidaId, Long itemId, Long usuarioId) {
        verificarPropiedad(comidaId, usuarioId);

        ComidaAlimento item = comidaRepository.findItemById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un item con id " + itemId));

        // Verificar que el item pertenece a la comida indicada para evitar borrados cruzados
        if (!item.getComidaId().equals(comidaId)) {
            throw new ResourceNotFoundException("El item " + itemId + " no pertenece a la comida " + comidaId);
        }

        comidaRepository.deleteItemById(itemId);
    }

    /**
     * Carga una comida y verifica que pertenece al usuario indicado.
     *
     * @throws ResourceNotFoundException si la comida no existe
     * @throws UnauthorizedException     si la comida pertenece a otro usuario
     */
    private void verificarPropiedad(Long comidaId, Long usuarioId) {
        Comida comida = comidaRepository.findById(comidaId)
                .orElseThrow(() -> new ResourceNotFoundException(COMIDA_NO_ENCONTRADA + comidaId));
        if (!comida.getUsuarioId().equals(usuarioId)) {
            throw new UnauthorizedException("Acceso denegado");
        }
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