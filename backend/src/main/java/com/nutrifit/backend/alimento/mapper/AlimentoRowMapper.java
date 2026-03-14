package com.nutrifit.backend.alimento.mapper;

import com.nutrifit.backend.alimento.model.Alimento;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AlimentoRowMapper implements RowMapper<Alimento> {

    @Override
    public Alimento mapRow(ResultSet rs, int rowNum) throws SQLException {
        Alimento alimento = new Alimento();
        alimento.setId(rs.getLong("id"));
        alimento.setNombre(rs.getString("nombre"));
        alimento.setPorcionG(rs.getBigDecimal("porcion_g"));
        alimento.setKcalPor100g(rs.getBigDecimal("kcal_por_100g"));
        alimento.setProteinasG(rs.getBigDecimal("proteinas_g"));
        alimento.setGrasasG(rs.getBigDecimal("grasas_g"));
        alimento.setCarbosG(rs.getBigDecimal("carbos_g"));
        alimento.setFuente(rs.getString("fuente"));
        return alimento;
    }
}