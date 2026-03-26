package com.nutrifit.backend.alimento.service;

import com.nutrifit.backend.alimento.dto.AlimentoRequest;
import com.nutrifit.backend.alimento.dto.AlimentoResponse;
import com.nutrifit.backend.alimento.model.Alimento;
import com.nutrifit.backend.alimento.repository.AlimentoRepository;
import com.nutrifit.backend.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios de AlimentoServiceImpl.
 * El repositorio se sustituye por un mock de Mockito, por lo que estos tests
 * no necesitan base de datos ni contexto de Spring.
 */
@ExtendWith(MockitoExtension.class)
class AlimentoServiceImplTest {

    @Mock
    private AlimentoRepository alimentoRepository;

    @InjectMocks
    private AlimentoServiceImpl service;

    // ---------------------------------------------------------------------------
    // Fixtures reutilizables
    // ---------------------------------------------------------------------------

    private Alimento alimentoMock() {
        return new Alimento(
                1L,
                "Pollo a la plancha",
                new BigDecimal("100"),
                new BigDecimal("165"),
                new BigDecimal("31.0"),
                new BigDecimal("3.6"),
                new BigDecimal("0.0"),
                "USDA"
        );
    }

    private AlimentoRequest requestMock() {
        AlimentoRequest req = new AlimentoRequest();
        req.setNombre("  Pollo a la plancha  "); // con espacios para probar trim
        req.setPorcionG(new BigDecimal("100"));
        req.setKcalPor100g(new BigDecimal("165"));
        req.setProteinasG(new BigDecimal("31.0"));
        req.setGrasasG(new BigDecimal("3.6"));
        req.setCarbosG(new BigDecimal("0.0"));
        req.setFuente("USDA");
        return req;
    }

    // ---------------------------------------------------------------------------
    // findAll
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("sin query devuelve todos los alimentos")
        void sinQuery_devuelveTodos() {
            when(alimentoRepository.findAll()).thenReturn(List.of(alimentoMock()));

            List<AlimentoResponse> resultado = service.findAll(null);

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getNombre()).isEqualTo("Pollo a la plancha");
            verify(alimentoRepository).findAll();
            verifyNoMoreInteractions(alimentoRepository);
        }

        @Test
        @DisplayName("query en blanco trata igual que sin query")
        void queryEnBlanco_devuelveTodos() {
            when(alimentoRepository.findAll()).thenReturn(List.of());

            service.findAll("   ");

            verify(alimentoRepository).findAll();
            verify(alimentoRepository, never()).searchByNombre(anyString());
        }

        @Test
        @DisplayName("query con texto delega en searchByNombre con texto recortado")
        void conQuery_delegaEnSearchByNombre() {
            when(alimentoRepository.searchByNombre("pollo")).thenReturn(List.of(alimentoMock()));

            List<AlimentoResponse> resultado = service.findAll("  pollo  ");

            assertThat(resultado).hasSize(1);
            verify(alimentoRepository).searchByNombre("pollo");
            verify(alimentoRepository, never()).findAll();
        }

        @Test
        @DisplayName("repositorio vacío devuelve lista vacía sin errores")
        void repositorioVacio_devuelveListaVacia() {
            when(alimentoRepository.findAll()).thenReturn(List.of());

            List<AlimentoResponse> resultado = service.findAll(null);

            assertThat(resultado).isEmpty();
        }
    }

    // ---------------------------------------------------------------------------
    // findById
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("id existente devuelve el DTO con los datos correctos")
        void idExistente_devuelveDTO() {
            when(alimentoRepository.findById(1L)).thenReturn(Optional.of(alimentoMock()));

            AlimentoResponse resultado = service.findById(1L);

            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getNombre()).isEqualTo("Pollo a la plancha");
            assertThat(resultado.getKcalPor100g()).isEqualByComparingTo("165");
        }

        @Test
        @DisplayName("id inexistente lanza ResourceNotFoundException con mensaje descriptivo")
        void idInexistente_lanzaExcepcion() {
            when(alimentoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("No existe un alimento con id 99");
        }
    }

    // ---------------------------------------------------------------------------
    // save
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("guarda el alimento y devuelve el DTO con el id asignado")
        void guardaYDevuelveDTO() {
            Alimento guardado = alimentoMock(); // tiene id=1 y nombre sin espacios
            when(alimentoRepository.save(any(Alimento.class))).thenReturn(guardado);

            AlimentoResponse resultado = service.save(requestMock());

            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getNombre()).isEqualTo("Pollo a la plancha");
            verify(alimentoRepository).save(any(Alimento.class));
        }

        @Test
        @DisplayName("el nombre del request se recorta antes de persistir")
        void nombreConEspacios_seRecorta() {
            Alimento guardado = alimentoMock();
            when(alimentoRepository.save(argThat(a -> "Pollo a la plancha".equals(a.getNombre()))))
                    .thenReturn(guardado);

            service.save(requestMock()); // el request tiene "  Pollo a la plancha  "

            verify(alimentoRepository).save(argThat(a -> "Pollo a la plancha".equals(a.getNombre())));
        }
    }

    // ---------------------------------------------------------------------------
    // update
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("id existente actualiza y devuelve el DTO con los nuevos datos")
        void idExistente_actualizaYDevuelveDTO() {
            Alimento actualizado = new Alimento(1L, "Pollo a la plancha",
                    new BigDecimal("100"), new BigDecimal("165"),
                    new BigDecimal("31.0"), new BigDecimal("3.6"),
                    new BigDecimal("0.0"), "USDA");

            when(alimentoRepository.findById(1L)).thenReturn(Optional.of(alimentoMock()));
            when(alimentoRepository.update(eq(1L), any(Alimento.class))).thenReturn(actualizado);

            AlimentoResponse resultado = service.update(1L, requestMock());

            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getNombre()).isEqualTo("Pollo a la plancha");
            verify(alimentoRepository).update(eq(1L), any(Alimento.class));
        }

        @Test
        @DisplayName("id inexistente lanza ResourceNotFoundException sin llamar a update")
        void idInexistente_lanzaExcepcionSinActualizar() {
            when(alimentoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.update(99L, requestMock()))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("No existe un alimento con id 99");

            verify(alimentoRepository, never()).update(anyLong(), any());
        }
    }

    // ---------------------------------------------------------------------------
    // deleteById
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("deleteById")
    class DeleteById {

        @Test
        @DisplayName("id existente elimina y devuelve true")
        void idExistente_eliminaYDevuelveTrue() {
            when(alimentoRepository.findById(1L)).thenReturn(Optional.of(alimentoMock()));
            when(alimentoRepository.deleteById(1L)).thenReturn(true);

            boolean resultado = service.deleteById(1L);

            assertThat(resultado).isTrue();
            verify(alimentoRepository).deleteById(1L);
        }

        @Test
        @DisplayName("id inexistente lanza ResourceNotFoundException sin llamar a deleteById")
        void idInexistente_lanzaExcepcionSinEliminar() {
            when(alimentoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deleteById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("No existe un alimento con id 99");

            verify(alimentoRepository, never()).deleteById(anyLong());
        }
    }
}
