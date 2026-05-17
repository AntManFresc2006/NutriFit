package com.nutrifit.backend.reto.service;

import com.nutrifit.backend.reto.dto.AceptarRetoRequest;
import com.nutrifit.backend.reto.dto.RetoResponse;
import com.nutrifit.backend.reto.model.Reto;
import com.nutrifit.backend.reto.model.UsuarioReto;
import com.nutrifit.backend.reto.repository.RetoRepository;
import com.nutrifit.backend.common.exception.ResourceNotFoundException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class RetoServiceImpl implements RetoService {

    private final RetoRepository retoRepository;
    private final JdbcTemplate jdbcTemplate;

    public RetoServiceImpl(RetoRepository retoRepository, JdbcTemplate jdbcTemplate) {
        this.retoRepository = retoRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RetoResponse> getRetos(Long usuarioId) {
        return retoRepository.findAllWithUserStatus(usuarioId);
    }

    @Override
    @Transactional
    public RetoResponse aceptarReto(Long usuarioId, AceptarRetoRequest req) {
        Reto reto = retoRepository.findById(req.getRetoId())
                .orElseThrow(() -> new ResourceNotFoundException("Reto no encontrado"));

        LocalDate hoy = LocalDate.now();
        if (retoRepository.existeUsuarioRetoActivo(usuarioId, reto.getId(), hoy)) {
            throw new IllegalStateException("Ya tienes este reto en curso");
        }

        retoRepository.aceptarReto(usuarioId, reto.getId(), hoy);

        return new RetoResponse(
                reto.getId(),
                reto.getTitulo(),
                reto.getDescripcion(),
                reto.getTipo(),
                reto.getMetaValor(),
                reto.getDuracionDias(),
                reto.getPuntos(),
                reto.getIcono(),
                null,
                0,
                true,
                false,
                hoy.plusDays(reto.getDuracionDias())
        );
    }

    @Override
    @Transactional
    public List<RetoResponse> sincronizarProgreso(Long usuarioId, LocalDate fecha) {
        List<UsuarioReto> activos = retoRepository.findActiveByUsuario(usuarioId);
        List<RetoResponse> completados = new ArrayList<>();

        for (UsuarioReto ur : activos) {
            Reto reto = retoRepository.findById(ur.getRetoId())
                    .orElse(null);
            if (reto == null) continue;

            int progreso = calcularProgresoParaReto(usuarioId, reto, ur.getFechaInicio(), ur.getFechaFin());
            boolean completado = progreso >= reto.getMetaValor();

            if (completado && !ur.isCompletado()) {
                retoRepository.actualizarProgreso(ur.getId(), progreso, true);

                completados.add(new RetoResponse(
                        reto.getId(),
                        reto.getTitulo(),
                        reto.getDescripcion(),
                        reto.getTipo(),
                        reto.getMetaValor(),
                        reto.getDuracionDias(),
                        reto.getPuntos(),
                        reto.getIcono(),
                        ur.getId(),
                        progreso,
                        true,
                        true,
                        ur.getFechaFin()
                ));
            } else if (!completado && ur.getProgreso() != progreso) {
                retoRepository.actualizarProgreso(ur.getId(), progreso, false);
            }
        }

        return completados;
    }

    @Override
    @Transactional
    public void abandonarReto(Long usuarioId, Long usuarioRetoId) {
        UsuarioReto ur = retoRepository.findUsuarioRetoById(usuarioRetoId)
                .orElseThrow(() -> new ResourceNotFoundException("Reto no encontrado"));

        if (!ur.getUsuarioId().equals(usuarioId)) {
            throw new IllegalStateException("No puedes abandonar un reto que no es tuyo");
        }

        retoRepository.abandonarReto(usuarioId, usuarioRetoId);
    }

    private int calcularProgresoParaReto(Long usuarioId, Reto reto, LocalDate inicio, LocalDate fin) {
        switch (reto.getTipo()) {
            case "PROTEINA":
                return contarDiasConProteina(usuarioId, inicio, fin);
            case "BALANCE":
                return contarDiasConBalance(usuarioId, inicio, fin);
            case "EJERCICIO":
                return contarDiasConEjercicio(usuarioId, inicio, fin);
            case "VARIEDAD":
                return contarDiasConVariedad(usuarioId, inicio, fin);
            case "RACHA":
                return calcularRachaDesdeFecha(usuarioId, fin);
            case "HIDRATACION":
                return contarDiasConHidratacion(usuarioId, inicio, fin);
            case "NUTRISCORE":
                return contarDiasConNutriScore(usuarioId, inicio, fin);
            default:
                return 0;
        }
    }

    private int contarDiasConProteina(Long usuarioId, LocalDate inicio, LocalDate fin) {
        double pesoActual = getPesoActual(usuarioId);
        double target = pesoActual * 0.8;
        int dias = 0;

        LocalDate dia = inicio;
        while (!dia.isAfter(fin)) {
            double proteina = getProteinaReal(usuarioId, dia);
            if (proteina >= target) dias++;
            dia = dia.plusDays(1);
        }

        return dias;
    }

    private int contarDiasConBalance(Long usuarioId, LocalDate inicio, LocalDate fin) {
        int dias = 0;

        LocalDate dia = inicio;
        while (!dia.isAfter(fin)) {
            double balance = getBalanceReal(usuarioId, dia);
            if (Math.abs(balance) <= 300) dias++;
            dia = dia.plusDays(1);
        }

        return dias;
    }

    private int contarDiasConEjercicio(Long usuarioId, LocalDate inicio, LocalDate fin) {
        String sql = """
                SELECT DISTINCT fecha FROM ejercicios_registro
                WHERE usuario_id = ? AND fecha >= ? AND fecha <= ?
                ORDER BY fecha
                """;

        List<LocalDate> fechas = jdbcTemplate.query(sql, (rs, i) ->
                rs.getDate("fecha").toLocalDate(), usuarioId, inicio, fin);

        return fechas.size();
    }

    private int contarDiasConVariedad(Long usuarioId, LocalDate inicio, LocalDate fin) {
        int dias = 0;

        LocalDate dia = inicio;
        while (!dia.isAfter(fin)) {
            int variedad = getVariedadCount(usuarioId, dia);
            if (variedad >= 4) dias++;
            dia = dia.plusDays(1);
        }

        return dias;
    }

    private int calcularRachaDesdeFecha(Long usuarioId, LocalDate fecha) {
        String sql = "SELECT DISTINCT fecha FROM comidas WHERE usuario_id = ? AND fecha <= ? ORDER BY fecha DESC LIMIT 60";
        List<LocalDate> fechas = jdbcTemplate.query(sql, (rs, i) ->
                rs.getDate("fecha").toLocalDate(), usuarioId, fecha);

        Set<LocalDate> fechasSet = new HashSet<>(fechas);
        int racha = 0;
        LocalDate dia = fecha;
        while (fechasSet.contains(dia)) {
            racha++;
            dia = dia.minusDays(1);
        }

        return racha;
    }

    private int contarDiasConHidratacion(Long usuarioId, LocalDate inicio, LocalDate fin) {
        String sql = """
                SELECT fecha FROM agua_registro
                WHERE usuario_id = ? AND fecha >= ? AND fecha <= ?
                GROUP BY fecha
                HAVING SUM(cantidad_ml) >= 2000
                """;

        List<LocalDate> fechas = jdbcTemplate.query(sql, (rs, i) ->
                rs.getDate("fecha").toLocalDate(), usuarioId, inicio, fin);

        return fechas.size();
    }

    private int contarDiasConNutriScore(Long usuarioId, LocalDate inicio, LocalDate fin) {
        int dias = 0;

        LocalDate dia = inicio;
        while (!dia.isAfter(fin)) {
            if (isNutriScoreGood(usuarioId, dia)) dias++;
            dia = dia.plusDays(1);
        }

        return dias;
    }

    private boolean isNutriScoreGood(Long usuarioId, LocalDate fecha) {
        boolean cumpleProteina = verificarProteina(usuarioId, fecha);
        boolean cumpleBalance = verificarBalance(usuarioId, fecha);
        boolean cumpleEjercicio = verificarEjercicio(usuarioId, fecha);
        boolean cumpleVariedad = verificarVariedad(usuarioId, fecha);

        return cumpleProteina && cumpleBalance && cumpleEjercicio && cumpleVariedad;
    }

    private boolean verificarProteina(Long usuarioId, LocalDate fecha) {
        double proteinaReal = getProteinaReal(usuarioId, fecha);
        double target = getPesoActual(usuarioId) * 0.8;
        return proteinaReal >= target;
    }

    private boolean verificarBalance(Long usuarioId, LocalDate fecha) {
        double balance = getBalanceReal(usuarioId, fecha);
        return Math.abs(balance) <= 300;
    }

    private boolean verificarEjercicio(Long usuarioId, LocalDate fecha) {
        String sql = "SELECT COUNT(*) FROM ejercicios_registro WHERE usuario_id = ? AND fecha = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, usuarioId, fecha);
        return count != null && count > 0;
    }

    private boolean verificarVariedad(Long usuarioId, LocalDate fecha) {
        return getVariedadCount(usuarioId, fecha) >= 4;
    }

    private double getProteinaReal(Long usuarioId, LocalDate fecha) {
        String sql = """
                SELECT COALESCE(SUM((a.proteinas_g * ca.gramos) / 100.0), 0) AS proteinas
                FROM comidas c
                LEFT JOIN comida_alimentos ca ON ca.comida_id = c.id
                LEFT JOIN alimentos a ON a.id = ca.alimento_id
                WHERE c.usuario_id = ? AND c.fecha = ?
                """;
        Double proteinas = jdbcTemplate.queryForObject(sql, Double.class, usuarioId, fecha);
        return proteinas != null ? proteinas : 0.0;
    }

    private double getBalanceReal(Long usuarioId, LocalDate fecha) {
        try {
            double kcalConsumidas = getKcalConsumidas(usuarioId, fecha);
            double kcalQuemadas = getKcalQuemadas(usuarioId, fecha);
            double tdee = getTdee(usuarioId);
            return kcalConsumidas - tdee - kcalQuemadas;
        } catch (Exception e) {
            return 0.0;
        }
    }

    private double getPesoActual(Long usuarioId) {
        String sql = "SELECT peso_kg_actual FROM usuarios WHERE id = ?";
        try {
            Double peso = jdbcTemplate.queryForObject(sql, Double.class, usuarioId);
            return peso != null ? peso : 70.0;
        } catch (Exception e) {
            return 70.0;
        }
    }

    private double getTdee(Long usuarioId) {
        String sql = """
                SELECT peso_kg_actual, altura_cm, sexo, fecha_nacimiento, nivel_actividad
                FROM usuarios WHERE id = ?
                """;
        try {
            Double tdee = jdbcTemplate.queryForObject(sql, (rs, i) -> {
                double peso  = rs.getDouble("peso_kg_actual");
                int altura   = rs.getInt("altura_cm");
                String sexo  = rs.getString("sexo");
                int edad     = java.time.Period.between(
                        rs.getDate("fecha_nacimiento").toLocalDate(),
                        java.time.LocalDate.now()).getYears();
                String act   = rs.getString("nivel_actividad");

                double bmr = "H".equals(sexo)
                        ? 88.362 + (13.397 * peso) + (4.799 * altura) - (5.677 * edad)
                        : 447.593 + (9.247 * peso) + (3.098 * altura) - (4.330 * edad);

                double mult = switch (act) {
                    case "LIGERO"   -> 1.375;
                    case "MODERADO" -> 1.55;
                    case "ALTO"     -> 1.725;
                    case "MUY_ALTO" -> 1.9;
                    default         -> 1.2;
                };
                return bmr * mult;
            }, usuarioId);
            return tdee != null ? tdee : 2000.0;
        } catch (Exception e) {
            return 2000.0;
        }
    }

    private double getKcalConsumidas(Long usuarioId, LocalDate fecha) {
        String sql = """
                SELECT COALESCE(SUM((a.kcal_por_100g * ca.gramos) / 100.0), 0) AS kcal
                FROM comidas c
                LEFT JOIN comida_alimentos ca ON ca.comida_id = c.id
                LEFT JOIN alimentos a ON a.id = ca.alimento_id
                WHERE c.usuario_id = ? AND c.fecha = ?
                """;
        Double kcal = jdbcTemplate.queryForObject(sql, Double.class, usuarioId, fecha);
        return kcal != null ? kcal : 0.0;
    }

    private double getKcalQuemadas(Long usuarioId, LocalDate fecha) {
        String sql = "SELECT COALESCE(SUM(kcal_quemadas), 0) FROM ejercicios_registro WHERE usuario_id = ? AND fecha = ?";
        Double kcal = jdbcTemplate.queryForObject(sql, Double.class, usuarioId, fecha);
        return kcal != null ? kcal : 0.0;
    }

    private int getVariedadCount(Long usuarioId, LocalDate fecha) {
        String sql = """
                SELECT COUNT(DISTINCT ca.alimento_id) AS variedad
                FROM comidas c
                INNER JOIN comida_alimentos ca ON ca.comida_id = c.id
                WHERE c.usuario_id = ? AND c.fecha = ?
                """;
        Integer variedad = jdbcTemplate.queryForObject(sql, Integer.class, usuarioId, fecha);
        return variedad != null ? variedad : 0;
    }
}
