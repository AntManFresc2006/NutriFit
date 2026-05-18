package com.nutrifit.backend.ejercicio.service;

import com.nutrifit.backend.common.exception.ResourceNotFoundException;
import com.nutrifit.backend.ejercicio.dto.EjercicioRequest;
import com.nutrifit.backend.ejercicio.dto.EjercicioResponse;
import com.nutrifit.backend.ejercicio.model.Ejercicio;
import com.nutrifit.backend.ejercicio.repository.EjercicioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EjercicioServiceImplTest {

    @Mock
    private EjercicioRepository ejercicioRepository;

    @InjectMocks
    private EjercicioServiceImpl service;

    private Ejercicio correrMock() {
        return new Ejercicio(1L, "Correr", 8.0, "Cardio", "CARDIO");
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("sin filtros devuelve todos los ejercicios")
        void sinFiltros_devuelveTodos() {
            when(ejercicioRepository.findAll()).thenReturn(List.of(correrMock()));

            List<EjercicioResponse> resultado = service.findAll(null, null);

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getNombre()).isEqualTo("Correr");
            verify(ejercicioRepository).findAll();
            verifyNoMoreInteractions(ejercicioRepository);
        }

        @Test
        @DisplayName("solo query delega en searchByNombre con texto recortado")
        void soloQuery_delegaEnSearchByNombre() {
            when(ejercicioRepository.searchByNombre("correr")).thenReturn(List.of(correrMock()));

            List<EjercicioResponse> resultado = service.findAll("  correr  ", null);

            assertThat(resultado).hasSize(1);
            verify(ejercicioRepository).searchByNombre("correr");
            verify(ejercicioRepository, never()).findAll();
        }

        @Test
        @DisplayName("solo tipo delega en findByTipo")
        void soloTipo_delegaEnFindByTipo() {
            when(ejercicioRepository.findByTipo("CARDIO")).thenReturn(List.of(correrMock()));

            List<EjercicioResponse> resultado = service.findAll(null, "CARDIO");

            assertThat(resultado).hasSize(1);
            verify(ejercicioRepository).findByTipo("CARDIO");
            verify(ejercicioRepository, never()).findAll();
        }

        @Test
        @DisplayName("query y tipo combinados delegan en searchByNombreAndTipo")
        void queryYTipo_delegaEnSearchByNombreAndTipo() {
            when(ejercicioRepository.searchByNombreAndTipo("correr", "CARDIO")).thenReturn(List.of(correrMock()));

            List<EjercicioResponse> resultado = service.findAll("  correr  ", "CARDIO");

            assertThat(resultado).hasSize(1);
            verify(ejercicioRepository).searchByNombreAndTipo("correr", "CARDIO");
            verify(ejercicioRepository, never()).findAll();
        }

        @Test
        @DisplayName("query y tipo en blanco tratan como sin filtros")
        void queryYTipoEnBlanco_tratanComoSinFiltros() {
            when(ejercicioRepository.findAll()).thenReturn(List.of());

            service.findAll("   ", "   ");

            verify(ejercicioRepository).findAll();
            verify(ejercicioRepository, never()).searchByNombre(anyString());
            verify(ejercicioRepository, never()).findByTipo(anyString());
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("id existente devuelve el DTO con los datos correctos")
        void idExistente_devuelveDTO() {
            when(ejercicioRepository.findById(1L)).thenReturn(Optional.of(correrMock()));

            EjercicioResponse resultado = service.findById(1L);

            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getNombre()).isEqualTo("Correr");
            assertThat(resultado.getMet()).isEqualTo(8.0);
            assertThat(resultado.getCategoria()).isEqualTo("Cardio");
            assertThat(resultado.getTipo()).isEqualTo("CARDIO");
        }

        @Test
        @DisplayName("id inexistente lanza ResourceNotFoundException con mensaje descriptivo")
        void idInexistente_lanzaExcepcion() {
            when(ejercicioRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("No existe un ejercicio con id 99");
        }
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("guarda el ejercicio y devuelve el DTO con el id asignado")
        void guardaYDevuelveDTO() {
            EjercicioRequest request = new EjercicioRequest();
            request.setNombre("  Natación  ");
            request.setMet(6.0);
            request.setCategoria("Acuático");

            Ejercicio guardado = new Ejercicio(2L, "Natación", 6.0, "Acuático", null);
            when(ejercicioRepository.save(argThat(e -> "Natación".equals(e.getNombre()))))
                    .thenReturn(guardado);

            EjercicioResponse resultado = service.save(request);

            assertThat(resultado.getId()).isEqualTo(2L);
            assertThat(resultado.getNombre()).isEqualTo("Natación");
            assertThat(resultado.getMet()).isEqualTo(6.0);
        }

        @Test
        @DisplayName("el nombre del request se recorta antes de persistir")
        void nombreConEspacios_seRecorta() {
            EjercicioRequest request = new EjercicioRequest();
            request.setNombre("  Yoga  ");
            request.setMet(2.5);
            request.setCategoria("Flexibilidad");

            Ejercicio guardado = new Ejercicio(3L, "Yoga", 2.5, "Flexibilidad", null);
            when(ejercicioRepository.save(argThat(e -> "Yoga".equals(e.getNombre()))))
                    .thenReturn(guardado);

            service.save(request);

            verify(ejercicioRepository).save(argThat(e -> "Yoga".equals(e.getNombre())));
        }
    }
}
