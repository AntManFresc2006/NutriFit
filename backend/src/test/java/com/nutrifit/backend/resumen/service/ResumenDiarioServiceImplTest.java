package com.nutrifit.backend.resumen.service;

import com.nutrifit.backend.resumen.dto.ResumenDiarioResponse;
import com.nutrifit.backend.resumen.repository.ResumenDiarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios de ResumenDiarioServiceImpl.
 * El repositorio se sustituye por un mock de Mockito, por lo que estos tests
 * no necesitan base de datos ni contexto de Spring.
 */
@ExtendWith(MockitoExtension.class)
class ResumenDiarioServiceImplTest {

    @Mock
    private ResumenDiarioRepository resumenDiarioRepository;

    @InjectMocks
    private ResumenDiarioServiceImpl service;

    private static final Long USUARIO_ID = 1L;
    private static final LocalDate FECHA = LocalDate.of(2026, 3, 26);

    @Nested
    @DisplayName("obtenerResumenDiario")
    class ObtenerResumenDiario {

        @Test
        @DisplayName("delega al repositorio con los argumentos correctos y devuelve el resultado")
        void delegaAlRepositorioYDevuelveElResultado() {
            ResumenDiarioResponse esperado = new ResumenDiarioResponse(
                    USUARIO_ID, FECHA, 650.0, 45.0, 20.0, 80.0, 0.0);
            when(resumenDiarioRepository.obtenerResumenDiario(USUARIO_ID, FECHA)).thenReturn(esperado);

            ResumenDiarioResponse resultado = service.obtenerResumenDiario(USUARIO_ID, FECHA);

            assertThat(resultado).isSameAs(esperado);
            verify(resumenDiarioRepository).obtenerResumenDiario(USUARIO_ID, FECHA);
        }

        @Test
        @DisplayName("día sin comidas: propaga el resumen con todos los valores a cero")
        void diaSinComidas_propagaResumenConValoresCero() {
            ResumenDiarioResponse sinComidas = new ResumenDiarioResponse(USUARIO_ID, FECHA, 0, 0, 0, 0, 0);
            when(resumenDiarioRepository.obtenerResumenDiario(USUARIO_ID, FECHA)).thenReturn(sinComidas);

            ResumenDiarioResponse resultado = service.obtenerResumenDiario(USUARIO_ID, FECHA);

            assertThat(resultado.getKcalTotales()).isZero();
            assertThat(resultado.getProteinasTotales()).isZero();
            assertThat(resultado.getGrasasTotales()).isZero();
            assertThat(resultado.getCarbosTotales()).isZero();
        }

        @Test
        @DisplayName("día con solo ejercicio y sin comidas: refleja el gasto calórico correctamente")
        void diaSoloEjercicio_reflejaKcalQuemadas() {
            // Día en que el usuario sale a correr pero no registra comidas.
            // La query ya no depende de `comidas` como tabla base, así que
            // kcal_quemadas_totales debe ser mayor que cero aunque la ingesta sea 0.
            ResumenDiarioResponse soloEjercicio = new ResumenDiarioResponse(USUARIO_ID, FECHA, 0, 0, 0, 0, 350.0);
            when(resumenDiarioRepository.obtenerResumenDiario(USUARIO_ID, FECHA)).thenReturn(soloEjercicio);

            ResumenDiarioResponse resultado = service.obtenerResumenDiario(USUARIO_ID, FECHA);

            assertThat(resultado.getKcalTotales()).isZero();
            assertThat(resultado.getKcalQuemadasTotales()).isEqualTo(350.0);
            assertThat(resultado.getBalanceNeto()).isEqualTo(-350.0);
        }

        @Test
        @DisplayName("valores nutricionales con decimales se propagan sin modificar")
        void valoresDecimales_sePropagaSinModificar() {
            ResumenDiarioResponse conDecimales = new ResumenDiarioResponse(
                    USUARIO_ID, FECHA, 523.75, 38.25, 17.50, 61.30, 0.0);
            when(resumenDiarioRepository.obtenerResumenDiario(USUARIO_ID, FECHA)).thenReturn(conDecimales);

            ResumenDiarioResponse resultado = service.obtenerResumenDiario(USUARIO_ID, FECHA);

            assertThat(resultado.getKcalTotales()).isEqualTo(523.75);
            assertThat(resultado.getProteinasTotales()).isEqualTo(38.25);
            assertThat(resultado.getGrasasTotales()).isEqualTo(17.50);
            assertThat(resultado.getCarbosTotales()).isEqualTo(61.30);
        }
    }
}
