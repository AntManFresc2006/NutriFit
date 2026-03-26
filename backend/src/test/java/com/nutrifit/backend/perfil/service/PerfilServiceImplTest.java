package com.nutrifit.backend.perfil.service;

import com.nutrifit.backend.common.exception.ResourceNotFoundException;
import com.nutrifit.backend.perfil.dto.PerfilResponse;
import com.nutrifit.backend.perfil.dto.PerfilUpdateRequest;
import com.nutrifit.backend.perfil.model.NivelActividad;
import com.nutrifit.backend.perfil.model.Perfil;
import com.nutrifit.backend.perfil.model.Sexo;
import com.nutrifit.backend.perfil.repository.PerfilRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios de PerfilServiceImpl.
 * El repositorio se sustituye por un mock de Mockito, por lo que estos tests
 * no necesitan base de datos ni contexto de Spring.
 */
@ExtendWith(MockitoExtension.class)
class PerfilServiceImplTest {

    @Mock
    private PerfilRepository perfilRepository;

    @InjectMocks
    private PerfilServiceImpl service;

    // Edad fija de 30 años sin importar cuándo se ejecute el test.
    private static final LocalDate NACIMIENTO_30 = LocalDate.now().minusYears(30);

    @Nested
    @DisplayName("getPerfil")
    class GetPerfil {

        @Test
        @DisplayName("hombre de 30 años: TMB aplica +5 y TDEE usa el factor de SEDENTARIO")
        void hombre_calculaTmbYTdeeCorrectamente() {
            // base = 10*70 + 6.25*170 - 5*30 = 1612.5  →  H: +5  →  TMB = 1617.5
            // TDEE = 1617.5 * 1.2 = 1941.0
            Perfil perfil = new Perfil(1L, "Antonio", "a@test.com",
                    Sexo.H, NACIMIENTO_30, 170, 70.0, null, NivelActividad.SEDENTARIO);
            when(perfilRepository.findById(1L)).thenReturn(Optional.of(perfil));

            PerfilResponse resultado = service.getPerfil(1L);

            assertThat(resultado.getTmb()).isEqualTo(1617.5);
            assertThat(resultado.getTdee()).isEqualTo(1941.0);
        }

        @Test
        @DisplayName("mujer de 30 años: TMB aplica -161 y TDEE usa el factor de MODERADO")
        void mujer_calculaTmbYTdeeCorrectamente() {
            // base = 10*60 + 6.25*160 - 5*30 = 1450.0  →  M: -161  →  TMB = 1289.0
            // TDEE = 1289.0 * 1.55 = 1997.95
            Perfil perfil = new Perfil(2L, "Laura", "l@test.com",
                    Sexo.M, NACIMIENTO_30, 160, 60.0, null, NivelActividad.MODERADO);
            when(perfilRepository.findById(2L)).thenReturn(Optional.of(perfil));

            PerfilResponse resultado = service.getPerfil(2L);

            assertThat(resultado.getTmb()).isEqualTo(1289.0);
            assertThat(resultado.getTdee()).isEqualTo(1997.95);
        }

        @Test
        @DisplayName("id inexistente: lanza ResourceNotFoundException con el id en el mensaje")
        void idInexistente_lanzaResourceNotFoundException() {
            when(perfilRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getPerfil(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("updatePerfil")
    class UpdatePerfil {

        @Test
        @DisplayName("aplica los campos del request al perfil existente y persiste por repositorio")
        void aplicaCambiosYPersistePorRepositorio() {
            Perfil existente = new Perfil(1L, "Antonio", "a@test.com",
                    Sexo.H, NACIMIENTO_30, 170, 70.0, null, NivelActividad.SEDENTARIO);

            LocalDate nuevaFecha = LocalDate.now().minusYears(28);
            Perfil actualizado = new Perfil(1L, "Antonio", "a@test.com",
                    Sexo.M, nuevaFecha, 165, 62.0, 58.0, NivelActividad.LIGERO);

            when(perfilRepository.findById(1L)).thenReturn(Optional.of(existente));
            when(perfilRepository.update(eq(1L), any(Perfil.class))).thenReturn(actualizado);

            PerfilUpdateRequest request = new PerfilUpdateRequest();
            request.setSexo(Sexo.M);
            request.setFechaNacimiento(nuevaFecha);
            request.setAlturaCm(165);
            request.setPesoKgActual(62.0);
            request.setPesoObjetivo(58.0);
            request.setNivelActividad(NivelActividad.LIGERO);

            PerfilResponse resultado = service.updatePerfil(1L, request);

            verify(perfilRepository).update(eq(1L), any(Perfil.class));
            assertThat(resultado.getSexo()).isEqualTo(Sexo.M);
            assertThat(resultado.getAlturaCm()).isEqualTo(165);
            assertThat(resultado.getPesoKgActual()).isEqualTo(62.0);
            assertThat(resultado.getPesoObjetivo()).isEqualTo(58.0);
            assertThat(resultado.getNivelActividad()).isEqualTo(NivelActividad.LIGERO);
        }

        @Test
        @DisplayName("id inexistente: lanza ResourceNotFoundException con el id en el mensaje")
        void idInexistente_lanzaResourceNotFoundException() {
            when(perfilRepository.findById(99L)).thenReturn(Optional.empty());

            PerfilUpdateRequest request = new PerfilUpdateRequest();
            request.setSexo(Sexo.H);
            request.setFechaNacimiento(NACIMIENTO_30);
            request.setAlturaCm(170);
            request.setPesoKgActual(70.0);
            request.setNivelActividad(NivelActividad.SEDENTARIO);

            assertThatThrownBy(() -> service.updatePerfil(99L, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }
}
