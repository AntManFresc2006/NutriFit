package com.nutrifit.backend.reto.repository;

import com.nutrifit.backend.reto.dto.RetoResponse;
import com.nutrifit.backend.reto.model.Reto;
import com.nutrifit.backend.reto.model.UsuarioReto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC del repositorio de retos.
 * Maneja las tablas 'retos' y 'usuario_retos'.
 */
@Repository
public class JdbcRetoRepository implements RetoRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcRetoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<RetoResponse> findAllWithUserStatus(Long usuarioId) {
        String sql = """
                SELECT r.id, r.titulo, r.descripcion, r.tipo, r.meta_valor, r.duracion_dias, r.puntos, r.icono,
                       ur.id as ur_id, ur.progreso, ur.completado, ur.fecha_fin,
                       CASE WHEN ur.id IS NOT NULL THEN true ELSE false END as aceptado
                FROM retos r
                LEFT JOIN usuario_retos ur ON ur.reto_id = r.id AND ur.usuario_id = ?
                ORDER BY r.puntos DESC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Long urId = rs.getObject("ur_id", Long.class);
            Integer progreso = urId != null ? rs.getInt("progreso") : null;
            LocalDate fechaFin = urId != null ? rs.getDate("fecha_fin").toLocalDate() : null;
            boolean completado = urId != null && rs.getBoolean("completado");

            return new RetoResponse(
                    rs.getLong("id"),
                    rs.getString("titulo"),
                    rs.getString("descripcion"),
                    rs.getString("tipo"),
                    rs.getInt("meta_valor"),
                    rs.getInt("duracion_dias"),
                    rs.getInt("puntos"),
                    rs.getString("icono"),
                    urId,
                    progreso,
                    urId != null,
                    completado,
                    fechaFin
            );
        }, usuarioId);
    }

    @Override
    public Optional<Reto> findById(Long id) {
        String sql = """
                SELECT id, titulo, descripcion, tipo, meta_valor, duracion_dias, puntos, icono
                FROM retos
                WHERE id = ?
                """;

        List<Reto> resultados = jdbcTemplate.query(sql, (rs, rowNum) ->
                new Reto(
                        rs.getLong("id"),
                        rs.getString("titulo"),
                        rs.getString("descripcion"),
                        rs.getString("tipo"),
                        rs.getInt("meta_valor"),
                        rs.getInt("duracion_dias"),
                        rs.getInt("puntos"),
                        rs.getString("icono")
                ), id);

        return resultados.stream().findFirst();
    }

    @Override
    public void aceptarReto(Long usuarioId, Long retoId, LocalDate hoy) {
        String sql = """
                INSERT INTO usuario_retos (usuario_id, reto_id, fecha_inicio, fecha_fin, progreso, completado)
                SELECT ?, ?, ?, ? + (SELECT duracion_dias FROM retos WHERE id = ?) * interval '1 day', 0, false
                """;

        jdbcTemplate.update(sql, usuarioId, retoId, hoy, hoy, retoId);
    }

    @Override
    public void actualizarProgreso(Long usuarioRetoId, int progreso, boolean completado) {
        String sql = """
                UPDATE usuario_retos
                SET progreso = ?, completado = ?, fecha_completado = ?
                WHERE id = ?
                """;

        java.time.LocalDateTime ahora = completado ? java.time.LocalDateTime.now() : null;
        jdbcTemplate.update(sql, progreso, completado, ahora, usuarioRetoId);
    }

    @Override
    public List<UsuarioReto> findActiveByUsuario(Long usuarioId) {
        String sql = """
                SELECT id, usuario_id, reto_id, fecha_inicio, fecha_fin, progreso, completado, fecha_completado
                FROM usuario_retos
                WHERE usuario_id = ? AND completado = false
                ORDER BY fecha_fin ASC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new UsuarioReto(
                        rs.getLong("id"),
                        rs.getLong("usuario_id"),
                        rs.getLong("reto_id"),
                        rs.getDate("fecha_inicio").toLocalDate(),
                        rs.getDate("fecha_fin").toLocalDate(),
                        rs.getInt("progreso"),
                        rs.getBoolean("completado"),
                        rs.getTimestamp("fecha_completado") != null ?
                                rs.getTimestamp("fecha_completado").toLocalDateTime() : null
                ), usuarioId);
    }

    @Override
    public boolean abandonarReto(Long usuarioId, Long usuarioRetoId) {
        String sql = """
                DELETE FROM usuario_retos
                WHERE id = ? AND usuario_id = ?
                """;

        return jdbcTemplate.update(sql, usuarioRetoId, usuarioId) > 0;
    }

    @Override
    public Optional<UsuarioReto> findUsuarioRetoById(Long usuarioRetoId) {
        String sql = """
                SELECT id, usuario_id, reto_id, fecha_inicio, fecha_fin, progreso, completado, fecha_completado
                FROM usuario_retos
                WHERE id = ?
                """;

        List<UsuarioReto> resultados = jdbcTemplate.query(sql, (rs, rowNum) ->
                new UsuarioReto(
                        rs.getLong("id"),
                        rs.getLong("usuario_id"),
                        rs.getLong("reto_id"),
                        rs.getDate("fecha_inicio").toLocalDate(),
                        rs.getDate("fecha_fin").toLocalDate(),
                        rs.getInt("progreso"),
                        rs.getBoolean("completado"),
                        rs.getTimestamp("fecha_completado") != null ?
                                rs.getTimestamp("fecha_completado").toLocalDateTime() : null
                ), usuarioRetoId);

        return resultados.stream().findFirst();
    }

    @Override
    public boolean existeUsuarioRetoActivo(Long usuarioId, Long retoId, LocalDate fecha) {
        String sql = """
                SELECT COUNT(*) FROM usuario_retos
                WHERE usuario_id = ? AND reto_id = ? AND fecha_inicio <= ? AND fecha_fin >= ? AND completado = false
                """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, usuarioId, retoId, fecha, fecha);
        return count != null && count > 0;
    }
}
