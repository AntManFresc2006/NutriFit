package com.nutrifit.backend.ejercicio.service;

import com.nutrifit.backend.common.exception.ResourceNotFoundException;
import com.nutrifit.backend.ejercicio.dto.RegistroEjercicioRequest;
import com.nutrifit.backend.ejercicio.dto.RegistroEjercicioResponse;
import com.nutrifit.backend.ejercicio.model.Ejercicio;
import com.nutrifit.backend.ejercicio.model.RegistroEjercicio;
import com.nutrifit.backend.ejercicio.repository.EjercicioRepository;
import com.nutrifit.backend.ejercicio.repository.RegistroEjercicioRepository;
import com.nutrifit.backend.perfil.model.Perfil;
import com.nutrifit.backend.perfil.repository.PerfilRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class RegistroEjercicioServiceImpl implements RegistroEjercicioService {

    private final RegistroEjercicioRepository registroRepository;
    private final EjercicioRepository ejercicioRepository;
    private final PerfilRepository perfilRepository;

    public RegistroEjercicioServiceImpl(RegistroEjercicioRepository registroRepository,
                                        EjercicioRepository ejercicioRepository,
                                        PerfilRepository perfilRepository) {
        this.registroRepository = registroRepository;
        this.ejercicioRepository = ejercicioRepository;
        this.perfilRepository = perfilRepository;
    }

    @Override
    public List<RegistroEjercicioResponse> findByUsuarioAndFecha(Long usuarioId, LocalDate fecha) {
        return registroRepository.findByUsuarioAndFecha(usuarioId, fecha);
    }

    @Override
    public RegistroEjercicioResponse registrar(Long usuarioId, RegistroEjercicioRequest request) {
        Ejercicio ejercicio = ejercicioRepository.findById(request.getEjercicioId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe un ejercicio con id " + request.getEjercicioId()));

        Perfil perfil = perfilRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe un usuario con id " + usuarioId));

        double kcalQuemadas = calcularKcal(ejercicio.getMet(), perfil.getPesoKgActual(), request.getDuracionMin());

        RegistroEjercicio registro = new RegistroEjercicio();
        registro.setUsuarioId(usuarioId);
        registro.setEjercicioId(ejercicio.getId());
        registro.setFecha(request.getFecha());
        registro.setDuracionMin(request.getDuracionMin());
        registro.setKcalQuemadas(kcalQuemadas);

        RegistroEjercicio guardado = registroRepository.save(registro);

        return new RegistroEjercicioResponse(
                guardado.getId(),
                guardado.getUsuarioId(),
                guardado.getEjercicioId(),
                ejercicio.getNombre(),
                guardado.getFecha(),
                guardado.getDuracionMin(),
                guardado.getKcalQuemadas()
        );
    }

    @Override
    public void deleteById(Long usuarioId, Long registroId) {
        RegistroEjercicio registro = registroRepository.findById(registroId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe un registro de ejercicio con id " + registroId));

        if (!registro.getUsuarioId().equals(usuarioId)) {
            throw new ResourceNotFoundException(
                    "El registro " + registroId + " no pertenece al usuario " + usuarioId);
        }

        registroRepository.deleteById(registroId);
    }

    /**
     * Fórmula MET: kcal = MET × peso_kg × (duracion_min / 60.0)
     * Resultado redondeado a 2 decimales.
     */
    static double calcularKcal(double met, double pesoKg, int duracionMin) {
        double resultado = met * pesoKg * (duracionMin / 60.0);
        return BigDecimal.valueOf(resultado)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
