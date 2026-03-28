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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Lógica de autenticación: crea cuentas, abre y cierra sesiones.
 *
 * <p>Las contraseñas nunca se almacenan en texto plano: {@code PasswordService}
 * delega en BCrypt. Los tokens son UUIDs aleatorios, sin estado en el servidor
 * más allá de su presencia en la tabla {@code sesiones}.</p>
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final SesionRepository sesionRepository;
    private final PasswordService passwordService;
    private final TokenService tokenService;

    public AuthServiceImpl(
            UsuarioRepository usuarioRepository,
            SesionRepository sesionRepository,
            PasswordService passwordService,
            TokenService tokenService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.sesionRepository = sesionRepository;
        this.passwordService = passwordService;
        this.tokenService = tokenService;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        // Normalizar email antes de buscar y guardar para evitar duplicados por capitalización
        String email = request.getEmail().trim().toLowerCase();

        usuarioRepository.findByEmail(email).ifPresent(usuario -> {
            throw new BadRequestException("Ya existe un usuario registrado con ese email");
        });

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre().trim());
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordService.hash(request.getPassword()));

        Usuario guardado = usuarioRepository.save(usuario);

        // Abrir sesión automáticamente tras el registro: el cliente no necesita hacer login por separado
        String token = tokenService.generateToken();
        Sesion sesion = new Sesion();
        sesion.setUsuarioId(guardado.getId());
        sesion.setToken(token);
        sesion.setExpiresAt(LocalDateTime.now().plusDays(7));
        sesionRepository.save(sesion);

        return new AuthResponse(
                guardado.getId(),
                guardado.getNombre(),
                guardado.getEmail(),
                token
        );
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        // El mismo mensaje tanto si el email no existe como si la contraseña es incorrecta,
        // para no facilitar la enumeración de cuentas registradas
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Credenciales inválidas"));

        boolean passwordOk = passwordService.matches(request.getPassword(), usuario.getPasswordHash());
        if (!passwordOk) {
            throw new UnauthorizedException("Credenciales inválidas");
        }

        String token = tokenService.generateToken();
        Sesion sesion = new Sesion();
        sesion.setUsuarioId(usuario.getId());
        sesion.setToken(token);
        sesion.setExpiresAt(LocalDateTime.now().plusDays(7));
        sesionRepository.save(sesion);

        return new AuthResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                token
        );
    }

    @Override
    public void logout(String token) {
        if (token == null || token.isBlank()) {
            throw new BadRequestException("El token es obligatorio para cerrar sesión");
        }

        // Borrar el token invalida la sesión; el AuthInterceptor rechazará cualquier petición futura con él
        sesionRepository.deleteByToken(token);
    }
}