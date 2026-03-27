package com.nutrifit.backend.resumen.repository;

import com.nutrifit.backend.resumen.dto.ResumenDiarioResponse;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;

import javax.sql.DataSource;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Implementación alternativa del resumen diario que delega el cálculo al
 * stored procedure {@code sp_resumen_diario} (definido en V8__sp_resumen_diario.sql).
 *
 * <p>Esta clase <strong>no está activa</strong>: la anotación {@code @Repository}
 * se omite intencionadamente para que Spring no la registre como bean y no entre
 * en conflicto con {@link JdbcResumenDiarioRepository}. Para activarla basta con
 * añadir {@code @Repository} y eliminar dicha anotación de la implementación JDBC.</p>
 */
public class SpResumenDiarioRepository implements ResumenDiarioRepository {

    private final SimpleJdbcCall jdbcCall;

    public SpResumenDiarioRepository(DataSource dataSource) {
        this.jdbcCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_resumen_diario")
                .returningResultSet("resumen", rowMapper());
    }

    @Override
    public ResumenDiarioResponse obtenerResumenDiario(Long usuarioId, LocalDate fecha) {
        Map<String, Object> params = Map.of(
                "p_usuario_id", usuarioId,
                "p_fecha", Date.valueOf(fecha)
        );

        Map<String, Object> result = jdbcCall.execute(params);

        @SuppressWarnings("unchecked")
        List<ResumenDiarioResponse> rows =
                (List<ResumenDiarioResponse>) result.get("resumen");

        if (rows != null && !rows.isEmpty()) {
            return rows.get(0);
        }

        return new ResumenDiarioResponse(usuarioId, fecha, 0, 0, 0, 0, 0);
    }

    private static RowMapper<ResumenDiarioResponse> rowMapper() {
        return (rs, rowNum) -> new ResumenDiarioResponse(
                rs.getLong("usuario_id"),
                rs.getDate("fecha").toLocalDate(),
                rs.getDouble("kcal_totales"),
                rs.getDouble("proteinas_totales"),
                rs.getDouble("grasas_totales"),
                rs.getDouble("carbos_totales"),
                rs.getDouble("kcal_quemadas_totales")
        );
    }
}
