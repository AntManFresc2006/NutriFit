package com.nutrifit.backend.alimento.repository;

import com.nutrifit.backend.alimento.mapper.AlimentoRowMapper;
import com.nutrifit.backend.alimento.model.Alimento;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcAlimentoRepository implements AlimentoRepository {

    private final JdbcTemplate jdbcTemplate;
    private final AlimentoRowMapper rowMapper = new AlimentoRowMapper();

    public JdbcAlimentoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Alimento> findAll() {
        String sql = """
                SELECT id, nombre, porcion_g, kcal_por_100g, proteinas_g, grasas_g, carbos_g, fuente
                FROM alimentos
                ORDER BY nombre ASC
                """;
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public List<Alimento> searchByNombre(String query) {
        String sql = """
                SELECT id, nombre, porcion_g, kcal_por_100g, proteinas_g, grasas_g, carbos_g, fuente
                FROM alimentos
                WHERE LOWER(nombre) LIKE LOWER(?)
                ORDER BY nombre ASC
                """;
        return jdbcTemplate.query(sql, rowMapper, "%" + query + "%");
    }

    @Override
    public Optional<Alimento> findById(Long id) {
        String sql = """
                SELECT id, nombre, porcion_g, kcal_por_100g, proteinas_g, grasas_g, carbos_g, fuente
                FROM alimentos
                WHERE id = ?
                """;

        List<Alimento> resultados = jdbcTemplate.query(sql, rowMapper, id);
        return resultados.stream().findFirst();
    }

    @Override
    public Alimento save(Alimento alimento) {
        String sql = """
                INSERT INTO alimentos (nombre, porcion_g, kcal_por_100g, proteinas_g, grasas_g, carbos_g, fuente)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, alimento.getNombre());
            ps.setBigDecimal(2, alimento.getPorcionG());
            ps.setBigDecimal(3, alimento.getKcalPor100g());
            ps.setBigDecimal(4, alimento.getProteinasG());
            ps.setBigDecimal(5, alimento.getGrasasG());
            ps.setBigDecimal(6, alimento.getCarbosG());
            ps.setString(7, alimento.getFuente());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            alimento.setId(key.longValue());
        }

        return alimento;
    }

    @Override
    public Alimento update(Long id, Alimento alimento) {
        String sql = """
                UPDATE alimentos
                SET nombre = ?, porcion_g = ?, kcal_por_100g = ?, proteinas_g = ?, grasas_g = ?, carbos_g = ?, fuente = ?
                WHERE id = ?
                """;

        jdbcTemplate.update(
                sql,
                alimento.getNombre(),
                alimento.getPorcionG(),
                alimento.getKcalPor100g(),
                alimento.getProteinasG(),
                alimento.getGrasasG(),
                alimento.getCarbosG(),
                alimento.getFuente(),
                id);

        alimento.setId(id);
        return alimento;
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM alimentos WHERE id = ?";
        int filasAfectadas = jdbcTemplate.update(sql, id);
        return filasAfectadas > 0;
    }
}