package com.nutrifit.backend.gamificacion.service;

import com.nutrifit.backend.gamificacion.dto.GamificacionResponse;
import com.nutrifit.backend.perfil.dto.PerfilResponse;
import com.nutrifit.backend.perfil.service.PerfilService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios de GamificacionService.
 * Todos los colaboradores (JdbcTemplate, PerfilService) son mocks.
 */
@ExtendWith(MockitoExtension.class)
class GamificacionServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private PerfilService perfilService;

    @InjectMocks
    private GamificacionService service;

    // ---------------------------------------------------------------------------
    // Fixtures reutilizables
    // ---------------------------------------------------------------------------

    private PerfilResponse perfilConPeso(double pesoKg, double tdee) {
        PerfilResponse perfil = new PerfilResponse();
        perfil.setPesoKgActual(pesoKg);
        perfil.setTdee(tdee);
        return perfil;
    }

    // ---------------------------------------------------------------------------
    // calcular() - exception handling
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("calcular")
    class Calcular {

        @Test
        @DisplayName("devuelve default cuando hay excepción en JdbcTemplate")
        void calcular_devuelveDefault_cuandoHayExcepcion() {
            Long usuarioId = 1L;
            LocalDate fecha = LocalDate.now();

            // GamificacionService llama query(sql, RowMapper, userId, fecha) — 2 varargs
            when(jdbcTemplate.query(any(String.class), any(RowMapper.class), any(), any()))
                    .thenThrow(new RuntimeException("DB error"));

            GamificacionResponse respuesta = service.calcular(usuarioId, fecha);

            assertThat(respuesta.getRacha()).isEqualTo(0);
            assertThat(respuesta.getNutriScore()).isEqualTo(0);
            assertThat(respuesta.getNutriGrade()).isEqualTo("—");
            assertThat(respuesta.isCumpleProteina()).isFalse();
            assertThat(respuesta.isCumpleBalance()).isFalse();
            assertThat(respuesta.isCumpleEjercicio()).isFalse();
            assertThat(respuesta.isCumpleVariedad()).isFalse();
        }
    }

    // ---------------------------------------------------------------------------
    // calcularRacha
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("calcularRacha")
    class CalcularRacha {

        @Test
        @DisplayName("devuelve cero cuando no hay comidas")
        void calcularRacha_devuelveCero_sinComidas() {
            Long usuarioId = 1L;
            LocalDate fecha = LocalDate.now();

            mockDefaultResponses(usuarioId, 0.0, 0.0, 0.0, 0, 0);

            GamificacionResponse respuesta = service.calcular(usuarioId, fecha);

            assertThat(respuesta.getRacha()).isEqualTo(0);
        }

        @Test
        @DisplayName("devuelve racha correcta cuando hay días consecutivos")
        void calcularRacha_devuelveRachaCorrecta_diasConsecutivos() {
            Long usuarioId = 1L;
            LocalDate hoy = LocalDate.of(2026, 5, 17);
            LocalDate ayer = hoy.minusDays(1);
            LocalDate anteayer = hoy.minusDays(2);

            List<LocalDate> fechas = new ArrayList<>();
            fechas.add(hoy);
            fechas.add(ayer);
            fechas.add(anteayer);

            // Configurar mocks base primero, luego sobreescribir el de racha
            mockDefaultResponses(usuarioId, 0.0, 0.0, 0.0, 0, 0);

            // El último stub registrado tiene prioridad en Mockito
            when(jdbcTemplate.query(any(String.class), any(RowMapper.class), any(), any()))
                    .thenReturn(fechas);

            GamificacionResponse respuesta = service.calcular(usuarioId, hoy);

            assertThat(respuesta.getRacha()).isEqualTo(3);
        }

        @Test
        @DisplayName("devuelve racha de 1 cuando hay brecha entre días")
        void calcularRacha_devuelve1_cuandoNoHayDiaAnterior() {
            Long usuarioId = 1L;
            LocalDate hoy = LocalDate.of(2026, 5, 17);
            LocalDate dosAntesDeHoy = hoy.minusDays(2);

            List<LocalDate> fechas = new ArrayList<>();
            fechas.add(hoy);
            fechas.add(dosAntesDeHoy);

            mockDefaultResponses(usuarioId, 0.0, 0.0, 0.0, 0, 0);

            when(jdbcTemplate.query(any(String.class), any(RowMapper.class), any(), any()))
                    .thenReturn(fechas);

            GamificacionResponse respuesta = service.calcular(usuarioId, hoy);

            assertThat(respuesta.getRacha()).isEqualTo(1);
        }
    }

    // ---------------------------------------------------------------------------
    // verificarProteina
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("verificarProteina")
    class VerificarProteina {

        @Test
        @DisplayName("es true cuando proteína alcanza target")
        void verificarProteina_esTrue_cuandoProteinaAlcanzaTarget() {
            Long usuarioId = 1L;
            LocalDate fecha = LocalDate.now();
            // peso = 80kg, target = 80 * 0.8 = 64g
            // proteínaReal = 70g >= 64g -> true

            mockDefaultResponses(usuarioId, 70.0, 0.0, 0.0, 0, 0);

            GamificacionResponse respuesta = service.calcular(usuarioId, fecha);

            assertThat(respuesta.isCumpleProteina()).isTrue();
        }

        @Test
        @DisplayName("es false cuando proteína está baja")
        void verificarProteina_esFalse_cuandoProteinaBaja() {
            Long usuarioId = 1L;
            LocalDate fecha = LocalDate.now();
            // peso = 80kg, target = 80 * 0.8 = 64g
            // proteínaReal = 30g < 64g -> false

            mockDefaultResponses(usuarioId, 30.0, 0.0, 0.0, 0, 0);

            GamificacionResponse respuesta = service.calcular(usuarioId, fecha);

            assertThat(respuesta.isCumpleProteina()).isFalse();
        }
    }

    // ---------------------------------------------------------------------------
    // verificarBalance
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("verificarBalance")
    class VerificarBalance {

        @Test
        @DisplayName("es true cuando balance está dentro de ±300 kcal")
        void verificarBalance_esTrue_cuandoBalanceMenor300() {
            Long usuarioId = 1L;
            LocalDate fecha = LocalDate.now();
            // balance = 2000 - 2000 - 200 = -200
            // |−200| = 200 ≤ 300 ✓

            mockDefaultResponses(usuarioId, 0.0, 2000.0, 200.0, 0, 0);

            GamificacionResponse respuesta = service.calcular(usuarioId, fecha);

            assertThat(respuesta.isCumpleBalance()).isTrue();
        }

        @Test
        @DisplayName("es false cuando balance es mayor que ±300 kcal")
        void verificarBalance_esFalse_cuandoBalanceMayor300() {
            Long usuarioId = 1L;
            LocalDate fecha = LocalDate.now();
            // balance = 3000 - 2000 - 0 = 1000
            // |1000| = 1000 > 300 ✗

            mockDefaultResponses(usuarioId, 0.0, 3000.0, 0.0, 0, 0);

            GamificacionResponse respuesta = service.calcular(usuarioId, fecha);

            assertThat(respuesta.isCumpleBalance()).isFalse();
        }
    }

    // ---------------------------------------------------------------------------
    // verificarEjercicio
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("verificarEjercicio")
    class VerificarEjercicio {

        @Test
        @DisplayName("es true cuando hay registro de ejercicio")
        void verificarEjercicio_esTrue_cuandoHayRegistro() {
            Long usuarioId = 1L;
            LocalDate fecha = LocalDate.now();

            mockDefaultResponses(usuarioId, 0.0, 0.0, 0.0, 1, 0);

            GamificacionResponse respuesta = service.calcular(usuarioId, fecha);

            assertThat(respuesta.isCumpleEjercicio()).isTrue();
        }

        @Test
        @DisplayName("es false cuando no hay registro de ejercicio")
        void verificarEjercicio_esFalse_cuandoNoHayRegistro() {
            Long usuarioId = 1L;
            LocalDate fecha = LocalDate.now();

            mockDefaultResponses(usuarioId, 0.0, 0.0, 0.0, 0, 0);

            GamificacionResponse respuesta = service.calcular(usuarioId, fecha);

            assertThat(respuesta.isCumpleEjercicio()).isFalse();
        }
    }

    // ---------------------------------------------------------------------------
    // verificarVariedad
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("verificarVariedad")
    class VerificarVariedad {

        @Test
        @DisplayName("es true cuando hay 4 o más alimentos")
        void verificarVariedad_esTrue_cuandoHay4Alimentos() {
            Long usuarioId = 1L;
            LocalDate fecha = LocalDate.now();

            mockDefaultResponses(usuarioId, 0.0, 0.0, 0.0, 0, 4);

            GamificacionResponse respuesta = service.calcular(usuarioId, fecha);

            assertThat(respuesta.isCumpleVariedad()).isTrue();
        }

        @Test
        @DisplayName("es false cuando hay menos de 4 alimentos")
        void verificarVariedad_esFalse_cuandoHayMenos4() {
            Long usuarioId = 1L;
            LocalDate fecha = LocalDate.now();

            mockDefaultResponses(usuarioId, 0.0, 0.0, 0.0, 0, 3);

            GamificacionResponse respuesta = service.calcular(usuarioId, fecha);

            assertThat(respuesta.isCumpleVariedad()).isFalse();
        }
    }

    // ---------------------------------------------------------------------------
    // calcularNutriScore
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("calcularNutriScore")
    class CalcularNutriScore {

        @Test
        @DisplayName("devuelve 100 y grade A cuando todo cumple")
        void calcularNutriScore_devuelve100_cuandoTodoCumple() {
            Long usuarioId = 1L;
            LocalDate fecha = LocalDate.now();
            // proteína=100 (cumple 100 >= 64), balance = 2000-2000-50 = -50 (|−50| ≤ 300)
            // exercise=1, variety=4 → Score: 25+25+25+25=100, grade=A

            mockDefaultResponses(usuarioId, 100.0, 2000.0, 50.0, 1, 4);

            GamificacionResponse respuesta = service.calcular(usuarioId, fecha);

            assertThat(respuesta.getNutriScore()).isEqualTo(100);
            assertThat(respuesta.getNutriGrade()).isEqualTo("A");
        }

        @Test
        @DisplayName("devuelve 0 y grade F cuando nada cumple")
        void calcularNutriScore_devuelveCero_cuandoNadaCumple() {
            Long usuarioId = 1L;
            LocalDate fecha = LocalDate.now();
            // proteína=10 (no cumple, 10 < 64), balance = 5000-2000-0 = 3000 (|3000| > 300)
            // exercise=0, variety=1 → Score: 0, grade=F

            mockDefaultResponses(usuarioId, 10.0, 5000.0, 0.0, 0, 1);

            GamificacionResponse respuesta = service.calcular(usuarioId, fecha);

            assertThat(respuesta.getNutriScore()).isEqualTo(0);
            assertThat(respuesta.getNutriGrade()).isEqualTo("F");
        }
    }

    // ---------------------------------------------------------------------------
    // Helper methods para mocking
    // ---------------------------------------------------------------------------

    /**
     * Configura mocks para todos los colaboradores de GamificacionService.
     * Nota: los métodos de JdbcTemplate aceptan (sql, Class/RowMapper, userId, fecha)
     * donde userId y fecha son 2 elementos varargs — se necesita any(), any().
     */
    private void mockDefaultResponses(Long usuarioId, double proteina, double kcalConsumidas,
                                       double kcalQuemadas, int exerciseCount, int varietyCount) {
        // Racha query: query(String, RowMapper<T>, Object... args) con 2 varargs
        lenient().when(jdbcTemplate.query(any(String.class), any(RowMapper.class), any(), any()))
                .thenReturn(new ArrayList<LocalDate>());

        // Todos los queryForObject: distinguidos por contenido SQL
        // - proteinas_g → consulta de proteínas (Double)
        // - kcal_por_100g → consulta de kcal consumidas (Double)
        // - kcal_quemadas → consulta de kcal quemadas (Double); también contiene "ejercicios_registro"
        //   pero se evalúa primero por estar antes en el if-else
        // - COUNT(*) → consulta de ejercicios (Integer)
        // - DISTINCT → consulta de variedad de alimentos (Integer)
        lenient().when(jdbcTemplate.queryForObject(any(String.class), any(Class.class), any(), any()))
                .thenAnswer(inv -> {
                    String sql = inv.getArgument(0);

                    if (sql.contains("proteinas_g")) {
                        return proteina;
                    } else if (sql.contains("kcal_por_100g")) {
                        return kcalConsumidas;
                    } else if (sql.contains("kcal_quemadas")) {
                        return kcalQuemadas;
                    } else if (sql.contains("COUNT(*)")) {
                        return exerciseCount;
                    } else if (sql.contains("DISTINCT")) {
                        return varietyCount;
                    }

                    // Fallback: 0.0 para Double, 0 para Integer
                    Class<?> resultType = inv.getArgument(1);
                    return resultType == Double.class ? 0.0 : 0;
                });

        // Perfil: devuelve peso=80kg, TDEE=2000 para todos los tests
        lenient().when(perfilService.getPerfil(usuarioId))
                .thenReturn(perfilConPeso(80.0, 2000.0));
    }
}
