package com.nutrifit.backend.auth.repository;

import com.nutrifit.backend.auth.model.Sesion;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;

/**
 * Implementación JDBC del repositorio de sesiones.
 */
@Repository
public class JdbcSesionRepository implements SesionRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcSesionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Sesion save(Sesion sesion) {
        String sql = """
                INSERT INTO sesiones (usuario_id, token, expires_at)
                VALUES (?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, sesion.getUsuarioId());
            ps.setString(2, sesion.getToken());
            ps.setObject(3, sesion.getExpiresAt());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            sesion.setId(key.longValue());
        }

        return sesion;
    }

    @Override
    public void deleteByToken(String token) {
        String sql = "DELETE FROM sesiones WHERE token = ?";
        jdbcTemplate.update(sql, token);
    }
}
