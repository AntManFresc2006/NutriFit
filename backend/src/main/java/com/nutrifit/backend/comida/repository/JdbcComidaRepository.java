package com.nutrifit.backend.comida.repository;

import com.nutrifit.backend.comida.model.Comida;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import com.nutrifit.backend.comida.model.ComidaAlimento;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC del repositorio de comidas.
 */
@Repository
public class JdbcComidaRepository implements ComidaRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcComidaRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Comida> findByUsuarioAndFecha(Long usuarioId, LocalDate fecha) {
        String sql = """
                SELECT id, usuario_id, fecha, tipo
                FROM comidas
                WHERE usuario_id = ? AND fecha = ?
                ORDER BY id ASC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Comida comida = new Comida();
            comida.setId(rs.getLong("id"));
            comida.setUsuarioId(rs.getLong("usuario_id"));
            comida.setFecha(rs.getDate("fecha").toLocalDate());
            comida.setTipo(rs.getString("tipo"));
            return comida;
        }, usuarioId, fecha);
    }

    @Override
public void addAlimentoToComida(Long comidaId, Long alimentoId, double gramos) {
    String sql = """
            INSERT INTO comida_alimentos (comida_id, alimento_id, gramos)
            VALUES (?, ?, ?)
            """;

    jdbcTemplate.update(sql, comidaId, alimentoId, gramos);
}

@Override
public List<ComidaAlimento> findItemsByComidaId(Long comidaId) {
    String sql = """
            SELECT id, comida_id, alimento_id, gramos
            FROM comida_alimentos
            WHERE comida_id = ?
            ORDER BY id ASC
            """;

    return jdbcTemplate.query(sql, (rs, rowNum) -> {
        ComidaAlimento item = new ComidaAlimento();
        item.setId(rs.getLong("id"));
        item.setComidaId(rs.getLong("comida_id"));
        item.setAlimentoId(rs.getLong("alimento_id"));
        item.setGramos(rs.getDouble("gramos"));
        return item;
    }, comidaId);
}

    @Override
    public Optional<Comida> findById(Long id) {
        String sql = """
                SELECT id, usuario_id, fecha, tipo
                FROM comidas
                WHERE id = ?
                """;

        List<Comida> resultados = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Comida comida = new Comida();
            comida.setId(rs.getLong("id"));
            comida.setUsuarioId(rs.getLong("usuario_id"));
            comida.setFecha(rs.getDate("fecha").toLocalDate());
            comida.setTipo(rs.getString("tipo"));
            return comida;
        }, id);

        return resultados.stream().findFirst();
    }

    @Override
    public Comida save(Comida comida) {
        String sql = """
                INSERT INTO comidas (usuario_id, fecha, tipo)
                VALUES (?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, comida.getUsuarioId());
            ps.setObject(2, comida.getFecha());
            ps.setString(3, comida.getTipo());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            comida.setId(key.longValue());
        }

        return comida;
    }
}