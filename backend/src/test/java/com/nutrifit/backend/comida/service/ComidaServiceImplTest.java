package com.nutrifit.backend.comida.service;

import com.nutrifit.backend.alimento.model.Alimento;
import com.nutrifit.backend.alimento.repository.AlimentoRepository;
import com.nutrifit.backend.comida.dto.ComidaAlimentoRequest;
import com.nutrifit.backend.comida.dto.ComidaItemDetalleResponse;
import com.nutrifit.backend.comida.dto.ComidaRequest;
import com.nutrifit.backend.comida.dto.ComidaResponse;
import com.nutrifit.backend.comida.model.Comida;
import com.nutrifit.backend.comida.model.ComidaAlimento;
import com.nutrifit.backend.comida.repository.ComidaRepository;
import com.nutrifit.backend.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios de ComidaServiceImpl.
 * Los repositorios se sustituyen por mocks de Mockito, por lo que estos tests
 * no necesitan base de datos ni contexto de Spring.
 */
@ExtendWith(MockitoExtension.class)
class ComidaServiceImplTest {

    @Mock
    private ComidaRepository comidaRepository;

    @Mock
    private AlimentoRepository alimentoRepository;

    @InjectMocks
    private ComidaServiceImpl service;

    // ---------------------------------------------------------------------------
    // Fixtures reutilizables
    // ---------------------------------------------------------------------------

    private static final Long USUARIO_ID  = 1L;
    private static final Long COMIDA_ID   = 10L;
    private static final Long ITEM_ID     = 100L;
    private static final Long ALIMENTO_ID = 5L;
    private static final LocalDate FECHA  = LocalDate.of(2026, 3, 27);

    private Comida comidaMock() {
        return new Comida(COMIDA_ID, USUARIO_ID, FECHA, "DESAYUNO");
    }

    private ComidaRequest requestMock(String tipo) {
        ComidaRequest req = new ComidaRequest();
        req.setFecha(FECHA);
        req.setTipo(tipo);
        return req;
    }

    private ComidaAlimentoRequest alimentoRequestMock() {
        ComidaAlimentoRequest req = new ComidaAlimentoRequest();
        req.setAlimentoId(ALIMENTO_ID);
        req.setGramos(150.0);
        return req;
    }

    private Alimento alimentoMock() {
        return new Alimento(
                ALIMENTO_ID, "Pechuga de pollo",
                new BigDecimal("100"), new BigDecimal("165"),
                new BigDecimal("31.0"), new BigDecimal("3.6"),
                new BigDecimal("0.0"), "USDA"
        );
    }

    private ComidaAlimento itemMock(Long comidaId) {
        return new ComidaAlimento(ITEM_ID, comidaId, ALIMENTO_ID, 150.0);
    }

    private ComidaItemDetalleResponse detalleMock() {
        return new ComidaItemDetalleResponse(
                ITEM_ID, COMIDA_ID, ALIMENTO_ID,
                "Pechuga de pollo", 150.0,
                247.5, 46.5, 5.4, 0.0
        );
    }

    // ---------------------------------------------------------------------------
    // save
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("crea la comida y devuelve el DTO con los datos correctos")
        void guardaYDevuelveDTO() {
            when(comidaRepository.save(any(Comida.class))).thenReturn(comidaMock());

            ComidaResponse resultado = service.save(USUARIO_ID, requestMock("DESAYUNO"));

            assertThat(resultado.getId()).isEqualTo(COMIDA_ID);
            assertThat(resultado.getUsuarioId()).isEqualTo(USUARIO_ID);
            assertThat(resultado.getFecha()).isEqualTo(FECHA);
            assertThat(resultado.getTipo()).isEqualTo("DESAYUNO");
            verify(comidaRepository).save(any(Comida.class));
        }

        @Test
        @DisplayName("el tipo del request se recorta y convierte a mayúsculas antes de persistir")
        void tipoConEspaciosYMinusculas_seNormaliza() {
            when(comidaRepository.save(any(Comida.class))).thenReturn(comidaMock());
            ArgumentCaptor<Comida> captor = ArgumentCaptor.forClass(Comida.class);

            service.save(USUARIO_ID, requestMock("  desayuno  "));

            verify(comidaRepository).save(captor.capture());
            assertThat(captor.getValue().getTipo()).isEqualTo("DESAYUNO");
        }

