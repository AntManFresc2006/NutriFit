package com.nutrifit.client.session;

/**
 * Mantiene en memoria la sesión activa del cliente JavaFX.
 * Guarda los datos básicos de login y el TDEE del perfil del usuario.
 */
public class SessionManager {

    private static Long usuarioId;
    private static String nombre;
    private static String email;
    private static String token;
    private static double tdee;

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
        tdee = 0;
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

    public static double getTdee() {
        return tdee;
    }

    public static void setTdee(double tdee) {
        SessionManager.tdee = tdee;
    }

    public static boolean isLoggedIn() {
        return token != null && !token.isBlank();
    }
}
