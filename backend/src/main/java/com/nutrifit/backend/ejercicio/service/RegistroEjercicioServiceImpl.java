package com.nutrifit.backend.ejercicio.service;

import com.nutrifit.backend.common.exception.ResourceNotFoundException;
import com.nutrifit.backend.ejercicio.dto.RegistroEjercicioRequest;
import com.nutrifit.backend.ejercicio.dto.RegistroEjercicioResponse;
import com.nutrifit.backend.ejercicio.dto.RecuperacionResponse;
import com.nutrifit.backend.ejercicio.model.Ejercicio;
import com.nutrifit.backend.ejercicio.model.RegistroEjercicio;
import com.nutrifit.backend.ejercicio.repository.EjercicioRepository;
import com.nutrifit.backend.ejercicio.repository.RegistroEjercicioRepository;
import com.nutrifit.backend.perfil.model.Perfil;
import com.nutrifit.backend.perfil.repository.PerfilRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Lógica para registrar la actividad física diaria del usuario.
 *
 * <p>Para calcular las kcal quemadas se necesitan el MET del ejercicio y el peso
 * actual del usuario, así que este servicio depende tanto de {@code EjercicioRepository}
 * como de {@code PerfilRepository}.</p>
 */
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
    @Transactional(readOnly = true)
    public List<RegistroEjercicioResponse> findByUsuarioAndFecha(Long usuarioId, LocalDate fecha) {
        return registroRepository.findByUsuarioAndFecha(usuarioId, fecha);
    }

    @Override
    @Transactional
    public RegistroEjercicioResponse registrar(Long usuarioId, RegistroEjercicioRequest request) {
        Ejercicio ejercicio = ejercicioRepository.findById(request.getEjercicioId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe un ejercicio con id " + request.getEjercicioId()));

        // El peso se lee del perfil en el momento del registro; si el usuario lo actualiza
        // después, los registros anteriores mantienen las kcal calculadas con el peso de entonces
        Perfil perfil = perfilRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe un usuario con id " + usuarioId));

        double kcalQuemadas = calcularKcal(ejercicio, perfil.getPesoKgActual(), request.getDuracionMin());

        RegistroEjercicio registro = new RegistroEjercicio();
        registro.setUsuarioId(usuarioId);
        registro.setEjercicioId(ejercicio.getId());
        registro.setFecha(request.getFecha());
        registro.setDuracionMin(request.getDuracionMin());
        registro.setKcalQuemadas(kcalQuemadas);

        RegistroEjercicio guardado = registroRepository.save(registro);

        // El nombre del ejercicio se incluye en la respuesta para que el cliente no tenga que
        // hacer una segunda petición al catálogo solo para mostrarlo
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
    @Transactional
    public void deleteById(Long usuarioId, Long registroId) {
        RegistroEjercicio registro = registroRepository.findById(registroId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe un registro de ejercicio con id " + registroId));

        // Comprobar autoría: un usuario no puede borrar registros de otro aunque conozca el id
        if (!registro.getUsuarioId().equals(usuarioId)) {
            throw new ResourceNotFoundException(
                    "El registro " + registroId + " no pertenece al usuario " + usuarioId);
        }

        registroRepository.deleteById(registroId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RecuperacionResponse> findUltimoIntensivoHoy(Long usuarioId, LocalDate fecha) {
        return registroRepository.findUltimoIntensivoHoy(usuarioId, fecha);
    }

    /**
     * Calcula las kcal quemadas según el tipo de ejercicio.
     *
     * <p>Para ejercicios AEROBICOS: {@code kcal = MET × peso_kg × (duracion_min / 60.0)}<br>
     * El MET (Metabolic Equivalent of Task) representa el coste metabólico relativo al reposo.<br>
     * <br>
     * Para ejercicios ANAEROBICOS: {@code kcal = met_per_serie × num_series}<br>
     * El campo MET para anaeróbicos almacena kcal-por-serie, y duracionMin representa series.
     * Se redondea a 2 decimales para no acumular ruido de punto flotante.</p>
     *
     * @param ejercicio   objeto ejercicio con tipo y MET
     * @param pesoKg      peso actual del usuario en kilogramos
     * @param duracionMin duración (minutos para aeróbicos, series para anaeróbicos)
     * @return kcal quemadas redondeadas a 2 decimales
     */
    static double calcularKcal(Ejercicio ejercicio, double pesoKg, int duracionMin) {
        double resultado;
        if ("ANAEROBICO".equals(ejercicio.getTipo())) {
            resultado = ejercicio.getMet() * duracionMin;
        } else {
            resultado = ejercicio.getMet() * pesoKg * (duracionMin / 60.0);
        }
        return BigDecimal.valueOf(resultado)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
