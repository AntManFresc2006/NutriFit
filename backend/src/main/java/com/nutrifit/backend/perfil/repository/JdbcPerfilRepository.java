package com.nutrifit.backend.perfil.repository;

import com.nutrifit.backend.perfil.model.NivelActividad;
import com.nutrifit.backend.perfil.model.Perfil;
import com.nutrifit.backend.perfil.model.Sexo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC del repositorio de perfiles de usuario.
 */
@Repository
public class JdbcPerfilRepository implements PerfilRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcPerfilRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Perfil> findById(Long id) {
        String sql = """
                SELECT id, nombre, email, sexo, fecha_nacimiento,
                       altura_cm, peso_kg_actual, peso_objetivo, nivel_actividad
                FROM usuarios
                WHERE id = ?
                """;

        List<Perfil> resultados = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Perfil perfil = new Perfil();
            perfil.setId(rs.getLong("id"));
            perfil.setNombre(rs.getString("nombre"));
            perfil.setEmail(rs.getString("email"));
            perfil.setSexo(Sexo.valueOf(rs.getString("sexo")));
            perfil.setFechaNacimiento(rs.getDate("fecha_nacimiento").toLocalDate());
            perfil.setAlturaCm(rs.getInt("altura_cm"));
            perfil.setPesoKgActual(rs.getDouble("peso_kg_actual"));
            perfil.setPesoObjetivo(rs.getObject("peso_objetivo", Double.class));
            perfil.setNivelActividad(NivelActividad.valueOf(rs.getString("nivel_actividad")));
            return perfil;
        }, id);

        return resultados.stream().findFirst();
    }

    @Override
    public Perfil update(Long id, Perfil perfil) {
        String sql = """
                UPDATE usuarios
                SET sexo = ?, fecha_nacimiento = ?, altura_cm = ?,
                    peso_kg_actual = ?, peso_objetivo = ?, nivel_actividad = ?
                WHERE id = ?
                """;

        jdbcTemplate.update(sql,
                perfil.getSexo().name(),
                perfil.getFechaNacimiento(),
                perfil.getAlturaCm(),
                perfil.getPesoKgActual(),
                perfil.getPesoObjetivo(),
                perfil.getNivelActividad().name(),
                id);

        return perfil;
    }
}
