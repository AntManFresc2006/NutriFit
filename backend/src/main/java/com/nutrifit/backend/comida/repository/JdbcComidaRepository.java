package com.nutrifit.backend.comida.repository;

import com.nutrifit.backend.comida.model.Comida;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import com.nutrifit.backend.comida.model.ComidaAlimento;
import com.nutrifit.backend.comida.dto.ComidaItemDetalleResponse;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos JDBC para comidas y sus alimentos asociados.
 *
 * <p>La relación entre comidas y alimentos es N:M a través de {@code comida_alimentos},
 * que además almacena los gramos consumidos. Los macros no se guardan precalculados:
 * se calculan en SQL cada vez que se consultan los ítems de una comida.</p>
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

    /**
     * Devuelve los ítems de una comida con los macros estimados calculados en SQL.
     *
     * <p>Se usa INNER JOIN porque si un alimento fue borrado del catálogo el ítem
     * quedaría sin datos nutricionales; con LEFT JOIN mostraría ceros engañosos.
     * En la práctica los alimentos no se eliminan si tienen ítems vinculados (FK).</p>
     *
     * <p>Fórmula de estimación: {@code (valor_por_100g * gramos) / 100}.
     * Los valores en la tabla están expresados por 100 g de alimento.</p>
     */
    @Override
    public List<ComidaItemDetalleResponse> findDetalleItemsByComidaId(Long comidaId) {
        String sql = """
                SELECT
                    ca.id AS item_id,
                    ca.comida_id,
                    ca.alimento_id,
                    a.nombre,
                    ca.gramos,
                    ROUND((a.kcal_por_100g * ca.gramos) / 100, 2) AS kcal_estimadas,
                    ROUND((a.proteinas_g * ca.gramos) / 100, 2) AS proteinas_estimadas,
                    ROUND((a.grasas_g * ca.gramos) / 100, 2) AS grasas_estimadas,
                    ROUND((a.carbos_g * ca.gramos) / 100, 2) AS carbos_estimados
                FROM comida_alimentos ca
                INNER JOIN alimentos a ON a.id = ca.alimento_id
                WHERE ca.comida_id = ?
                ORDER BY ca.id ASC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new ComidaItemDetalleResponse(
                        rs.getLong("item_id"),
                        rs.getLong("comida_id"),
                        rs.getLong("alimento_id"),
                        rs.getString("nombre"),
                        rs.getDouble("gramos"),
                        rs.getDouble("kcal_estimadas"),
                        rs.getDouble("proteinas_estimadas"),
                        rs.getDouble("grasas_estimadas"),
                        rs.getDouble("carbos_estimados")
                ), comidaId);
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

        // GeneratedKeyHolder es necesario para recuperar el id autoincremental
        // que MariaDB asigna tras el INSERT
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, comida.getUsuarioId());
            ps.setObject(2, comida.getFecha());  // setObject maneja LocalDate sin conversión manual
            ps.setString(3, comida.getTipo());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            comida.setId(key.longValue());
        }

        return comida;
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM comidas WHERE id = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }

    @Override
    public Optional<ComidaAlimento> findItemById(Long itemId) {
        String sql = """
                SELECT id, comida_id, alimento_id, gramos
                FROM comida_alimentos
                WHERE id = ?
                """;

        List<ComidaAlimento> resultados = jdbcTemplate.query(sql, (rs, rowNum) -> {
            ComidaAlimento item = new ComidaAlimento();
            item.setId(rs.getLong("id"));
            item.setComidaId(rs.getLong("comida_id"));
            item.setAlimentoId(rs.getLong("alimento_id"));
            item.setGramos(rs.getDouble("gramos"));
            return item;
        }, itemId);

        return resultados.stream().findFirst();
    }

    @Override
    public boolean deleteItemById(Long itemId) {
        String sql = "DELETE FROM comida_alimentos WHERE id = ?";
        return jdbcTemplate.update(sql, itemId) > 0;
    }
}