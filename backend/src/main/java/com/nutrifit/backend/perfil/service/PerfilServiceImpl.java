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
 * Lógica del perfil de usuario: persistencia de datos biométricos y cálculo de objetivos calóricos.
 *
 * <p>La TMB se calcula con la fórmula Mifflin-St Jeor (1990), que ofrece mejores resultados
 * que la Harris-Benedict clásica especialmente en personas con sobrepeso.
 * El TDEE se obtiene multiplicando la TMB por el factor de nivel de actividad.</p>
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

    /**
     * Tasa Metabólica Basal según Mifflin-St Jeor.
     *
     * <p>Fórmula base: {@code 10×peso + 6.25×altura − 5×edad}<br>
     * Ajuste por sexo: +5 para hombres, −161 para mujeres.<br>
     * La edad se calcula en el momento de la consulta, no se almacena.</p>
     */
    private double calcularTmb(Perfil perfil) {
        int edad = Period.between(perfil.getFechaNacimiento(), LocalDate.now()).getYears();
        double base = 10 * perfil.getPesoKgActual()
                + 6.25 * perfil.getAlturaCm()
                - 5 * edad;
        return perfil.getSexo() == Sexo.H ? base + 5 : base - 161;
    }

    /**
     * Gasto energético total diario (TDEE): TMB multiplicado por el factor
     * de actividad del enum {@code NivelActividad} (1.2 sedentario … 1.9 muy alto).
     */
    private double calcularTdee(Perfil perfil, double tmb) {
        return tmb * perfil.getNivelActividad().getFactor();
    }

    private PerfilResponse toResponse(Perfil perfil) {
        // Redondear a 2 decimales antes de construir la respuesta para no exponer
        // ruido de punto flotante (p.ej. 1495.2500000001) al cliente
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
