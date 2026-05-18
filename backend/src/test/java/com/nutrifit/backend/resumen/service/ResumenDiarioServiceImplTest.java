package com.nutrifit.backend.resumen.service;

import com.nutrifit.backend.perfil.dto.PerfilResponse;
import com.nutrifit.backend.perfil.service.PerfilService;
import com.nutrifit.backend.resumen.dto.ResumenDiarioResponse;
import com.nutrifit.backend.resumen.repository.ResumenDiarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ResumenDiarioServiceImplTest {

    @Mock
    private ResumenDiarioRepository resumenDiarioRepository;

    @Mock
    private PerfilService perfilService;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private ResumenDiarioServiceImpl service;

    private static final Long USUARIO_ID = 1L;
    private static final LocalDate FECHA = LocalDate.of(2026, 3, 26);

    private PerfilResponse perfilConTdee(double tdee, double pesoActual, Double pesoObjetivo) {
        PerfilResponse p = new PerfilResponse();
        p.setTdee(tdee);
        p.setPesoKgActual(pesoActual);
        p.setPesoObjetivo(pesoObjetivo);
        return p;
    }

    // ---------------------------------------------------------------------------
    // Delegación al repositorio
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("obtenerResumenDiario — delegación al repositorio")
    class DelegacionRepositorio {

        @Test
        @DisplayName("delega al repositorio con los argumentos correctos y devuelve el resultado")
        void delegaAlRepositorioYDevuelveElResultado() {
            ResumenDiarioResponse esperado = new ResumenDiarioResponse(
                    USUARIO_ID, FECHA, 650.0, 45.0, 20.0, 80.0, 0.0);
            when(resumenDiarioRepository.obtenerResumenDiario(USUARIO_ID, FECHA)).thenReturn(esperado);
            when(perfilService.getPerfil(USUARIO_ID)).thenThrow(new RuntimeException("sin perfil"));

            ResumenDiarioResponse resultado = service.obtenerResumenDiario(USUARIO_ID, FECHA);

            assertThat(resultado).isSameAs(esperado);
            verify(resumenDiarioRepository).obtenerResumenDiario(USUARIO_ID, FECHA);
        }

        @Test
        @DisplayName("día sin comidas: propaga el resumen con todos los valores a cero")
        void diaSinComidas_propagaResumenConValoresCero() {
            ResumenDiarioResponse sinComidas = new ResumenDiarioResponse(USUARIO_ID, FECHA, 0, 0, 0, 0, 0);
            when(resumenDiarioRepository.obtenerResumenDiario(USUARIO_ID, FECHA)).thenReturn(sinComidas);
            when(perfilService.getPerfil(USUARIO_ID)).thenThrow(new RuntimeException("sin perfil"));

            ResumenDiarioResponse resultado = service.obtenerResumenDiario(USUARIO_ID, FECHA);

            assertThat(resultado.getKcalTotales()).isZero();
            assertThat(resultado.getProteinasTotales()).isZero();
            assertThat(resultado.getGrasasTotales()).isZero();
            assertThat(resultado.getCarbosTotales()).isZero();
        }

        @Test
        @DisplayName("día con solo ejercicio y sin comidas: refleja el gasto calórico correctamente")
        void diaSoloEjercicio_reflejaKcalQuemadas() {
            ResumenDiarioResponse soloEjercicio = new ResumenDiarioResponse(USUARIO_ID, FECHA, 0, 0, 0, 0, 350.0);
            when(resumenDiarioRepository.obtenerResumenDiario(USUARIO_ID, FECHA)).thenReturn(soloEjercicio);
            when(perfilService.getPerfil(USUARIO_ID)).thenThrow(new RuntimeException("sin perfil"));

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
            when(perfilService.getPerfil(USUARIO_ID)).thenThrow(new RuntimeException("sin perfil"));

            ResumenDiarioResponse resultado = service.obtenerResumenDiario(USUARIO_ID, FECHA);

            assertThat(resultado.getKcalTotales()).isEqualTo(523.75);
            assertThat(resultado.getProteinasTotales()).isEqualTo(38.25);
            assertThat(resultado.getGrasasTotales()).isEqualTo(17.50);
            assertThat(resultado.getCarbosTotales()).isEqualTo(61.30);
        }
    }

    // ---------------------------------------------------------------------------
    // Enriquecimiento con TDEE y estado de balance
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("enriquecerConTdee — estado de balance")
    class EstadoBalance {

        @Test
        @DisplayName("kcal muy por encima del TDEE → estado SUPERAVIT y balanceReal correcto")
        void kcalAlta_estadoSuperavit() {
            // kcal 2500, tdee 2000, quemadas 0 → balance = 500 > 100 → SUPERAVIT
            ResumenDiarioResponse resumen = new ResumenDiarioResponse(USUARIO_ID, FECHA, 2500.0, 60, 80, 300, 0);
            when(resumenDiarioRepository.obtenerResumenDiario(USUARIO_ID, FECHA)).thenReturn(resumen);
            when(perfilService.getPerfil(USUARIO_ID)).thenReturn(perfilConTdee(2000.0, 80.0, null));
            when(jdbcTemplate.queryForObject(anyString(), eq(Double.class), any(), any(), any(), any()))
                    .thenReturn(null);

            ResumenDiarioResponse resultado = service.obtenerResumenDiario(USUARIO_ID, FECHA);

            assertThat(resultado.getTdee()).isEqualTo(2000.0);
            assertThat(resultado.getBalanceReal()).isEqualTo(500.0);
            assertThat(resultado.getEstadoBalance()).isEqualTo("SUPERAVIT");
        }

        @Test
        @DisplayName("kcal muy por debajo del TDEE → estado DEFICIT y balanceReal negativo")
        void kcalBaja_estadoDeficit() {
            // kcal 1500, tdee 2000, quemadas 0 → balance = -500 < -100 → DEFICIT
            ResumenDiarioResponse resumen = new ResumenDiarioResponse(USUARIO_ID, FECHA, 1500.0, 45, 50, 180, 0);
            when(resumenDiarioRepository.obtenerResumenDiario(USUARIO_ID, FECHA)).thenReturn(resumen);
            when(perfilService.getPerfil(USUARIO_ID)).thenReturn(perfilConTdee(2000.0, 80.0, null));
            when(jdbcTemplate.queryForObject(anyString(), eq(Double.class), any(), any(), any(), any()))
                    .thenReturn(null);

            ResumenDiarioResponse resultado = service.obtenerResumenDiario(USUARIO_ID, FECHA);

            assertThat(resultado.getBalanceReal()).isEqualTo(-500.0);
            assertThat(resultado.getEstadoBalance()).isEqualTo("DEFICIT");
        }

        @Test
        @DisplayName("kcal dentro del margen del TDEE (±100 kcal) → estado MANTENIMIENTO")
        void kcalCercaTdee_estadoMantenimiento() {
            // kcal 2050, tdee 2000, quemadas 0 → balance = 50 → MANTENIMIENTO
            ResumenDiarioResponse resumen = new ResumenDiarioResponse(USUARIO_ID, FECHA, 2050.0, 55, 70, 260, 0);
            when(resumenDiarioRepository.obtenerResumenDiario(USUARIO_ID, FECHA)).thenReturn(resumen);
            when(perfilService.getPerfil(USUARIO_ID)).thenReturn(perfilConTdee(2000.0, 80.0, null));
            when(jdbcTemplate.queryForObject(anyString(), eq(Double.class), any(), any(), any(), any()))
                    .thenReturn(null);

            ResumenDiarioResponse resultado = service.obtenerResumenDiario(USUARIO_ID, FECHA);

            assertThat(resultado.getBalanceReal()).isEqualTo(50.0);
            assertThat(resultado.getEstadoBalance()).isEqualTo("MANTENIMIENTO");
        }

        @Test
        @DisplayName("kcal quemadas restan del balance real")
        void kcalQuemadas_restanDelBalanceReal() {
            // kcal 2500, tdee 2000, quemadas 600 → balance = 2500 - 2000 - 600 = -100 → MANTENIMIENTO
            ResumenDiarioResponse resumen = new ResumenDiarioResponse(USUARIO_ID, FECHA, 2500.0, 60, 80, 300, 600.0);
            when(resumenDiarioRepository.obtenerResumenDiario(USUARIO_ID, FECHA)).thenReturn(resumen);
            when(perfilService.getPerfil(USUARIO_ID)).thenReturn(perfilConTdee(2000.0, 80.0, null));
            when(jdbcTemplate.queryForObject(anyString(), eq(Double.class), any(), any(), any(), any()))
                    .thenReturn(null);

            ResumenDiarioResponse resultado = service.obtenerResumenDiario(USUARIO_ID, FECHA);

            assertThat(resultado.getBalanceReal()).isEqualTo(-100.0);
            assertThat(resultado.getEstadoBalance()).isEqualTo("MANTENIMIENTO");
        }

        @Test
        @DisplayName("usuario sin perfil configurado → TDEE=0 y balanceReal igual a kcal netas")
        void sinPerfil_tdeeEsCero() {
            ResumenDiarioResponse resumen = new ResumenDiarioResponse(USUARIO_ID, FECHA, 1800.0, 50, 60, 220, 0);
            when(resumenDiarioRepository.obtenerResumenDiario(USUARIO_ID, FECHA)).thenReturn(resumen);
            when(perfilService.getPerfil(USUARIO_ID)).thenThrow(new RuntimeException("perfil no encontrado"));

            ResumenDiarioResponse resultado = service.obtenerResumenDiario(USUARIO_ID, FECHA);

            assertThat(resultado.getTdee()).isZero();
            // balanceReal = 1800 - 0 - 0 = 1800
            assertThat(resultado.getBalanceReal()).isEqualTo(1800.0);
            assertThat(resultado.getEstadoBalance()).isEqualTo("SUPERAVIT");
        }
    }

    // ---------------------------------------------------------------------------
    // Cálculo de fecha objetivo
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("enriquecerConFechaObjetivo")
    class FechaObjetivo {

        @Test
        @DisplayName("sin peso objetivo no se calcula fecha objetivo")
        void sinPesoObjetivo_noCalculaFecha() {
            ResumenDiarioResponse resumen = new ResumenDiarioResponse(USUARIO_ID, FECHA, 1500.0, 45, 50, 180, 0);
            when(resumenDiarioRepository.obtenerResumenDiario(USUARIO_ID, FECHA)).thenReturn(resumen);
            // pesoObjetivo = null
            when(perfilService.getPerfil(USUARIO_ID)).thenReturn(perfilConTdee(2000.0, 80.0, null));
            when(jdbcTemplate.queryForObject(anyString(), eq(Double.class), any(), any(), any(), any()))
                    .thenReturn(null);

            ResumenDiarioResponse resultado = service.obtenerResumenDiario(USUARIO_ID, FECHA);

            assertThat(resultado.getDiasParaObjetivo()).isNull();
            assertThat(resultado.getFechaObjetivo()).isNull();
        }

        @Test
        @DisplayName("quiere perder peso con déficit suficiente → calcula días y fecha objetivo")
        void quierePerderPeso_calculaFechaObjetivo() {
            // tdee=2000, kcal=1500, balance=-500 (déficit) — quiere bajar de 85 a 75 kg
            ResumenDiarioResponse resumen = new ResumenDiarioResponse(USUARIO_ID, FECHA, 1500.0, 45, 50, 180, 0);
            when(resumenDiarioRepository.obtenerResumenDiario(USUARIO_ID, FECHA)).thenReturn(resumen);
            when(perfilService.getPerfil(USUARIO_ID)).thenReturn(perfilConTdee(2000.0, 85.0, 75.0));
            // JdbcTemplate retorna null → mediaBalance=0 → fallback a balanceReal=-500
            when(jdbcTemplate.queryForObject(anyString(), eq(Double.class), any(), any(), any(), any()))
                    .thenReturn(null);

            ResumenDiarioResponse resultado = service.obtenerResumenDiario(USUARIO_ID, FECHA);

            // días = ceil((85-75) * 7700 / 500) = ceil(154) = 154
            assertThat(resultado.getDiasParaObjetivo()).isEqualTo(154);
            assertThat(resultado.getFechaObjetivo()).isNotNull();
        }

        @Test
        @DisplayName("quiere ganar peso con superávit suficiente → calcula días y fecha objetivo")
        void quiereGanarPeso_calculaFechaObjetivo() {
            // tdee=2000, kcal=2600, balance=+600 — quiere subir de 60 a 65 kg
            ResumenDiarioResponse resumen = new ResumenDiarioResponse(USUARIO_ID, FECHA, 2600.0, 65, 85, 325, 0);
            when(resumenDiarioRepository.obtenerResumenDiario(USUARIO_ID, FECHA)).thenReturn(resumen);
            when(perfilService.getPerfil(USUARIO_ID)).thenReturn(perfilConTdee(2000.0, 60.0, 65.0));
            when(jdbcTemplate.queryForObject(anyString(), eq(Double.class), any(), any(), any(), any()))
                    .thenReturn(null);

            ResumenDiarioResponse resultado = service.obtenerResumenDiario(USUARIO_ID, FECHA);

            // días = ceil((65-60) * 7700 / 600) = ceil(64.17) = 65
            assertThat(resultado.getDiasParaObjetivo()).isEqualTo(65);
            assertThat(resultado.getFechaObjetivo()).isNotNull();
        }

        @Test
        @DisplayName("déficit insuficiente (< 50 kcal) no genera fecha objetivo")
        void deficitInsuficiente_noGeneraFecha() {
            // tdee=2000, kcal=1980, balance=-20 (< 50) — quiere perder peso pero balance es mínimo
            ResumenDiarioResponse resumen = new ResumenDiarioResponse(USUARIO_ID, FECHA, 1980.0, 50, 65, 245, 0);
            when(resumenDiarioRepository.obtenerResumenDiario(USUARIO_ID, FECHA)).thenReturn(resumen);
            when(perfilService.getPerfil(USUARIO_ID)).thenReturn(perfilConTdee(2000.0, 85.0, 75.0));
            when(jdbcTemplate.queryForObject(anyString(), eq(Double.class), any(), any(), any(), any()))
                    .thenReturn(null);

            ResumenDiarioResponse resultado = service.obtenerResumenDiario(USUARIO_ID, FECHA);

            assertThat(resultado.getDiasParaObjetivo()).isNull();
            assertThat(resultado.getFechaObjetivo()).isNull();
        }

        @Test
        @DisplayName("media de balance histórico sustituye al balance del día actual cuando no es cero")
        void mediaHistoricaNoNula_usaMediaEnLugarDeBalanceReal() {
            // tdee=2000, kcal=1500, balance_dia=-500; pero mediaBalance=-300 (histórico)
            ResumenDiarioResponse resumen = new ResumenDiarioResponse(USUARIO_ID, FECHA, 1500.0, 45, 50, 180, 0);
            when(resumenDiarioRepository.obtenerResumenDiario(USUARIO_ID, FECHA)).thenReturn(resumen);
            when(perfilService.getPerfil(USUARIO_ID)).thenReturn(perfilConTdee(2000.0, 85.0, 75.0));
            // JdbcTemplate retorna -300 → usa -300 en lugar de balanceReal=-500
            when(jdbcTemplate.queryForObject(anyString(), eq(Double.class), any(), any(), any(), any()))
                    .thenReturn(-300.0);

            ResumenDiarioResponse resultado = service.obtenerResumenDiario(USUARIO_ID, FECHA);

            // días = ceil((85-75) * 7700 / 300) = ceil(256.67) = 257
            assertThat(resultado.getDiasParaObjetivo()).isEqualTo(257);
        }
    }
}
