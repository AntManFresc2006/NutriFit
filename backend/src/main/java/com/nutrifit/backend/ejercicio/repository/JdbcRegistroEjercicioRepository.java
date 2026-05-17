package com.nutrifit.backend.ejercicio.repository;

import com.nutrifit.backend.ejercicio.dto.RegistroEjercicioResponse;
import com.nutrifit.backend.ejercicio.dto.RecuperacionResponse;
import com.nutrifit.backend.ejercicio.model.RegistroEjercicio;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos JDBC para los registros de actividad física diaria.
 *
 * <p>Las kcal quemadas se almacenan ya calculadas en el momento del INSERT
 * (el servicio las calcula antes de llamar a {@code save}), por lo que las
 * queries de lectura no necesitan hacer ningún cálculo adicional.</p>
 */
@Repository
public class JdbcRegistroEjercicioRepository implements RegistroEjercicioRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcRegistroEjercicioRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Devuelve los registros del día con el nombre del ejercicio ya incluido.
     *
     * <p>Se hace JOIN con {@code ejercicios} para evitar que el cliente tenga que
     * hacer una petición extra al catálogo solo para mostrar el nombre.
     * INNER JOIN es correcto porque un registro sin ejercicio vinculado sería
     * un dato corrupto que no debería mostrarse.</p>
     */
    @Override
    public List<RegistroEjercicioResponse> findByUsuarioAndFecha(Long usuarioId, LocalDate fecha) {
        String sql = """
                SELECT
                    er.id,
                    er.usuario_id,
                    er.ejercicio_id,
                    e.nombre  AS nombre_ejercicio,
                    e.tipo    AS tipo_ejercicio,
                    er.fecha,
                    er.duracion_min,
                    er.kcal_quemadas,
                    er.intensidad,
                    er.num_series
                FROM ejercicios_registro er
                INNER JOIN ejercicios e ON e.id = er.ejercicio_id
                WHERE er.usuario_id = ? AND er.fecha = ?
                ORDER BY er.id ASC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            RegistroEjercicioResponse r = new RegistroEjercicioResponse();
            r.setId(rs.getLong("id"));
            r.setUsuarioId(rs.getLong("usuario_id"));
            r.setEjercicioId(rs.getLong("ejercicio_id"));
            r.setNombreEjercicio(rs.getString("nombre_ejercicio"));
            r.setTipoEjercicio(rs.getString("tipo_ejercicio"));
            r.setFecha(rs.getDate("fecha").toLocalDate());
            r.setDuracionMin(rs.getInt("duracion_min"));
            r.setKcalQuemadas(rs.getDouble("kcal_quemadas"));
            r.setIntensidad(rs.getString("intensidad"));
            int ns = rs.getInt("num_series");
            r.setNumSeries(rs.wasNull() ? null : ns);
            return r;
        }, usuarioId, fecha);
    }

    @Override
    public Optional<RegistroEjercicio> findById(Long id) {
        String sql = """
                SELECT id, usuario_id, ejercicio_id, fecha, duracion_min, kcal_quemadas,
                       intensidad, num_series
                FROM ejercicios_registro
                WHERE id = ?
                """;

        List<RegistroEjercicio> resultados = jdbcTemplate.query(sql, (rs, rowNum) -> {
            RegistroEjercicio r = new RegistroEjercicio();
            r.setId(rs.getLong("id"));
            r.setUsuarioId(rs.getLong("usuario_id"));
            r.setEjercicioId(rs.getLong("ejercicio_id"));
            r.setFecha(rs.getDate("fecha").toLocalDate());
            r.setDuracionMin(rs.getInt("duracion_min"));
            r.setKcalQuemadas(rs.getDouble("kcal_quemadas"));
            r.setIntensidad(rs.getString("intensidad"));
            int ns = rs.getInt("num_series");
            r.setNumSeries(rs.wasNull() ? null : ns);
            return r;
        }, id);

        return resultados.stream().findFirst();
    }

    @Override
    public RegistroEjercicio save(RegistroEjercicio registro) {
        String sql = """
                INSERT INTO ejercicios_registro
                    (usuario_id, ejercicio_id, fecha, duracion_min, kcal_quemadas, intensidad, num_series)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, registro.getUsuarioId());
            ps.setLong(2, registro.getEjercicioId());
            ps.setObject(3, registro.getFecha());
            ps.setInt(4, registro.getDuracionMin());
            ps.setDouble(5, registro.getKcalQuemadas());
            ps.setString(6, registro.getIntensidad());
            if (registro.getNumSeries() != null) {
                ps.setInt(7, registro.getNumSeries());
            } else {
                ps.setNull(7, java.sql.Types.SMALLINT);
            }
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            registro.setId(key.longValue());
        }
        return registro;
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM ejercicios_registro WHERE id = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }

    @Override
    public Optional<RecuperacionResponse> findUltimoIntensivoHoy(Long usuarioId, LocalDate fecha) {
        String sql = """
                SELECT e.nombre AS nombre_ejercicio, e.met
                FROM ejercicios_registro er
                INNER JOIN ejercicios e ON e.id = er.ejercicio_id
                WHERE er.usuario_id = ? AND er.fecha = ? AND e.met > 5.0
                ORDER BY er.id DESC
                LIMIT 1
                """;

        List<RecuperacionResponse> results = jdbcTemplate.query(sql, (rs, rowNum) ->
                new RecuperacionResponse(
                        true,
                        rs.getString("nombre_ejercicio"),
                        rs.getDouble("met"),
                        null,
                        50
                ), usuarioId, fecha);

        return results.stream().findFirst();
    }
}
