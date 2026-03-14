package com.nutrifit.backend.alimento.service;

import com.nutrifit.backend.alimento.dto.AlimentoRequest;
import com.nutrifit.backend.alimento.dto.AlimentoResponse;
import com.nutrifit.backend.alimento.model.Alimento;
import com.nutrifit.backend.alimento.repository.AlimentoRepository;
import com.nutrifit.backend.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementación de la capa de servicio del módulo de alimentos.
 * Aquí se centraliza la lógica de negocio del CRUD, incluyendo:
 * - búsqueda general o filtrada
 * - comprobación de existencia por id
 * - conversión entre DTOs y modelo de dominio
 */
@Service
public class AlimentoServiceImpl implements AlimentoService {

    private final AlimentoRepository alimentoRepository;

    /**
     * Inyección del repositorio encargado del acceso a datos.
     */
    public AlimentoServiceImpl(AlimentoRepository alimentoRepository) {
        this.alimentoRepository = alimentoRepository;
    }

    /**
     * Devuelve todos los alimentos o filtra por nombre si se recibe una búsqueda.
     *
     * @param query texto opcional para filtrar alimentos por nombre
     * @return lista de alimentos en formato DTO de respuesta
     */
    @Override
    public List<AlimentoResponse> findAll(String query) {
        List<Alimento> alimentos;

        if (query == null || query.isBlank()) {
            alimentos = alimentoRepository.findAll();
        } else {
            alimentos = alimentoRepository.searchByNombre(query.trim());
        }

        return alimentos.stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Obtiene un alimento a partir de su id.
     * Si no existe, lanza una excepción controlada que después se transforma en un 404.
     *
     * @param id identificador del alimento
     * @return alimento encontrado en formato DTO de respuesta
     */
    @Override
    public AlimentoResponse findById(Long id) {
        Alimento alimento = alimentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un alimento con id " + id));

        return toResponse(alimento);
    }

    /**
     * Crea un nuevo alimento a partir de los datos recibidos desde la API.
     *
     * @param request datos del alimento enviados por el cliente
     * @return alimento creado en formato DTO de respuesta
     */
    @Override
    public AlimentoResponse save(AlimentoRequest request) {
        Alimento alimento = toModel(request);
        Alimento guardado = alimentoRepository.save(alimento);
        return toResponse(guardado);
    }

    /**
     * Actualiza un alimento existente.
     * Antes de actualizar, se verifica que el id exista en base de datos.
     *
     * @param id identificador del alimento a actualizar
     * @param request nuevos datos del alimento
     * @return alimento actualizado en formato DTO de respuesta
     */
    @Override
    public AlimentoResponse update(Long id, AlimentoRequest request) {
        alimentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un alimento con id " + id));

        Alimento alimento = toModel(request);
        Alimento actualizado = alimentoRepository.update(id, alimento);
        return toResponse(actualizado);
    }

    /**
     * Elimina un alimento existente por su id.
     * Si no existe, se lanza una excepción controlada.
     *
     * @param id identificador del alimento a eliminar
     * @return true si la eliminación se realizó correctamente
     */
    @Override
    public boolean deleteById(Long id) {
        alimentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un alimento con id " + id));

        return alimentoRepository.deleteById(id);
    }

    /**
     * Convierte el DTO de entrada en el modelo interno de dominio.
     *
     * @param request datos recibidos desde el cliente
     * @return objeto Alimento listo para persistir
     */
    private Alimento toModel(AlimentoRequest request) {
        Alimento alimento = new Alimento();
        alimento.setNombre(request.getNombre().trim());
        alimento.setPorcionG(request.getPorcionG());
        alimento.setKcalPor100g(request.getKcalPor100g());
        alimento.setProteinasG(request.getProteinasG());
        alimento.setGrasasG(request.getGrasasG());
        alimento.setCarbosG(request.getCarbosG());
        alimento.setFuente(request.getFuente());
        return alimento;
    }

    /**
     * Convierte el modelo interno de dominio en un DTO de respuesta.
     *
     * @param alimento entidad obtenida o persistida en base de datos
     * @return DTO preparado para enviarse al cliente
     */
    private AlimentoResponse toResponse(Alimento alimento) {
        return new AlimentoResponse(
                alimento.getId(),
                alimento.getNombre(),
                alimento.getPorcionG(),
                alimento.getKcalPor100g(),
                alimento.getProteinasG(),
                alimento.getGrasasG(),
                alimento.getCarbosG(),
                alimento.getFuente()
        );
    }
}