package com.nutrifit.backend.ejercicio.service;

import com.nutrifit.backend.common.exception.ResourceNotFoundException;
import com.nutrifit.backend.ejercicio.dto.RegistroEjercicioRequest;
import com.nutrifit.backend.ejercicio.dto.RegistroEjercicioResponse;
import com.nutrifit.backend.ejercicio.model.Ejercicio;
import com.nutrifit.backend.ejercicio.model.RegistroEjercicio;
import com.nutrifit.backend.ejercicio.repository.EjercicioRepository;
import com.nutrifit.backend.ejercicio.repository.RegistroEjercicioRepository;
import com.nutrifit.backend.perfil.model.Perfil;
import com.nutrifit.backend.perfil.repository.PerfilRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios de RegistroEjercicioServiceImpl.
 * Los repositorios se sustituyen por mocks de Mockito, por lo que estos tests
 * no necesitan base de datos ni contexto de Spring.
 */
@ExtendWith(MockitoExtension.class)
class RegistroEjercicioServiceImplTest {

    @Mock
    private RegistroEjercicioRepository registroRepository;

    @Mock
    private EjercicioRepository ejercicioRepository;

    @Mock
    private PerfilRepository perfilRepository;

    @InjectMocks
    private RegistroEjercicioServiceImpl service;

    // ---------------------------------------------------------------------------
    // Fixtures reutilizables
    // ---------------------------------------------------------------------------

    private static final Long USUARIO_ID   = 1L;
    private static final Long EJERCICIO_ID = 10L;
    private static final Long REGISTRO_ID  = 100L;
    private static final LocalDate FECHA   = LocalDate.of(2026, 3, 27);

    private Ejercicio ejercicioMock() {
        return new Ejercicio(EJERCICIO_ID, "Correr", 8.0, "CARDIO");
    }

    private Perfil perfilMock(double peso) {
        Perfil perfil = new Perfil();
        perfil.setPesoKgActual(peso);
        return perfil;
    }

    private RegistroEjercicioRequest requestMock(int duracionMin) {
        RegistroEjercicioRequest req = new RegistroEjercicioRequest();
        req.setEjercicioId(EJERCICIO_ID);
        req.setFecha(FECHA);
        req.setDuracionMin(duracionMin);
        return req;
    }

    private RegistroEjercicio registroGuardadoMock(double kcal) {
        RegistroEjercicio r = new RegistroEjercicio();
        r.setId(REGISTRO_ID);
        r.setUsuarioId(USUARIO_ID);
        r.setEjercicioId(EJERCICIO_ID);
        r.setFecha(FECHA);
        r.setDuracionMin(45);
        r.setKcalQuemadas(kcal);
        return r;
    }

    // ---------------------------------------------------------------------------
    // calcularKcal
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("calcularKcal")
    class CalcularKcal {

        @Test
        @DisplayName("correr (MET=8.0), 75 kg, 45 min → 450.00 kcal")
        void correr_75kg_45min_devuelve450() {
            double resultado = RegistroEjercicioServiceImpl.calcularKcal(8.0, 75.0, 45);
            assertThat(resultado).isEqualTo(450.00);
        }

        @Test
        @DisplayName("caminar (MET=3.5), 80 kg, 30 min → 140.00 kcal")
        void caminar_80kg_30min_devuelve140() {
            double resultado = RegistroEjercicioServiceImpl.calcularKcal(3.5, 80.0, 30);
            assertThat(resultado).isEqualTo(140.00);
        }

        @Test
        @DisplayName("yoga (MET=2.5), 60 kg, 60 min → 150.00 kcal")
        void yoga_60kg_60min_devuelve150() {
            double resultado = RegistroEjercicioServiceImpl.calcularKcal(2.5, 60.0, 60);
            assertThat(resultado).isEqualTo(150.00);
        }

