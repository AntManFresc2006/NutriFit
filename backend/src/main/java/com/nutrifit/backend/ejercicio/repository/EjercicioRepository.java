package com.nutrifit.backend.ejercicio.repository;

import com.nutrifit.backend.ejercicio.model.Ejercicio;

import java.util.List;
import java.util.Optional;

public interface EjercicioRepository {

    List<Ejercicio> findAll();

    List<Ejercicio> findByTipo(String tipo);

    List<Ejercicio> searchByNombre(String query);

    List<Ejercicio> searchByNombreAndTipo(String query, String tipo);

    Optional<Ejercicio> findById(Long id);

    Ejercicio save(Ejercicio ejercicio);
}
