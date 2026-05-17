package com.nutrifit.backend.listacompra.repository;

import com.nutrifit.backend.listacompra.model.ListaCompraItem;
import com.nutrifit.backend.listacompra.dto.ListaCompraItemRequest;
import com.nutrifit.backend.listacompra.dto.ListaCompraItemResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;

@Repository
public class JdbcListaCompraRepository implements ListaCompraRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcListaCompraRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<ListaCompraItem> findByUsuario(Long usuarioId) {
        String sql = """
                SELECT id, usuario_id, nombre, cantidad, categoria, completado, created_at
                FROM lista_compra
                WHERE usuario_id = ?
                ORDER BY completado ASC, categoria ASC, created_at DESC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            ListaCompraItem item = new ListaCompraItem();
            item.setId(rs.getLong("id"));
            item.setUsuarioId(rs.getLong("usuario_id"));
            item.setNombre(rs.getString("nombre"));
            item.setCantidad(rs.getString("cantidad"));
            item.setCategoria(rs.getString("categoria"));
            item.setCompletado(rs.getBoolean("completado"));
            item.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            return item;
        }, usuarioId);
    }

    @Override
    public ListaCompraItemResponse save(Long usuarioId, ListaCompraItemRequest req) {
        String categoria = req.getCategoria() != null ? req.getCategoria() : "OTROS";

        String sql = """
                INSERT INTO lista_compra (usuario_id, nombre, cantidad, categoria, completado, created_at)
                VALUES (?, ?, ?, ?, FALSE, CURRENT_TIMESTAMP)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, usuarioId);
            ps.setString(2, req.getNombre());
            ps.setString(3, req.getCantidad());
            ps.setString(4, categoria);
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            String selectSql = """
                    SELECT id, nombre, cantidad, categoria, completado, created_at
                    FROM lista_compra
                    WHERE id = ?
                    """;

            List<ListaCompraItemResponse> results = jdbcTemplate.query(selectSql, (rs, rowNum) ->
                    new ListaCompraItemResponse(
                            rs.getLong("id"),
                            rs.getString("nombre"),
                            rs.getString("cantidad"),
                            rs.getString("categoria"),
                            rs.getBoolean("completado"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    ), key.longValue());

            return results.isEmpty() ? null : results.get(0);
        }

        return null;
    }

    @Override
    public ListaCompraItemResponse toggleCompletado(Long id, Long usuarioId) {
        // Verificar que el item pertenece al usuario
        String checkSql = "SELECT usuario_id FROM lista_compra WHERE id = ?";
        List<Long> owners = jdbcTemplate.query(checkSql, (rs, rowNum) -> rs.getLong("usuario_id"), id);

        if (owners.isEmpty() || !owners.get(0).equals(usuarioId)) {
            return null;
        }

        String toggleSql = """
                UPDATE lista_compra
                SET completado = NOT completado
                WHERE id = ?
                """;

        jdbcTemplate.update(toggleSql, id);

        String selectSql = """
                SELECT id, nombre, cantidad, categoria, completado, created_at
                FROM lista_compra
                WHERE id = ?
                """;

        List<ListaCompraItemResponse> results = jdbcTemplate.query(selectSql, (rs, rowNum) ->
                new ListaCompraItemResponse(
                        rs.getLong("id"),
                        rs.getString("nombre"),
                        rs.getString("cantidad"),
                        rs.getString("categoria"),
                        rs.getBoolean("completado"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                ), id);

        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public boolean deleteById(Long id, Long usuarioId) {
        String checkSql = "SELECT usuario_id FROM lista_compra WHERE id = ?";
        List<Long> owners = jdbcTemplate.query(checkSql, (rs, rowNum) -> rs.getLong("usuario_id"), id);

        if (owners.isEmpty() || !owners.get(0).equals(usuarioId)) {
            return false;
        }

        String deleteSql = "DELETE FROM lista_compra WHERE id = ?";
        return jdbcTemplate.update(deleteSql, id) > 0;
    }

    @Override
    public void deleteCompletados(Long usuarioId) {
        String sql = """
                DELETE FROM lista_compra
                WHERE usuario_id = ? AND completado = TRUE
                """;
        jdbcTemplate.update(sql, usuarioId);
    }

    @Override
    public List<String> findAlimentosMasUsados(Long usuarioId, int limit) {
        String sql = """
                SELECT a.nombre
                FROM comida_alimentos ca
                JOIN alimentos a ON a.id = ca.alimento_id
                JOIN comidas c ON c.id = ca.comida_id
                WHERE c.usuario_id = ?
                GROUP BY a.nombre
                ORDER BY COUNT(*) DESC
                LIMIT ?
                """;

        return jdbcTemplate.queryForList(sql, String.class, usuarioId, limit);
    }
}