        @Test
        @DisplayName("resultado con decimales se redondea a 2 cifras con HALF_UP")
        void resultadoConDecimales_seRedondeaADosCifras() {
            // MET=5.0, 70 kg, 20 min → 5.0 * 70 * (20/60.0) = 116.6666... → 116.67
            double resultado = RegistroEjercicioServiceImpl.calcularKcal(5.0, 70.0, 20);
            assertThat(resultado).isEqualTo(116.67);
        }
    }

    // ---------------------------------------------------------------------------
    // registrar
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("registrar")
    class Registrar {

        @Test
        @DisplayName("ejercicio y usuario existentes: delega en el repositorio con los parámetros correctos y devuelve DTO con kcal calculadas")
        void ambosExistentes_delegaYDevuelveDTO() {
            when(ejercicioRepository.findById(EJERCICIO_ID)).thenReturn(Optional.of(ejercicioMock()));
            when(perfilRepository.findById(USUARIO_ID)).thenReturn(Optional.of(perfilMock(75.0)));
            when(registroRepository.save(any(RegistroEjercicio.class)))
                    .thenReturn(registroGuardadoMock(450.00));

            RegistroEjercicioResponse resultado = service.registrar(USUARIO_ID, requestMock(45));

            assertThat(resultado.getId()).isEqualTo(REGISTRO_ID);
            assertThat(resultado.getUsuarioId()).isEqualTo(USUARIO_ID);
            assertThat(resultado.getEjercicioId()).isEqualTo(EJERCICIO_ID);
            assertThat(resultado.getFecha()).isEqualTo(FECHA);
            assertThat(resultado.getDuracionMin()).isEqualTo(45);
            assertThat(resultado.getKcalQuemadas()).isEqualTo(450.00);
            verify(registroRepository).save(any(RegistroEjercicio.class));
        }

        @Test
        @DisplayName("el nombreEjercicio en la response es el del ejercicio encontrado, no el que manda el cliente")
        void nombreEjercicio_provieneDeLaEntidadNoDelRequest() {
            Ejercicio ejercicio = new Ejercicio(EJERCICIO_ID, "Correr en cinta", 8.0, "CARDIO");
            when(ejercicioRepository.findById(EJERCICIO_ID)).thenReturn(Optional.of(ejercicio));
            when(perfilRepository.findById(USUARIO_ID)).thenReturn(Optional.of(perfilMock(75.0)));
            when(registroRepository.save(any(RegistroEjercicio.class)))
                    .thenReturn(registroGuardadoMock(450.00));

            RegistroEjercicioResponse resultado = service.registrar(USUARIO_ID, requestMock(45));

            assertThat(resultado.getNombreEjercicio()).isEqualTo("Correr en cinta");
        }

        @Test
        @DisplayName("las kcal quemadas se calculan correctamente a partir de MET, peso y duración")
        void kcalQuemadas_seCalculanCorrectamente() {
            when(ejercicioRepository.findById(EJERCICIO_ID)).thenReturn(Optional.of(ejercicioMock()));
            when(perfilRepository.findById(USUARIO_ID)).thenReturn(Optional.of(perfilMock(75.0)));

            ArgumentCaptor<RegistroEjercicio> captor = ArgumentCaptor.forClass(RegistroEjercicio.class);
            when(registroRepository.save(captor.capture())).thenReturn(registroGuardadoMock(450.00));

            service.registrar(USUARIO_ID, requestMock(45));

            assertThat(captor.getValue().getKcalQuemadas()).isEqualTo(450.00);
        }

