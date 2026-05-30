package com.nutrifit.backend.detective.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutrifit.backend.detective.dto.DetectiveAnalisisDto;
import com.nutrifit.backend.detective.dto.DetectiveStatsDto;
import com.nutrifit.backend.detective.dto.HallazgoDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcDetectiveRepository implements DetectiveRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JdbcDetectiveRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Long upsertAnalizando(Long usuarioId, int diasAnalizados,
                                 String estadisticasJson, String hallazgosJson) {
        String sql = """
                INSERT INTO detective_analisis
                    (usuario_id, dias_analizados, estadisticas_json, hallazgos_json,
                     estado, analisis_ia, error_msg, created_at)
                VALUES (?, ?, ?, ?, 'ANALIZANDO', NULL, NULL, NOW())
                ON CONFLICT (usuario_id) DO UPDATE
                    SET dias_analizados   = EXCLUDED.dias_analizados,
                        estadisticas_json = EXCLUDED.estadisticas_json,
                        hallazgos_json    = EXCLUDED.hallazgos_json,
                        estado            = 'ANALIZANDO',
                        analisis_ia       = NULL,
                        error_msg         = NULL,
                        created_at        = NOW()
                RETURNING id
                """;
        return jdbcTemplate.queryForObject(sql, Long.class,
                usuarioId, diasAnalizados, estadisticasJson, hallazgosJson);
    }

    @Override
    public void updateIa(Long id, String analisisIa) {
        jdbcTemplate.update(
                "UPDATE detective_analisis SET analisis_ia = ?, estado = 'LISTO' WHERE id = ?",
                analisisIa, id);
    }

    @Override
    public void updateError(Long id, String errorMsg) {
        jdbcTemplate.update(
                "UPDATE detective_analisis SET error_msg = ?, estado = 'ERROR' WHERE id = ?",
                errorMsg, id);
    }

    @Override
    public Optional<DetectiveAnalisisDto> findByUsuario(Long usuarioId) {
        String sql = """
                SELECT id, dias_analizados, estado, estadisticas_json,
                       hallazgos_json, analisis_ia, error_msg
                FROM detective_analisis
                WHERE usuario_id = ?
                """;
        try {
            DetectiveAnalisisDto dto = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                DetectiveAnalisisDto d = new DetectiveAnalisisDto();
                d.setId(rs.getLong("id"));
                d.setDiasAnalizados(rs.getInt("dias_analizados"));
                d.setEstado(rs.getString("estado"));
                d.setAnalisisIa(rs.getString("analisis_ia"));
                d.setErrorMsg(rs.getString("error_msg"));
                try {
                    String statsJson = rs.getString("estadisticas_json");
                    if (statsJson != null) {
                        d.setEstadisticas(objectMapper.readValue(statsJson, DetectiveStatsDto.class));
                    }
                    String hallazgosJson = rs.getString("hallazgos_json");
                    if (hallazgosJson != null) {
                        d.setHallazgos(objectMapper.readValue(hallazgosJson,
                                new TypeReference<List<HallazgoDto>>() {}));
                    }
                } catch (Exception e) {
                    // JSON malformed — leave stats/hallazgos null
                }
                return d;
            }, usuarioId);
            return Optional.ofNullable(dto);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<DiaForenseDto> getDatosNutricionales(Long usuarioId, String fechaInicio, String fechaFin) {
        String sql = """
                SELECT
                    gs.dia::date                                                    AS fecha,
                    COUNT(DISTINCT ca.alimento_id) > 0                             AS tiene_registro,
                    COALESCE(SUM(a.kcal_por_100g  * ca.gramos / 100.0), 0)        AS kcal,
                    COALESCE(SUM(a.proteinas_g    * ca.gramos / 100.0), 0)        AS proteinas
                FROM generate_series(?::date, ?::date, '1 day') AS gs(dia)
                LEFT JOIN comidas           c  ON c.fecha      = gs.dia::date
                                              AND c.usuario_id = ?
                LEFT JOIN comida_alimentos  ca ON ca.comida_id = c.id
                LEFT JOIN alimentos         a  ON a.id         = ca.alimento_id
                GROUP BY gs.dia
                ORDER BY gs.dia
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            LocalDate fecha = rs.getObject("fecha", LocalDate.class);
            boolean tieneRegistro = rs.getBoolean("tiene_registro");
            double kcal = rs.getDouble("kcal");
            double proteinas = rs.getDouble("proteinas");
            return new DiaForenseDto(fecha, tieneRegistro, kcal, proteinas);
        }, fechaInicio, fechaFin, usuarioId);
    }
}
