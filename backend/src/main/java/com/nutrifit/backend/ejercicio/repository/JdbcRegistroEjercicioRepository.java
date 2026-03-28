package com.nutrifit.backend.ejercicio.repository;

import com.nutrifit.backend.ejercicio.dto.RegistroEjercicioResponse;
import com.nutrifit.backend.ejercicio.model.RegistroEjercicio;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
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
                    e.nombre AS nombre_ejercicio,
                    er.fecha,
                    er.duracion_min,
                    er.kcal_quemadas
                FROM ejercicios_registro er
                INNER JOIN ejercicios e ON e.id = er.ejercicio_id
                WHERE er.usuario_id = ? AND er.fecha = ?
                ORDER BY er.id ASC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new RegistroEjercicioResponse(
                rs.getLong("id"),
                rs.getLong("usuario_id"),
                rs.getLong("ejercicio_id"),
                rs.getString("nombre_ejercicio"),
                rs.getDate("fecha").toLocalDate(),
                rs.getInt("duracion_min"),
                rs.getDouble("kcal_quemadas")
        ), usuarioId, fecha);
    }

    @Override
    public Optional<RegistroEjercicio> findById(Long id) {
        String sql = """
                SELECT id, usuario_id, ejercicio_id, fecha, duracion_min, kcal_quemadas
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
            return r;
        }, id);

        return resultados.stream().findFirst();
    }

    @Override
    public RegistroEjercicio save(RegistroEjercicio registro) {
        String sql = """
                INSERT INTO ejercicios_registro
                    (usuario_id, ejercicio_id, fecha, duracion_min, kcal_quemadas)
                VALUES (?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, registro.getUsuarioId());
            ps.setLong(2, registro.getEjercicioId());
            ps.setObject(3, registro.getFecha());  // LocalDate → DATE sin conversión manual
            ps.setInt(4, registro.getDuracionMin());
            ps.setDouble(5, registro.getKcalQuemadas());
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
}
