package com.nutrifit.backend.auth.repository;

import com.nutrifit.backend.auth.model.Usuario;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC del repositorio de usuarios para autenticación.
 */
@Repository
public class JdbcUsuarioRepository implements UsuarioRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcUsuarioRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Usuario> findByEmail(String email) {
        String sql = """
                SELECT id, nombre, email, password_hash
                FROM usuarios
                WHERE email = ?
                """;

        List<Usuario> resultados = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Usuario usuario = new Usuario();
            usuario.setId(rs.getLong("id"));
            usuario.setNombre(rs.getString("nombre"));
            usuario.setEmail(rs.getString("email"));
            usuario.setPasswordHash(rs.getString("password_hash"));
            return usuario;
        }, email);

        return resultados.stream().findFirst();
    }

    @Override
    public Usuario save(Usuario usuario) {
        String sql = """
                INSERT INTO usuarios (
                    nombre,
                    email,
                    password_hash,
                    sexo,
                    fecha_nacimiento,
                    altura_cm,
                    peso_kg_actual,
                    nivel_actividad
                ) VALUES (?, ?, ?, 'H', '2000-01-01', 170, 70.00, 'SEDENTARIO')
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getEmail());
            ps.setString(3, usuario.getPasswordHash());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            usuario.setId(key.longValue());
        }

        return usuario;
    }
}