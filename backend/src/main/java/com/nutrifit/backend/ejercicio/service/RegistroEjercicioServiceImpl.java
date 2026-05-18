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
 * Implementación del servicio de registros de ejercicio con cálculo automático de kcal.
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

        double kcalQuemadas = calcularKcal(ejercicio, perfil.getPesoKgActual(),
                request.getDuracionMin(), request.getIntensidad(), request.getNumSeries());

        RegistroEjercicio registro = new RegistroEjercicio();
        registro.setUsuarioId(usuarioId);
        registro.setEjercicioId(ejercicio.getId());
        registro.setFecha(request.getFecha());
        registro.setDuracionMin(request.getDuracionMin());
        registro.setKcalQuemadas(kcalQuemadas);
        registro.setIntensidad(request.getIntensidad());
        registro.setNumSeries(request.getNumSeries());

        RegistroEjercicio guardado = registroRepository.save(registro);

        RegistroEjercicioResponse resp = new RegistroEjercicioResponse();
        resp.setId(guardado.getId());
        resp.setUsuarioId(guardado.getUsuarioId());
        resp.setEjercicioId(guardado.getEjercicioId());
        resp.setNombreEjercicio(ejercicio.getNombre());
        resp.setTipoEjercicio(ejercicio.getTipo());
        resp.setFecha(guardado.getFecha());
        resp.setDuracionMin(guardado.getDuracionMin());
        resp.setKcalQuemadas(guardado.getKcalQuemadas());
        resp.setIntensidad(guardado.getIntensidad());
        resp.setNumSeries(guardado.getNumSeries());
        return resp;
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
    static double calcularKcal(Ejercicio ejercicio, double pesoKg, int duracionMin,
                               String intensidad, Integer numSeries) {
        double resultado;
        if ("ANAEROBICO".equals(ejercicio.getTipo())) {
            double factor = switch (intensidad != null ? intensidad : "MEDIA") {
                case "BAJA" -> 2.0;
                case "ALTA" -> 5.5;
                default     -> 3.5;
            };
            resultado = ejercicio.getMet() * factor * (numSeries != null ? numSeries : 1);
        } else {
            resultado = ejercicio.getMet() * pesoKg * (duracionMin / 60.0);
        }
        return BigDecimal.valueOf(resultado)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
