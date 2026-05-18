package com.nutrifit.backend.resumen.service;

import com.nutrifit.backend.perfil.service.PerfilService;
import com.nutrifit.backend.resumen.dto.ResumenDiarioResponse;
import com.nutrifit.backend.resumen.repository.ResumenDiarioRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Implementación del servicio de resumen diario.
 *
 * <p>Obtiene los totales del repositorio y los enriquece con el TDEE del perfil,
 * el estado del balance (DÉFICIT / MANTENIMIENTO / SUPERÁVIT) y, si el usuario
 * tiene peso objetivo y hay un balance sostenido, una proyección de fecha estimada.</p>
 */
@Service
public class ResumenDiarioServiceImpl implements ResumenDiarioService {

    private final ResumenDiarioRepository resumenDiarioRepository;
    private final PerfilService perfilService;
    private final JdbcTemplate jdbcTemplate;

    public ResumenDiarioServiceImpl(ResumenDiarioRepository resumenDiarioRepository,
                                    PerfilService perfilService,
                                    JdbcTemplate jdbcTemplate) {
        this.resumenDiarioRepository = resumenDiarioRepository;
        this.perfilService = perfilService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional(readOnly = true)
    public ResumenDiarioResponse obtenerResumenDiario(Long usuarioId, LocalDate fecha) {
        ResumenDiarioResponse resumen = resumenDiarioRepository.obtenerResumenDiario(usuarioId, fecha);
        enriquecerConTdee(resumen, usuarioId);
        enriquecerConFechaObjetivo(resumen, usuarioId);
        return resumen;
    }

    private String estadoDesdeBalance(double balanceReal) {
        if (balanceReal > 100) return "SUPERAVIT";
        if (balanceReal < -100) return "DEFICIT";
        return "MANTENIMIENTO";
    }

    private void enriquecerConTdee(ResumenDiarioResponse resumen, Long usuarioId) {
        double tdee = 0;
        try {
            tdee = perfilService.getPerfil(usuarioId).getTdee();
        } catch (Exception e) {
            // usuario sin perfil: TDEE = 0, balance real = balance neto
        }
        double balanceReal = resumen.getKcalTotales() - tdee - resumen.getKcalQuemadasTotales();
        resumen.setTdee(tdee);
        resumen.setBalanceReal(balanceReal);
        resumen.setEstadoBalance(estadoDesdeBalance(balanceReal));
    }

    private void enriquecerConFechaObjetivo(ResumenDiarioResponse resumen, Long usuarioId) {
        try {
            var perfil = perfilService.getPerfil(usuarioId);
            Double pesoObjetivo = perfil.getPesoObjetivo();

            if (pesoObjetivo == null) {
                return;
            }

            double pesoActual = perfil.getPesoKgActual();
            double mediaBalance = calcularMediaBalanceUltimosDias(usuarioId, 7);

            // Si no hay balance claro en 7 días, usa el balance real del día actual como fallback
            if (mediaBalance == 0) {
                mediaBalance = resumen.getBalanceReal();
            }

            boolean quierePerderPeso = pesoActual > pesoObjetivo;
            boolean quiereGanarPeso = pesoActual < pesoObjetivo;

            if (quierePerderPeso && mediaBalance < -50) {
                double kgToLose = pesoActual - pesoObjetivo;
                int diasNecesarios = (int) Math.ceil((kgToLose * 7700.0) / Math.abs(mediaBalance));
                diasNecesarios = Math.min(diasNecesarios, 1825);
                LocalDate fechaObjetivo = LocalDate.now().plusDays(diasNecesarios);
                resumen.setDiasParaObjetivo(diasNecesarios);
                resumen.setFechaObjetivo(fechaObjetivo.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            } else if (quiereGanarPeso && mediaBalance > 50) {
                double kgToGain = pesoObjetivo - pesoActual;
                int diasNecesarios = (int) Math.ceil((kgToGain * 7700.0) / mediaBalance);
                diasNecesarios = Math.min(diasNecesarios, 1825);
                LocalDate fechaObjetivo = LocalDate.now().plusDays(diasNecesarios);
                resumen.setDiasParaObjetivo(diasNecesarios);
                resumen.setFechaObjetivo(fechaObjetivo.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
        } catch (Exception e) {
            // Si falla el cálculo, dejamos los campos null
        }
    }

    private double calcularMediaBalanceUltimosDias(Long usuarioId, int dias) {
        LocalDate hoy = LocalDate.now();
        LocalDate haceDias = hoy.minusDays(dias - 1L);

        String sql = """
                SELECT AVG(balance_dia) as media_balance
                FROM (
                    SELECT
                        c.fecha,
                        COALESCE(SUM((a.kcal_por_100g * ca.gramos) / 100.0), 0) - ? - COALESCE(SUM(er.kcal_quemadas), 0) AS balance_dia
                    FROM comidas c
                    LEFT JOIN comida_alimentos ca ON ca.comida_id = c.id
                    LEFT JOIN alimentos a ON a.id = ca.alimento_id
                    LEFT JOIN ejercicios_registro er ON er.usuario_id = c.usuario_id AND er.fecha = c.fecha
                    WHERE c.usuario_id = ? AND c.fecha >= ? AND c.fecha <= ?
                    GROUP BY c.fecha
                ) subquery
                """;

        try {
            double tdee = perfilService.getPerfil(usuarioId).getTdee();
            Double media = jdbcTemplate.queryForObject(sql, Double.class, tdee, usuarioId, haceDias, hoy);
            return media != null ? media : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}
