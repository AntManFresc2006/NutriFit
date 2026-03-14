package com.nutrifit.backend.alimento.service;

import com.nutrifit.backend.alimento.dto.AlimentoRequest;
import com.nutrifit.backend.alimento.dto.AlimentoResponse;
import com.nutrifit.backend.alimento.model.Alimento;
import com.nutrifit.backend.alimento.repository.AlimentoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlimentoServiceImpl implements AlimentoService {

    private final AlimentoRepository alimentoRepository;

    public AlimentoServiceImpl(AlimentoRepository alimentoRepository) {
        this.alimentoRepository = alimentoRepository;
    }

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

    @Override
    public AlimentoResponse findById(Long id) {
        Alimento alimento = alimentoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe un alimento con id " + id));

        return toResponse(alimento);
    }

    @Override
    public AlimentoResponse save(AlimentoRequest request) {
        Alimento alimento = toModel(request);
        Alimento guardado = alimentoRepository.save(alimento);
        return toResponse(guardado);
    }

    @Override
    public AlimentoResponse update(Long id, AlimentoRequest request) {
        alimentoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe un alimento con id " + id));

        Alimento alimento = toModel(request);
        Alimento actualizado = alimentoRepository.update(id, alimento);
        return toResponse(actualizado);
    }

    @Override
    public boolean deleteById(Long id) {
        alimentoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe un alimento con id " + id));

        return alimentoRepository.deleteById(id);
    }

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
