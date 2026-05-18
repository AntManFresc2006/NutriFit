package com.nutrifit.client.model;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PerfilDto")
class PerfilDtoTest {

    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Test
    @DisplayName("deserializa perfil completo con todos los campos")
    void deserializa_perfil_completo() throws Exception {
        String json = """
                {
                  "id": 3,
                  "nombre": "Carlos",
                  "email": "carlos@test.com",
                  "sexo": "MASCULINO",
                  "fechaNacimiento": "1995-06-15",
                  "alturaCm": 180,
                  "pesoKgActual": 80.5,
                  "pesoObjetivo": 75.0,
                  "nivelActividad": "MODERADO",
                  "tmb": 1900.0,
                  "tdee": 2400.0
                }
                """;

        PerfilDto dto = mapper.readValue(json, PerfilDto.class);

        assertThat(dto.getId()).isEqualTo(3L);
        assertThat(dto.getNombre()).isEqualTo("Carlos");
        assertThat(dto.getEmail()).isEqualTo("carlos@test.com");
        assertThat(dto.getSexo()).isEqualTo("MASCULINO");
        assertThat(dto.getAlturaCm()).isEqualTo(180);
        assertThat(dto.getPesoKgActual()).isEqualTo(80.5);
        assertThat(dto.getPesoObjetivo()).isEqualTo(75.0);
        assertThat(dto.getNivelActividad()).isEqualTo("MODERADO");
        assertThat(dto.getTmb()).isEqualTo(1900.0);
        assertThat(dto.getTdee()).isEqualTo(2400.0);
    }

    @Test
    @DisplayName("pesoObjetivo puede ser null")
    void peso_objetivo_nullable() throws Exception {
        String json = """
                {"id": 1, "nombre": "Ana", "pesoObjetivo": null}
                """;

        PerfilDto dto = mapper.readValue(json, PerfilDto.class);

        assertThat(dto.getPesoObjetivo()).isNull();
    }

    @Test
    @DisplayName("deserializa perfil sin pesoObjetivo en el JSON")
    void deserializa_sin_peso_objetivo() throws Exception {
        String json = """
                {"id": 2, "nombre": "Luis", "email": "luis@test.com"}
                """;

        PerfilDto dto = mapper.readValue(json, PerfilDto.class);

        assertThat(dto.getNombre()).isEqualTo("Luis");
        assertThat(dto.getPesoObjetivo()).isNull();
    }
}