        @Test
        @DisplayName("el usuarioId y la fecha se asignan correctamente a la entidad persistida")
        void usuarioIdYFechaSeAsignanCorrectamente() {
            when(comidaRepository.save(any(Comida.class))).thenReturn(comidaMock());
            ArgumentCaptor<Comida> captor = ArgumentCaptor.forClass(Comida.class);

            service.save(USUARIO_ID, requestMock("ALMUERZO"));

            verify(comidaRepository).save(captor.capture());
            assertThat(captor.getValue().getUsuarioId()).isEqualTo(USUARIO_ID);
            assertThat(captor.getValue().getFecha()).isEqualTo(FECHA);
        }
    }

    // ---------------------------------------------------------------------------
    // findByUsuarioAndFecha
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("findByUsuarioAndFecha")
    class FindByUsuarioAndFecha {

        @Test
        @DisplayName("delega en el repositorio y mapea correctamente a DTOs")
        void delegaYMapea() {
            when(comidaRepository.findByUsuarioAndFecha(USUARIO_ID, FECHA))
                    .thenReturn(List.of(comidaMock()));

            List<ComidaResponse> resultado = service.findByUsuarioAndFecha(USUARIO_ID, FECHA);

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getId()).isEqualTo(COMIDA_ID);
            assertThat(resultado.get(0).getTipo()).isEqualTo("DESAYUNO");
            verify(comidaRepository).findByUsuarioAndFecha(USUARIO_ID, FECHA);
        }

        @Test
        @DisplayName("repositorio vacío para esa fecha devuelve lista vacía sin errores")
        void sinComidas_devuelveListaVacia() {
            when(comidaRepository.findByUsuarioAndFecha(USUARIO_ID, FECHA))
                    .thenReturn(List.of());

            List<ComidaResponse> resultado = service.findByUsuarioAndFecha(USUARIO_ID, FECHA);

            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("varias comidas en el mismo día se mapean todas al DTO")
        void variasComidas_seMapeanTodas() {
            Comida almuerzo = new Comida(11L, USUARIO_ID, FECHA, "ALMUERZO");
            Comida cena     = new Comida(12L, USUARIO_ID, FECHA, "CENA");
            when(comidaRepository.findByUsuarioAndFecha(USUARIO_ID, FECHA))
                    .thenReturn(List.of(almuerzo, cena));

            List<ComidaResponse> resultado = service.findByUsuarioAndFecha(USUARIO_ID, FECHA);

            assertThat(resultado).hasSize(2);
            assertThat(resultado).extracting(ComidaResponse::getTipo)
                    .containsExactly("ALMUERZO", "CENA");
        }
    }

    // ---------------------------------------------------------------------------
    // deleteById
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("deleteById")
    class DeleteById {

        @Test
        @DisplayName("id existente: elimina la comida llamando al repositorio")
        void idExistente_eliminaComida() {
            when(comidaRepository.findById(COMIDA_ID)).thenReturn(Optional.of(comidaMock()));

            service.deleteById(COMIDA_ID);

            verify(comidaRepository).deleteById(COMIDA_ID);
        }

        @Test
        @DisplayName("id inexistente: lanza ResourceNotFoundException sin llamar a deleteById")
        void idInexistente_lanzaExcepcionSinEliminar() {
            when(comidaRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deleteById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");

            verify(comidaRepository, never()).deleteById(anyLong());
        }
    }

    // ---------------------------------------------------------------------------
    // addAlimentoToComida
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("addAlimentoToComida")
    class AddAlimentoToComida {

        @Test
        @DisplayName("comida y alimento existentes: delega en el repositorio con los parámetros correctos")
        void ambosExistentes_delegaEnRepositorio() {
            when(comidaRepository.findById(COMIDA_ID)).thenReturn(Optional.of(comidaMock()));
            when(alimentoRepository.findById(ALIMENTO_ID)).thenReturn(Optional.of(alimentoMock()));

            service.addAlimentoToComida(COMIDA_ID, alimentoRequestMock());

            verify(comidaRepository).addAlimentoToComida(COMIDA_ID, ALIMENTO_ID, 150.0);
        }

        @Test
        @DisplayName("comida inexistente: lanza ResourceNotFoundException sin consultar el alimento")
        void comidaInexistente_lanzaExcepcionSinConsultarAlimento() {
            when(comidaRepository.findById(COMIDA_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.addAlimentoToComida(COMIDA_ID, alimentoRequestMock()))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(String.valueOf(COMIDA_ID));

            verify(alimentoRepository, never()).findById(anyLong());
            verify(comidaRepository, never()).addAlimentoToComida(anyLong(), anyLong(), anyDouble());
        }

        @Test
        @DisplayName("alimento inexistente: lanza ResourceNotFoundException sin añadir el item")
        void alimentoInexistente_lanzaExcepcionSinAnadirItem() {
            when(comidaRepository.findById(COMIDA_ID)).thenReturn(Optional.of(comidaMock()));
            when(alimentoRepository.findById(ALIMENTO_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.addAlimentoToComida(COMIDA_ID, alimentoRequestMock()))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(String.valueOf(ALIMENTO_ID));

            verify(comidaRepository, never()).addAlimentoToComida(anyLong(), anyLong(), anyDouble());
        }
    }

    // ---------------------------------------------------------------------------
    // findDetalleItemsByComidaId
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("findDetalleItemsByComidaId")
    class FindDetalleItemsByComidaId {

        @Test
        @DisplayName("comida existente: devuelve la lista de items enriquecidos con datos nutricionales")
        void comidaExistente_devuelveItems() {
            when(comidaRepository.findById(COMIDA_ID)).thenReturn(Optional.of(comidaMock()));
            when(comidaRepository.findDetalleItemsByComidaId(COMIDA_ID))
                    .thenReturn(List.of(detalleMock()));

            List<ComidaItemDetalleResponse> resultado =
                    service.findDetalleItemsByComidaId(COMIDA_ID);

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getItemId()).isEqualTo(ITEM_ID);
            assertThat(resultado.get(0).getNombre()).isEqualTo("Pechuga de pollo");
            assertThat(resultado.get(0).getGramos()).isEqualTo(150.0);
            verify(comidaRepository).findDetalleItemsByComidaId(COMIDA_ID);
        }

        @Test
        @DisplayName("comida existente sin items: devuelve lista vacía sin errores")
        void comidaSinItems_devuelveListaVacia() {
            when(comidaRepository.findById(COMIDA_ID)).thenReturn(Optional.of(comidaMock()));
            when(comidaRepository.findDetalleItemsByComidaId(COMIDA_ID)).thenReturn(List.of());

            List<ComidaItemDetalleResponse> resultado =
                    service.findDetalleItemsByComidaId(COMIDA_ID);

            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("comida inexistente: lanza ResourceNotFoundException sin consultar items")
        void comidaInexistente_lanzaExcepcionSinConsultarItems() {
            when(comidaRepository.findById(COMIDA_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findDetalleItemsByComidaId(COMIDA_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(String.valueOf(COMIDA_ID));

            verify(comidaRepository, never()).findDetalleItemsByComidaId(anyLong());
        }
    }

    // ---------------------------------------------------------------------------
    // deleteItem
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("deleteItem")
    class DeleteItem {

        @Test
        @DisplayName("item existente y perteneciente a la comida: delega el borrado en el repositorio")
        void itemExistenteYPerteneciente_delegaBorrado() {
            when(comidaRepository.findItemById(ITEM_ID))
                    .thenReturn(Optional.of(itemMock(COMIDA_ID)));

            service.deleteItem(COMIDA_ID, ITEM_ID);

            verify(comidaRepository).deleteItemById(ITEM_ID);
        }

        @Test
        @DisplayName("item inexistente: lanza ResourceNotFoundException con el id del item en el mensaje")
        void itemInexistente_lanzaExcepcion() {
            when(comidaRepository.findItemById(ITEM_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deleteItem(COMIDA_ID, ITEM_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(String.valueOf(ITEM_ID));

            verify(comidaRepository, never()).deleteItemById(anyLong());
        }

        @Test
        @DisplayName("item que no pertenece a la comida: lanza ResourceNotFoundException con ambos ids en el mensaje")
        void itemDeOtraComida_lanzaExcepcionSinBorrar() {
            Long otraComidaId = 999L;
            when(comidaRepository.findItemById(ITEM_ID))
                    .thenReturn(Optional.of(itemMock(otraComidaId)));

            assertThatThrownBy(() -> service.deleteItem(COMIDA_ID, ITEM_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(String.valueOf(ITEM_ID))
                    .hasMessageContaining(String.valueOf(COMIDA_ID));

            verify(comidaRepository, never()).deleteItemById(anyLong());
        }
    }
}
