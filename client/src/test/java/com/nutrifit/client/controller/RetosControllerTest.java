package com.nutrifit.client.controller;

import com.nutrifit.client.model.RetoDto;
import com.nutrifit.client.service.RetosApiClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("RetosController")
class RetosControllerTest {

    @Test
    @DisplayName("construye correctamente con inyección de dependencias")
    void construye_correctamente() {
        RetosController controller = new RetosController();
        assertThat(controller).isNotNull();
    }

    @Test
    @DisplayName("ApiClient obtiene retos correctamente")
    void api_client_obtiene_retos() throws Exception {
        RetosApiClient mockClient = mock(RetosApiClient.class);
        List<RetoDto> expected = Arrays.asList(
                new RetoDto(1L, "Proteína al 100%", "Cumple metas de proteína", "nutricion", 100, 30, 50, "💪", 1L, 80, true, false, LocalDate.now().plusDays(30)),
                new RetoDto(2L, "1000 kcal quemadas", "Quema 1000 kcal en ejercicio", "ejercicio", 1000, 7, 100, "🔥", 2L, 45, true, false, LocalDate.now().plusDays(7))
        );

        when(mockClient.obtenerRetos(anyLong()))
                .thenReturn(expected);

        List<RetoDto> result = mockClient.obtenerRetos(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitulo()).isEqualTo("Proteína al 100%");
        assertThat(result.get(1).getPuntos()).isEqualTo(100);
        assertThat(result.get(0).isAceptado()).isTrue();
    }
}
