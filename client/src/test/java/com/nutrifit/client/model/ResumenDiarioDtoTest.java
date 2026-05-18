package com.nutrifit.client.model;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

@DisplayName("ResumenDiarioDto")
class ResumenDiarioDtoTest {

    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Test
    @DisplayName("deserializa JSON completo del backend correctamente")
    void deserializa_json_completo() throws Exception {
        String json = """
                {
                  "usuarioId": 5,
                  "fecha": "2026-05-18",
                  "kcalTotales": 1800.5,
                  "proteinasTotales": 120.0,
                  "grasasTotales": 60.0,
                  "carbosTotales": 200.0,
                  "kcalQuemadasTotales": 300.0,
                  "balanceNeto": 1500.5,
                  "tdee": 2100.0,
                  "balanceReal": -300.0,
                  "estadoBalance": "DEFICIT"
                }
                """;

        ResumenDiarioDto dto = mapper.readValue(json, ResumenDiarioDto.class);

        assertThat(dto.getUsuarioId()).isEqualTo(5L);
        assertThat(dto.getFecha()).isEqualTo("2026-05-18");
        assertThat(dto.getKcalTotales()).isEqualTo(1800.5);
        assertThat(dto.getProteinasTotales()).isEqualTo(120.0);
        assertThat(dto.getTdee()).isEqualTo(2100.0);
        assertThat(dto.getBalanceReal()).isEqualTo(-300.0);
        assertThat(dto.getEstadoBalance()).isEqualTo("DEFICIT");
    }

    @Test
    @DisplayName("campos numéricos son cero cuando el JSON está vacío")
    void campos_numericos_por_defecto_cero() throws Exception {
        ResumenDiarioDto dto = mapper.readValue("{}", ResumenDiarioDto.class);

        assertThat(dto.getKcalTotales()).isZero();
        assertThat(dto.getTdee()).isZero();
        assertThat(dto.getBalanceReal()).isZero();
        assertThat(dto.getUsuarioId()).isNull();
    }

    @Test
    @DisplayName("ignora campos desconocidos del JSON sin lanzar excepción")
    void ignora_campos_desconocidos() {
        String json = """
                {"kcalTotales": 2000.0, "campoNuevoDelBackend": "ignorar"}
                """;
        assertThatNoException().isThrownBy(() -> mapper.readValue(json, ResumenDiarioDto.class));
    }
}
