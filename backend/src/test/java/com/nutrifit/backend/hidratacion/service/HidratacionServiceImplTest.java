package com.nutrifit.backend.hidratacion.service;

import com.nutrifit.backend.hidratacion.dto.AguaRequest;
import com.nutrifit.backend.hidratacion.dto.AguaResponse;
import com.nutrifit.backend.hidratacion.dto.HidratacionDiariaResponse;
import com.nutrifit.backend.hidratacion.repository.JdbcAguaRepository;
import com.nutrifit.backend.common.exception.UnauthorizedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios de HidratacionServiceImpl.
 * Todos los colaboradores (repositorio) son mocks,
 * por lo que no se necesita base de datos ni contexto de Spring.
 */
@ExtendWith(MockitoExtension.class)
class HidratacionServiceImplTest {

    @Mock
    private JdbcAguaRepository repository;

    @InjectMocks
    private HidratacionServiceImpl service;

    // ---------------------------------------------------------------------------
    // Fixtures reutilizables
    // ---------------------------------------------------------------------------

    private AguaRequest aguaRequest(LocalDate fecha, Integer cantidadMl) {
        return new AguaRequest(fecha, cantidadMl);
    }

    private AguaResponse aguaResponse(Long id, LocalDate fecha, int cantidadMl, LocalTime hora) {
        return new AguaResponse(id, fecha, cantidadMl, hora);
    }

    // ---------------------------------------------------------------------------
    // registrar
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("registrar")
    class Registrar {

        @Test
        @DisplayName("registra hidratación correctamente y devuelve AguaResponse")
        void registroExitoso_devuelveAguaResponse() {
            Long usuarioId = 1L;
            LocalDate fecha = LocalDate.of(2026, 5, 17);
            AguaRequest request = aguaRequest(fecha, 500);
            AguaResponse esperado = aguaResponse(1L, fecha, 500, LocalTime.of(10, 30));

            when(repository.save(usuarioId, request)).thenReturn(esperado);

            AguaResponse resultado = service.registrar(usuarioId, request);

            assertThat(resultado).isEqualTo(esperado);
            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getCantidadMl()).isEqualTo(500);
            assertThat(resultado.getFecha()).isEqualTo(fecha);
            verify(repository).save(usuarioId, request);
        }

        @Test
        @DisplayName("delega el guardado al repositorio")
        void delegaAlRepositorio() {
            Long usuarioId = 2L;
            AguaRequest request = aguaRequest(LocalDate.now(), 250);

            when(repository.save(usuarioId, request))
                    .thenReturn(new AguaResponse(10L, LocalDate.now(), 250, LocalTime.now()));

            service.registrar(usuarioId, request);

            verify(repository).save(usuarioId, request);
        }
    }

    // ---------------------------------------------------------------------------
    // getDiario
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("getDiario")
    class GetDiario {

        @Test
        @DisplayName("lista vacía cuando no hay registros del día")
        void sinRegistros_devuelveListaVacia() {
            Long usuarioId = 1L;
            LocalDate fecha = LocalDate.of(2026, 5, 17);

            when(repository.findByUsuarioAndFecha(usuarioId, fecha))
                    .thenReturn(List.of());

            HidratacionDiariaResponse resultado = service.getDiario(usuarioId, fecha);

            assertThat(resultado.getFecha()).isEqualTo(fecha);
            assertThat(resultado.getTotalMl()).isEqualTo(0);
            assertThat(resultado.getObjetivoMl()).isEqualTo(2000);
            assertThat(resultado.getPorcentaje()).isEqualTo(0);
            assertThat(resultado.getRegistros()).isEmpty();
        }

        @Test
        @DisplayName("suma total de ml y calcula porcentaje correctamente")
        void conRegistros_sumaTotalYCalculaPorcentaje() {
            Long usuarioId = 1L;
            LocalDate fecha = LocalDate.of(2026, 5, 17);

            List<AguaResponse> registros = List.of(
                    aguaResponse(1L, fecha, 500, LocalTime.of(9, 0)),
                    aguaResponse(2L, fecha, 500, LocalTime.of(12, 0)),
                    aguaResponse(3L, fecha, 500, LocalTime.of(15, 0))
            );

            when(repository.findByUsuarioAndFecha(usuarioId, fecha))
                    .thenReturn(registros);

            HidratacionDiariaResponse resultado = service.getDiario(usuarioId, fecha);

            assertThat(resultado.getFecha()).isEqualTo(fecha);
            assertThat(resultado.getTotalMl()).isEqualTo(1500);
            assertThat(resultado.getObjetivoMl()).isEqualTo(2000);
            assertThat(resultado.getPorcentaje()).isEqualTo(75);
            assertThat(resultado.getRegistros()).hasSize(3);
        }

        @Test
        @DisplayName("limita porcentaje al 100% cuando se alcanza o supera el objetivo")
        void porcentajeSuperaObjetivo_limitaA100() {
            Long usuarioId = 1L;
            LocalDate fecha = LocalDate.of(2026, 5, 17);

            List<AguaResponse> registros = List.of(
                    aguaResponse(1L, fecha, 1500, LocalTime.of(9, 0)),
                    aguaResponse(2L, fecha, 800, LocalTime.of(12, 0))
            );

            when(repository.findByUsuarioAndFecha(usuarioId, fecha))
                    .thenReturn(registros);

            HidratacionDiariaResponse resultado = service.getDiario(usuarioId, fecha);

            assertThat(resultado.getTotalMl()).isEqualTo(2300);
            assertThat(resultado.getPorcentaje()).isEqualTo(100);
        }
    }

    // ---------------------------------------------------------------------------
    // eliminar
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("eliminar")
    class Eliminar {

        @Test
        @DisplayName("elimina registro cuando el usuario es el propietario")
        void usuarioPropietario_eliminaRegistro() {
            Long usuarioId = 1L;
            Long registroId = 5L;

            when(repository.findUsuarioIdByRegistroId(registroId)).thenReturn(usuarioId);

            service.eliminar(usuarioId, registroId);

            verify(repository).findUsuarioIdByRegistroId(registroId);
            verify(repository).deleteById(registroId);
        }

        @Test
        @DisplayName("lanza UnauthorizedException cuando el usuario no es el propietario")
        void usuarioNoPropietario_lanzaExcepcion() {
            Long usuarioId = 1L;
            Long registroId = 5L;
            Long duenioReal = 2L;

            when(repository.findUsuarioIdByRegistroId(registroId)).thenReturn(duenioReal);

            assertThatThrownBy(() -> service.eliminar(usuarioId, registroId))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("Acceso denegado");

            verify(repository).findUsuarioIdByRegistroId(registroId);
            verify(repository, never()).deleteById(anyLong());
        }

        @Test
        @DisplayName("lanza UnauthorizedException cuando el registro no existe (dueño null)")
        void registroInexistente_lanzaExcepcion() {
            Long usuarioId = 1L;
            Long registroId = 999L;

            when(repository.findUsuarioIdByRegistroId(registroId)).thenReturn(null);

            assertThatThrownBy(() -> service.eliminar(usuarioId, registroId))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("Acceso denegado");

            verify(repository).findUsuarioIdByRegistroId(registroId);
            verify(repository, never()).deleteById(anyLong());
        }
    }
}
