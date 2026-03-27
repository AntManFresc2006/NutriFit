package com.nutrifit.backend.ejercicio.repository;

import com.nutrifit.backend.ejercicio.model.Ejercicio;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcEjercicioRepository implements EjercicioRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcEjercicioRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Ejercicio> findAll() {
        String sql = """
                SELECT id, nombre, met, categoria
                FROM ejercicios
                ORDER BY nombre ASC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRow(rs));
    }

    @Override
    public List<Ejercicio> searchByNombre(String query) {
        String sql = """
                SELECT id, nombre, met, categoria
                FROM ejercicios
                WHERE LOWER(nombre) LIKE LOWER(?)
                ORDER BY nombre ASC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRow(rs), "%" + query + "%");
    }

    @Override
    public Optional<Ejercicio> findById(Long id) {
        String sql = """
                SELECT id, nombre, met, categoria
                FROM ejercicios
                WHERE id = ?
                """;
        List<Ejercicio> resultados = jdbcTemplate.query(sql, (rs, rowNum) -> mapRow(rs), id);
        return resultados.stream().findFirst();
    }

    @Override
    public Ejercicio save(Ejercicio ejercicio) {
        String sql = """
                INSERT INTO ejercicios (nombre, met, categoria)
                VALUES (?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, ejercicio.getNombre());
            ps.setDouble(2, ejercicio.getMet());
            ps.setString(3, ejercicio.getCategoria());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            ejercicio.setId(key.longValue());
        }
        return ejercicio;
    }

    private Ejercicio mapRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new Ejercicio(
                rs.getLong("id"),
                rs.getString("nombre"),
                rs.getDouble("met"),
                rs.getString("categoria")
        );
    }
}
