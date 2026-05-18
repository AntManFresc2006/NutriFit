package com.nutrifit.client.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AlimentoFx")
class AlimentoFxTest {

    @Test
    @DisplayName("setId y getId funcionan correctamente")
    void set_get_id() {
        AlimentoFx alimento = new AlimentoFx();
        alimento.setId(42L);
        assertThat(alimento.getId()).isEqualTo(42L);
    }

    @Test
    @DisplayName("setNombre y getNombre funcionan correctamente")
    void set_get_nombre() {
        AlimentoFx alimento = new AlimentoFx();
        alimento.setNombre("Pollo a la plancha");
        assertThat(alimento.getNombre()).isEqualTo("Pollo a la plancha");
    }

    @Test
    @DisplayName("setKcalPor100g y getKcalPor100g funcionan correctamente")
    void set_get_kcal_por_100g() {
        AlimentoFx alimento = new AlimentoFx();
        alimento.setKcalPor100g(165.0);
        assertThat(alimento.getKcalPor100g()).isEqualTo(165.0);
    }

    @Test
    @DisplayName("macros se almacenan correctamente")
    void set_get_macros() {
        AlimentoFx alimento = new AlimentoFx();
        alimento.setProteinasG(31.0);
        alimento.setGrasasG(3.6);
        alimento.setCarbosG(0.0);

        assertThat(alimento.getProteinasG()).isEqualTo(31.0);
        assertThat(alimento.getGrasasG()).isEqualTo(3.6);
        assertThat(alimento.getCarbosG()).isZero();
    }

    @Test
    @DisplayName("property observable notifica al listener cuando cambia el valor")
    void property_notifica_cambio() {
        AlimentoFx alimento = new AlimentoFx();
        AtomicInteger notificaciones = new AtomicInteger(0);

        alimento.nombreProperty().addListener((obs, oldVal, newVal) -> notificaciones.incrementAndGet());

        alimento.setNombre("Arroz");
        alimento.setNombre("Pasta");

        assertThat(notificaciones.get()).isEqualTo(2);
    }
}
