package com.nutrifit.client.controller;

import com.nutrifit.client.model.PesoHistorialDto;
import com.nutrifit.client.model.PesoHistorialRequestDto;
import com.nutrifit.client.service.PesoHistorialApiClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("PesoHistorialController")
class PesoHistorialControllerTest {

    @Test
    @DisplayName("construye correctamente con inyección de dependencias")
    void construye_correctamente() {
        PesoHistorialController controller = new PesoHistorialController();
        assertThat(controller).isNotNull();
    }

    @Test
    @DisplayName("ApiClient obtiene historial de peso correctamente")
    void api_client_obtiene_historial() throws Exception {
        PesoHistorialApiClient mockClient = mock(PesoHistorialApiClient.class);
        List<PesoHistorialDto> expected = Arrays.asList(
                new PesoHistorialDto(1L, "2026-05-30", 70.5),
                new PesoHistorialDto(2L, "2026-05-29", 70.8)
        );

        when(mockClient.obtenerHistorial(anyLong(), anyInt()))
                .thenReturn(expected);

        List<PesoHistorialDto> result = mockClient.obtenerHistorial(1L, 30);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getFecha()).isEqualTo("2026-05-30");
        assertThat(result.get(0).getPesoKg()).isEqualTo(70.5);
    }

    @Test
    @DisplayName("ApiClient registra peso correctamente")
    void api_client_registra_peso() throws Exception {
        PesoHistorialApiClient mockClient = mock(PesoHistorialApiClient.class);
        PesoHistorialDto expected = new PesoHistorialDto(3L, "2026-05-31", 70.2);

        when(mockClient.registrarPeso(anyLong(), any(PesoHistorialRequestDto.class)))
                .thenReturn(expected);

        PesoHistorialRequestDto request = new PesoHistorialRequestDto("2026-05-31", 70.2);
        PesoHistorialDto result = mockClient.registrarPeso(1L, request);

        assertThat(result.getFecha()).isEqualTo("2026-05-31");
        assertThat(result.getPesoKg()).isEqualTo(70.2);
    }
}
