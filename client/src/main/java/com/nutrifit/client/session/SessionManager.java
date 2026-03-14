package com.nutrifit.client.session;

/**
 * Mantiene en memoria la sesión activa del cliente JavaFX.
 * Por ahora solo guarda los datos básicos devueltos por login.
 */
public class SessionManager {

    private static Long usuarioId;
    private static String nombre;
    private static String email;
    private static String token;

    public static void setSession(Long usuarioId, String nombre, String email, String token) {
        SessionManager.usuarioId = usuarioId;
        SessionManager.nombre = nombre;
        SessionManager.email = email;
        SessionManager.token = token;
    }

    public static void clear() {
        usuarioId = null;
        nombre = null;
        email = null;
        token = null;
    }

    public static Long getUsuarioId() {
        return usuarioId;
    }

    public static String getNombre() {
        return nombre;
    }

    public static String getEmail() {
        return email;
    }

    public static String getToken() {
        return token;
    }

    public static boolean isLoggedIn() {
        return token != null && !token.isBlank();
    }
}