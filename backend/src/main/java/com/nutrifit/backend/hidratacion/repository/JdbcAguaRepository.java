package com.nutrifit.backend.hidratacion.repository;

import com.nutrifit.backend.hidratacion.dto.AguaRequest;
import com.nutrifit.backend.hidratacion.dto.AguaResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC del repositorio de registros de agua. Gestiona la tabla agua_registro.
 */
@Repository
public class JdbcAguaRepository implements AguaRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcAguaRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public AguaResponse save(Long usuarioId, AguaRequest request) {
        LocalTime ahora = LocalTime.now();
        String sql = """
                INSERT INTO agua_registro (usuario_id, fecha, cantidad_ml, hora)
                VALUES (?, ?, ?, ?)
                RETURNING id, fecha, cantidad_ml, hora
                """;

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new AguaResponse(
                rs.getLong("id"),
                rs.getDate("fecha").toLocalDate(),
                rs.getInt("cantidad_ml"),
                rs.getTime("hora").toLocalTime()
        ), usuarioId, request.getFecha(), request.getCantidadMl(), ahora);
    }

    @Override
    public List<AguaResponse> findByUsuarioAndFecha(Long usuarioId, LocalDate fecha) {
        String sql = """
                SELECT id, fecha, cantidad_ml, hora
                FROM agua_registro
                WHERE usuario_id = ? AND fecha = ?
                ORDER BY hora DESC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new AguaResponse(
                rs.getLong("id"),
                rs.getDate("fecha").toLocalDate(),
                rs.getInt("cantidad_ml"),
                rs.getTime("hora").toLocalTime()
        ), usuarioId, fecha);
    }

    @Override
    public Optional<AguaResponse> findById(Long id) {
        String sql = """
                SELECT id, fecha, cantidad_ml, hora, usuario_id
                FROM agua_registro
                WHERE id = ?
                """;

        List<AguaResponse> results = jdbcTemplate.query(sql, (rs, rowNum) -> new AguaResponse(
                rs.getLong("id"),
                rs.getDate("fecha").toLocalDate(),
                rs.getInt("cantidad_ml"),
                rs.getTime("hora").toLocalTime()
        ), id);

        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM agua_registro WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public Long findUsuarioIdByRegistroId(Long registroId) {
        String sql = "SELECT usuario_id FROM agua_registro WHERE id = ?";
        List<Long> results = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("usuario_id"), registroId);
        return results.isEmpty() ? null : results.get(0);
    }
}