        @Test
        @DisplayName("ejercicio inexistente: lanza ResourceNotFoundException sin consultar el perfil ni guardar")
        void ejercicioInexistente_lanzaExcepcionSinConsultarPerfilNiGuardar() {
            when(ejercicioRepository.findById(EJERCICIO_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.registrar(USUARIO_ID, requestMock(45)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(String.valueOf(EJERCICIO_ID));

            verify(perfilRepository, never()).findById(anyLong());
            verify(registroRepository, never()).save(any());
        }

        @Test
        @DisplayName("usuario inexistente (ejercicio ok): lanza ResourceNotFoundException sin guardar")
        void usuarioInexistente_lanzaExcepcionSinGuardar() {
            when(ejercicioRepository.findById(EJERCICIO_ID)).thenReturn(Optional.of(ejercicioMock()));
            when(perfilRepository.findById(USUARIO_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.registrar(USUARIO_ID, requestMock(45)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(String.valueOf(USUARIO_ID));

            verify(registroRepository, never()).save(any());
        }
    }

    // ---------------------------------------------------------------------------
    // findByUsuarioAndFecha
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("findByUsuarioAndFecha")
    class FindByUsuarioAndFecha {

        @Test
        @DisplayName("delega en el repositorio y devuelve su resultado directamente")
        void delegaEnRepositorioYDevuelveResultado() {
            RegistroEjercicioResponse respuesta = new RegistroEjercicioResponse(
                    REGISTRO_ID, USUARIO_ID, EJERCICIO_ID, "Correr", FECHA, 45, 450.00);
            when(registroRepository.findByUsuarioAndFecha(USUARIO_ID, FECHA))
                    .thenReturn(List.of(respuesta));

            List<RegistroEjercicioResponse> resultado = service.findByUsuarioAndFecha(USUARIO_ID, FECHA);

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getId()).isEqualTo(REGISTRO_ID);
            assertThat(resultado.get(0).getNombreEjercicio()).isEqualTo("Correr");
            assertThat(resultado.get(0).getKcalQuemadas()).isEqualTo(450.00);
            verify(registroRepository).findByUsuarioAndFecha(USUARIO_ID, FECHA);
        }

        @Test
        @DisplayName("repositorio vacío para esa fecha devuelve lista vacía sin errores")
        void repositorioVacio_devuelveListaVacia() {
            when(registroRepository.findByUsuarioAndFecha(USUARIO_ID, FECHA))
                    .thenReturn(List.of());

            List<RegistroEjercicioResponse> resultado = service.findByUsuarioAndFecha(USUARIO_ID, FECHA);

            assertThat(resultado).isEmpty();
        }
    }

    // ---------------------------------------------------------------------------
    // deleteById
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("deleteById")
    class DeleteById {

        @Test
        @DisplayName("registro existente y perteneciente al usuario: delega el borrado en el repositorio")
        void registroExistenteYPerteneciente_delegaBorrado() {
            RegistroEjercicio registro = registroGuardadoMock(450.00);
            when(registroRepository.findById(REGISTRO_ID)).thenReturn(Optional.of(registro));

            service.deleteById(USUARIO_ID, REGISTRO_ID);

            verify(registroRepository).deleteById(REGISTRO_ID);
        }

        @Test
        @DisplayName("registro inexistente: lanza ResourceNotFoundException sin borrar")
        void registroInexistente_lanzaExcepcionSinBorrar() {
            when(registroRepository.findById(REGISTRO_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deleteById(USUARIO_ID, REGISTRO_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(String.valueOf(REGISTRO_ID));

            verify(registroRepository, never()).deleteById(anyLong());
        }

        @Test
        @DisplayName("registro que no pertenece al usuario: lanza ResourceNotFoundException con ambos ids en el mensaje, sin borrar")
        void registroDeOtroUsuario_lanzaExcepcionConAmbosIdsYSinBorrar() {
            Long otroUsuarioId = 999L;
            RegistroEjercicio registroAjeno = new RegistroEjercicio();
            registroAjeno.setId(REGISTRO_ID);
            registroAjeno.setUsuarioId(otroUsuarioId);
            registroAjeno.setEjercicioId(EJERCICIO_ID);
            registroAjeno.setFecha(FECHA);
            registroAjeno.setDuracionMin(45);
            registroAjeno.setKcalQuemadas(450.00);

            when(registroRepository.findById(REGISTRO_ID)).thenReturn(Optional.of(registroAjeno));

            assertThatThrownBy(() -> service.deleteById(USUARIO_ID, REGISTRO_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(String.valueOf(REGISTRO_ID))
                    .hasMessageContaining(String.valueOf(USUARIO_ID));

            verify(registroRepository, never()).deleteById(anyLong());
        }
    }
}
