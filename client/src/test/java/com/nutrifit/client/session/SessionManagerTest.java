package com.nutrifit.client.session;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SessionManager")
class SessionManagerTest {

    @BeforeEach
    @AfterEach
    void resetSesion() {
        SessionManager.clear();
    }

    @Nested
    @DisplayName("setSession")
    class SetSession {

        @Test
        @DisplayName("almacena todos los campos correctamente")
        void almacena_todos_los_campos() {
            SessionManager.setSession(1L, "Ana", "ana@test.com", "token-abc");

            assertThat(SessionManager.getUsuarioId()).isEqualTo(1L);
            assertThat(SessionManager.getNombre()).isEqualTo("Ana");
            assertThat(SessionManager.getEmail()).isEqualTo("ana@test.com");
            assertThat(SessionManager.getToken()).isEqualTo("token-abc");
        }

        @Test
        @DisplayName("sobreescribe una sesión previa")
        void sobreescribe_sesion_previa() {
            SessionManager.setSession(1L, "Ana", "ana@test.com", "token-viejo");
            SessionManager.setSession(2L, "Luis", "luis@test.com", "token-nuevo");

            assertThat(SessionManager.getUsuarioId()).isEqualTo(2L);
            assertThat(SessionManager.getToken()).isEqualTo("token-nuevo");
        }
    }

    @Nested
    @DisplayName("isLoggedIn")
    class IsLoggedIn {

        @Test
        @DisplayName("devuelve true con token válido")
        void true_con_token_valido() {
            SessionManager.setSession(1L, "Ana", "ana@test.com", "token-abc");
            assertThat(SessionManager.isLoggedIn()).isTrue();
        }

        @Test
        @DisplayName("devuelve false sin sesión iniciada")
        void false_sin_sesion() {
            assertThat(SessionManager.isLoggedIn()).isFalse();
        }

        @Test
        @DisplayName("devuelve false con token en blanco")
        void false_con_token_en_blanco() {
            SessionManager.setSession(1L, "Ana", "ana@test.com", "   ");
            assertThat(SessionManager.isLoggedIn()).isFalse();
        }
    }

    @Nested
    @DisplayName("clear")
    class Clear {

        @Test
        @DisplayName("pone todos los campos a null y tdee a cero")
        void limpia_todos_los_campos() {
            SessionManager.setSession(1L, "Ana", "ana@test.com", "token-abc");
            SessionManager.setTdee(2000.0);

            SessionManager.clear();

            assertThat(SessionManager.getUsuarioId()).isNull();
            assertThat(SessionManager.getNombre()).isNull();
            assertThat(SessionManager.getEmail()).isNull();
            assertThat(SessionManager.getToken()).isNull();
            assertThat(SessionManager.getTdee()).isZero();
            assertThat(SessionManager.isLoggedIn()).isFalse();
        }
    }

    @Nested
    @DisplayName("TDEE")
    class Tdee {

        @Test
        @DisplayName("setTdee y getTdee funcionan correctamente")
        void set_get_tdee() {
            SessionManager.setTdee(2350.5);
            assertThat(SessionManager.getTdee()).isEqualTo(2350.5);
        }

        @Test
        @DisplayName("tdee por defecto es cero")
        void tdee_por_defecto_es_cero() {
            assertThat(SessionManager.getTdee()).isZero();
        }
    }
}
