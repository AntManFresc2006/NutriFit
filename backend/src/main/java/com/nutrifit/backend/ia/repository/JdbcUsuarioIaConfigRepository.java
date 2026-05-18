package com.nutrifit.backend.ia.repository;

import com.nutrifit.backend.ia.model.UsuarioIaConfig;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC del repositorio de configuración de IA.
 * Maneja la tabla 'usuario_ia_config'.
 */
@Repository
public class JdbcUsuarioIaConfigRepository implements UsuarioIaConfigRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcUsuarioIaConfigRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<UsuarioIaConfig> findByUsuarioId(Long usuarioId) {
        String sql = """
                SELECT usuario_id, proxy_url, model, api_key
                FROM usuario_ia_config
                WHERE usuario_id = ?
                """;

        RowMapper<UsuarioIaConfig> rowMapper = (rs, rowNum) -> {
            UsuarioIaConfig config = new UsuarioIaConfig();
            config.setUsuarioId(rs.getLong("usuario_id"));
            config.setProxyUrl(rs.getString("proxy_url"));
            config.setModel(rs.getString("model"));
            config.setApiKey(rs.getString("api_key"));
            return config;
        };

        List<UsuarioIaConfig> resultados = jdbcTemplate.query(sql, rowMapper, usuarioId);
        return resultados.stream().findFirst();
    }

    @Override
    public void save(Long usuarioId, UsuarioIaConfig config) {
        String sql = """
                INSERT INTO usuario_ia_config (usuario_id, proxy_url, model, api_key, created_at, updated_at)
                VALUES (?, ?, ?, ?, NOW(), NOW())
                ON CONFLICT (usuario_id) DO UPDATE
                SET proxy_url = ?, model = ?, api_key = ?, updated_at = NOW()
                """;

        jdbcTemplate.update(sql,
                usuarioId, config.getProxyUrl(), config.getModel(), config.getApiKey(),
                config.getProxyUrl(), config.getModel(), config.getApiKey()
        );
    }

    @Override
    public void deleteByUsuarioId(Long usuarioId) {
        String sql = "DELETE FROM usuario_ia_config WHERE usuario_id = ?";
        jdbcTemplate.update(sql, usuarioId);
    }
}
