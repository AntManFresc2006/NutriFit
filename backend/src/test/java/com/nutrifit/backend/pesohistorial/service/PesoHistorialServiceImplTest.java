package com.nutrifit.backend.pesohistorial.service;

import com.nutrifit.backend.pesohistorial.dto.PesoHistorialResponse;
import com.nutrifit.backend.pesohistorial.repository.PesoHistorialRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios de PesoHistorialServiceImpl.
 * Todos los colaboradores (repositorio) son mocks,
 * por lo que no se necesita base de datos ni contexto de Spring.
 */
@ExtendWith(MockitoExtension.class)
class PesoHistorialServiceImplTest {

    @Mock
    private PesoHistorialRepository repository;

    @InjectMocks
    private PesoHistorialServiceImpl service;

    // ---------------------------------------------------------------------------
    // Fixtures reutilizables
    // ---------------------------------------------------------------------------

    private PesoHistorialResponse pesoResponse(Long id, String fecha, double pesoKg) {
        return new PesoHistorialResponse(id, fecha, pesoKg);
    }

    // ---------------------------------------------------------------------------
    // upsert
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("upsert")
    class Upsert {

        @Test
        @DisplayName("inserta nuevo registro de peso correctamente")
        void nuevoRegistro_inserta() {
            Long usuarioId = 1L;
            LocalDate fecha = LocalDate.of(2026, 5, 17);
            double pesoKg = 75.5;
            PesoHistorialResponse esperado = pesoResponse(1L, "2026-05-17", 75.5);

            when(repository.upsert(usuarioId, fecha, pesoKg)).thenReturn(esperado);

            PesoHistorialResponse resultado = service.upsert(usuarioId, fecha, pesoKg);

            assertThat(resultado).isEqualTo(esperado);
            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getPesoKg()).isEqualTo(75.5);
            assertThat(resultado.getFecha()).isEqualTo("2026-05-17");
            verify(repository).upsert(usuarioId, fecha, pesoKg);
        }

        @Test
        @DisplayName("actualiza registro existente de peso")
        void registroExistente_actualiza() {
            Long usuarioId = 1L;
            LocalDate fecha = LocalDate.of(2026, 5, 17);
            double pesoKg = 74.0;
            PesoHistorialResponse esperado = pesoResponse(5L, "2026-05-17", 74.0);

            when(repository.upsert(usuarioId, fecha, pesoKg)).thenReturn(esperado);

            PesoHistorialResponse resultado = service.upsert(usuarioId, fecha, pesoKg);

            assertThat(resultado).isEqualTo(esperado);
            assertThat(resultado.getPesoKg()).isEqualTo(74.0);
            verify(repository).upsert(usuarioId, fecha, pesoKg);
        }

        @Test
        @DisplayName("delega la operación upsert al repositorio")
        void delegaAlRepositorio() {
            Long usuarioId = 2L;
            LocalDate fecha = LocalDate.now();
            double pesoKg = 80.0;

            when(repository.upsert(usuarioId, fecha, pesoKg))
                    .thenReturn(new PesoHistorialResponse(10L, fecha.toString(), pesoKg));

            service.upsert(usuarioId, fecha, pesoKg);

            verify(repository).upsert(usuarioId, fecha, pesoKg);
        }
    }

    // ---------------------------------------------------------------------------
    // findByUsuario
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("findByUsuario")
    class FindByUsuario {

        @Test
        @DisplayName("obtiene historial de peso con límite especificado")
        void conRegistros_devuelveListaLimitada() {
            Long usuarioId = 1L;
            int limit = 5;

            List<PesoHistorialResponse> esperado = List.of(
                    pesoResponse(1L, "2026-05-17", 75.5),
                    pesoResponse(2L, "2026-05-16", 75.2),
                    pesoResponse(3L, "2026-05-15", 75.0)
            );

            when(repository.findByUsuario(usuarioId, limit)).thenReturn(esperado);

            List<PesoHistorialResponse> resultado = service.findByUsuario(usuarioId, limit);

            assertThat(resultado).hasSize(3);
            assertThat(resultado).isEqualTo(esperado);
            assertThat(resultado.get(0).getPesoKg()).isEqualTo(75.5);
            assertThat(resultado.get(2).getPesoKg()).isEqualTo(75.0);
            verify(repository).findByUsuario(usuarioId, limit);
        }

        @Test
        @DisplayName("devuelve lista vacía cuando no hay historial")
        void sinRegistros_devuelveListaVacia() {
            Long usuarioId = 1L;
            int limit = 10;

            when(repository.findByUsuario(usuarioId, limit)).thenReturn(List.of());

            List<PesoHistorialResponse> resultado = service.findByUsuario(usuarioId, limit);

            assertThat(resultado).isEmpty();
            verify(repository).findByUsuario(usuarioId, limit);
        }

        @Test
        @DisplayName("respeta el límite de registros retornados")
        void limiteRespetado() {
            Long usuarioId = 1L;
            int limit = 3;

            List<PesoHistorialResponse> esperado = List.of(
                    pesoResponse(1L, "2026-05-17", 75.5),
                    pesoResponse(2L, "2026-05-16", 75.2),
                    pesoResponse(3L, "2026-05-15", 75.0)
            );

            when(repository.findByUsuario(usuarioId, limit)).thenReturn(esperado);

            List<PesoHistorialResponse> resultado = service.findByUsuario(usuarioId, limit);

            assertThat(resultado).hasSize(3);
            verify(repository).findByUsuario(usuarioId, limit);
        }
    }

    // ---------------------------------------------------------------------------
    // deleteByUsuarioAndFecha
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("deleteByUsuarioAndFecha")
    class DeleteByUsuarioAndFecha {

        @Test
        @DisplayName("elimina registro de peso por fecha")
        void registroExistente_elimina() {
            Long usuarioId = 1L;
            LocalDate fecha = LocalDate.of(2026, 5, 17);

            service.deleteByUsuarioAndFecha(usuarioId, fecha);

            verify(repository).deleteByUsuarioAndFecha(usuarioId, fecha);
        }

        @Test
        @DisplayName("delega la eliminación al repositorio")
        void delegaAlRepositorio() {
            Long usuarioId = 2L;
            LocalDate fecha = LocalDate.of(2026, 5, 10);

            service.deleteByUsuarioAndFecha(usuarioId, fecha);

            verify(repository).deleteByUsuarioAndFecha(usuarioId, fecha);
        }
    }
}
