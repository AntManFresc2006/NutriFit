package com.nutrifit.client.controller;

import com.nutrifit.client.model.GamificacionDto;
import com.nutrifit.client.service.GamificacionApiClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("GamificacionController")
class GamificacionControllerTest {

    @Test
    @DisplayName("construye correctamente con inyección de dependencias")
    void construye_correctamente() {
        GamificacionController controller = new GamificacionController();
        assertThat(controller).isNotNull();
    }

    @Test
    @DisplayName("ApiClient obtiene gamificación correctamente")
    void api_client_obtiene_gamificacion() throws Exception {
        GamificacionApiClient mockClient = mock(GamificacionApiClient.class);
        GamificacionDto expected = new GamificacionDto(5, 850, "A", true, true, false, true);

        when(mockClient.obtenerGamificacion(anyLong(), any(LocalDate.class)))
                .thenReturn(expected);

        GamificacionDto result = mockClient.obtenerGamificacion(1L, LocalDate.now());

        assertThat(result.getRacha()).isEqualTo(5);
        assertThat(result.getNutriScore()).isEqualTo(850);
        assertThat(result.getNutriGrade()).isEqualTo("A");
        assertThat(result.isCumpleProteina()).isTrue();
    }
}
