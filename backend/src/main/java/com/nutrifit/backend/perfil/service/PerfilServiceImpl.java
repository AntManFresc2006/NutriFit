package com.nutrifit.backend.perfil.service;

import com.nutrifit.backend.common.exception.ResourceNotFoundException;
import com.nutrifit.backend.perfil.dto.PerfilResponse;
import com.nutrifit.backend.perfil.dto.PerfilUpdateRequest;
import com.nutrifit.backend.perfil.model.Perfil;
import com.nutrifit.backend.perfil.model.Sexo;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;

/**
 * Lógica de negocio del perfil de usuario.
 * Aplica la fórmula Mifflin-St Jeor para calcular TMB y TDEE.
 */
@Service
public class PerfilServiceImpl implements PerfilService {

    private final com.nutrifit.backend.perfil.repository.PerfilRepository perfilRepository;

    public PerfilServiceImpl(com.nutrifit.backend.perfil.repository.PerfilRepository perfilRepository) {
        this.perfilRepository = perfilRepository;
    }

    @Override
    public PerfilResponse getPerfil(Long id) {
        Perfil perfil = perfilRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un perfil con id " + id));
        return toResponse(perfil);
    }

    @Override
    public PerfilResponse updatePerfil(Long id, PerfilUpdateRequest request) {
        Perfil existente = perfilRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un perfil con id " + id));

        existente.setSexo(request.getSexo());
        existente.setFechaNacimiento(request.getFechaNacimiento());
        existente.setAlturaCm(request.getAlturaCm());
        existente.setPesoKgActual(request.getPesoKgActual());
        existente.setPesoObjetivo(request.getPesoObjetivo());
        existente.setNivelActividad(request.getNivelActividad());

        Perfil actualizado = perfilRepository.update(id, existente);
        return toResponse(actualizado);
    }

    // -------------------------------------------------------------------------
    // Cálculo TMB (Mifflin-St Jeor) y TDEE
    // -------------------------------------------------------------------------

    private double calcularTmb(Perfil perfil) {
        int edad = Period.between(perfil.getFechaNacimiento(), LocalDate.now()).getYears();
        double base = 10 * perfil.getPesoKgActual()
                + 6.25 * perfil.getAlturaCm()
                - 5 * edad;
        return perfil.getSexo() == Sexo.H ? base + 5 : base - 161;
    }

    private double calcularTdee(Perfil perfil, double tmb) {
        return tmb * perfil.getNivelActividad().getFactor();
    }

    private PerfilResponse toResponse(Perfil perfil) {
        double tmb = Math.round(calcularTmb(perfil) * 100.0) / 100.0;
        double tdee = Math.round(calcularTdee(perfil, tmb) * 100.0) / 100.0;
        return new PerfilResponse(
                perfil.getId(),
                perfil.getNombre(),
                perfil.getEmail(),
                perfil.getSexo(),
                perfil.getFechaNacimiento(),
                perfil.getAlturaCm(),
                perfil.getPesoKgActual(),
                perfil.getPesoObjetivo(),
                perfil.getNivelActividad(),
                tmb,
                tdee
        );
    }
}
