package com.nutrifit.backend.auth.service;

import com.nutrifit.backend.auth.dto.AuthResponse;
import com.nutrifit.backend.auth.dto.LoginRequest;
import com.nutrifit.backend.auth.dto.RegisterRequest;
import com.nutrifit.backend.auth.model.Sesion;
import com.nutrifit.backend.auth.model.Usuario;
import com.nutrifit.backend.auth.repository.SesionRepository;
import com.nutrifit.backend.auth.repository.UsuarioRepository;
import com.nutrifit.backend.auth.security.PasswordService;
import com.nutrifit.backend.auth.security.TokenService;
import com.nutrifit.backend.common.exception.BadRequestException;
import com.nutrifit.backend.common.exception.UnauthorizedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios de AuthServiceImpl.
 * Todos los colaboradores (repositorios, servicios de seguridad) son mocks,
 * por lo que no se necesita base de datos ni contexto de Spring.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private SesionRepository sesionRepository;

    @Mock
    private PasswordService passwordService;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthServiceImpl service;

    // ---------------------------------------------------------------------------
    // Fixtures reutilizables
    // ---------------------------------------------------------------------------

    private RegisterRequest registerRequest(String nombre, String email, String password) {
        RegisterRequest req = new RegisterRequest();
        req.setNombre(nombre);
        req.setEmail(email);
        req.setPassword(password);
        return req;
    }

    private LoginRequest loginRequest(String email, String password) {
        LoginRequest req = new LoginRequest();
        req.setEmail(email);
        req.setPassword(password);
        return req;
    }

    private Usuario usuarioGuardado() {
        return new Usuario(1L, "Ana García", "ana@ejemplo.com", "hash_bcrypt");
    }

    // ---------------------------------------------------------------------------
    // register
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("register")
    class Register {

        @Test
        @DisplayName("email ya registrado lanza BadRequestException sin guardar nada")
        void emailDuplicado_lanzaExcepcion() {
            when(usuarioRepository.findByEmail("ana@ejemplo.com"))
                    .thenReturn(Optional.of(usuarioGuardado()));

            assertThatThrownBy(() -> service.register(
                    registerRequest("Ana", "ana@ejemplo.com", "secreto123")))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Ya existe un usuario registrado con ese email");

            verify(usuarioRepository, never()).save(any());
            verify(sesionRepository, never()).save(any());
        }

        @Test
        @DisplayName("registro exitoso guarda usuario, crea sesión y devuelve AuthResponse")
        void registroExitoso_devuelveAuthResponse() {
            when(usuarioRepository.findByEmail("ana@ejemplo.com")).thenReturn(Optional.empty());
            when(passwordService.hash("secreto123")).thenReturn("hash_bcrypt");
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioGuardado());
            when(tokenService.generateToken()).thenReturn("token-abc-123");
            when(sesionRepository.save(any(Sesion.class))).thenAnswer(inv -> inv.getArgument(0));

            AuthResponse respuesta = service.register(
                    registerRequest("Ana García", "ana@ejemplo.com", "secreto123"));

            assertThat(respuesta.getUsuarioId()).isEqualTo(1L);
            assertThat(respuesta.getNombre()).isEqualTo("Ana García");
            assertThat(respuesta.getEmail()).isEqualTo("ana@ejemplo.com");
            assertThat(respuesta.getToken()).isEqualTo("token-abc-123");

            // Verificar que se creó la sesión con el token y una expiración futura
            ArgumentCaptor<Sesion> sesionCaptor = ArgumentCaptor.forClass(Sesion.class);
            verify(sesionRepository).save(sesionCaptor.capture());
            Sesion sesionGuardada = sesionCaptor.getValue();
            assertThat(sesionGuardada.getToken()).isEqualTo("token-abc-123");
            assertThat(sesionGuardada.getExpiresAt()).isAfter(LocalDateTime.now());
        }

        @Test
        @DisplayName("email con mayúsculas y espacios se normaliza antes de buscar y persistir")
        void emailConMayusculas_seNormaliza() {
            when(usuarioRepository.findByEmail("ana@ejemplo.com")).thenReturn(Optional.empty());
            when(passwordService.hash(anyString())).thenReturn("hash");
            when(tokenService.generateToken()).thenReturn("token");

            ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
            when(usuarioRepository.save(usuarioCaptor.capture())).thenReturn(usuarioGuardado());
            when(sesionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.register(registerRequest("Ana", "  ANA@EJEMPLO.COM  ", "secreto123"));

            // El email persistido debe estar en minúsculas y sin espacios
            assertThat(usuarioCaptor.getValue().getEmail()).isEqualTo("ana@ejemplo.com");
            // La búsqueda de duplicado también debe usar el email normalizado
            verify(usuarioRepository).findByEmail("ana@ejemplo.com");
        }
    }

    // ---------------------------------------------------------------------------
    // login
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("email no registrado lanza UnauthorizedException con mensaje genérico")
        void emailInexistente_lanzaExcepcion() {
            when(usuarioRepository.findByEmail("fantasma@ejemplo.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.login(
                    loginRequest("fantasma@ejemplo.com", "cualquierCosa")))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("Credenciales inválidas");
        }

        @Test
        @DisplayName("contraseña incorrecta lanza UnauthorizedException con el mismo mensaje genérico")
        void passwordIncorrecto_lanzaExcepcionSinCrearSesion() {
            when(usuarioRepository.findByEmail("ana@ejemplo.com"))
                    .thenReturn(Optional.of(usuarioGuardado()));
            when(passwordService.matches("contraseñaMal", "hash_bcrypt")).thenReturn(false);

            assertThatThrownBy(() -> service.login(
                    loginRequest("ana@ejemplo.com", "contraseñaMal")))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("Credenciales inválidas");

            // El mismo mensaje para email inexistente y contraseña errónea: no filtra información
            verify(sesionRepository, never()).save(any());
        }

        @Test
        @DisplayName("login exitoso crea sesión y devuelve AuthResponse con los datos del usuario")
        void loginExitoso_devuelveAuthResponse() {
            when(usuarioRepository.findByEmail("ana@ejemplo.com"))
                    .thenReturn(Optional.of(usuarioGuardado()));
            when(passwordService.matches("secreto123", "hash_bcrypt")).thenReturn(true);
            when(tokenService.generateToken()).thenReturn("token-xyz-789");
            when(sesionRepository.save(any(Sesion.class))).thenAnswer(inv -> inv.getArgument(0));

            AuthResponse respuesta = service.login(loginRequest("ana@ejemplo.com", "secreto123"));

            assertThat(respuesta.getUsuarioId()).isEqualTo(1L);
            assertThat(respuesta.getNombre()).isEqualTo("Ana García");
            assertThat(respuesta.getEmail()).isEqualTo("ana@ejemplo.com");
            assertThat(respuesta.getToken()).isEqualTo("token-xyz-789");

            ArgumentCaptor<Sesion> sesionCaptor = ArgumentCaptor.forClass(Sesion.class);
            verify(sesionRepository).save(sesionCaptor.capture());
            assertThat(sesionCaptor.getValue().getUsuarioId()).isEqualTo(1L);
            assertThat(sesionCaptor.getValue().getExpiresAt()).isAfter(LocalDateTime.now());
        }
    }

    // ---------------------------------------------------------------------------
    // logout
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("logout")
    class Logout {

        @Test
        @DisplayName("token null lanza BadRequestException")
        void tokenNull_lanzaExcepcion() {
            assertThatThrownBy(() -> service.logout(null))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("El token es obligatorio para cerrar sesión");

            verify(sesionRepository, never()).deleteByToken(anyString());
        }

        @Test
        @DisplayName("token en blanco lanza BadRequestException")
        void tokenEnBlanco_lanzaExcepcion() {
            assertThatThrownBy(() -> service.logout("   "))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("El token es obligatorio para cerrar sesión");

            verify(sesionRepository, never()).deleteByToken(anyString());
        }

        @Test
        @DisplayName("token válido delega el borrado en el repositorio")
        void tokenValido_eliminaSesion() {
            service.logout("token-abc-123");

            verify(sesionRepository).deleteByToken("token-abc-123");
        }
    }
}
