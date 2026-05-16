package com.nutrifit.client.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Lee la URL del backend desde config.properties (classpath) o desde la
 * variable de entorno NUTRIFIT_BACKEND_URL (útil en instalaciones de producción).
 * El instalador generado por jpackage puede sobrescribir config.properties
 * para apuntar al servidor real sin que el usuario toque nada.
 */
public final class AppConfig {

    private static final String BACKEND_URL;

    static {
        // Variable de entorno tiene prioridad sobre el archivo
        String env = System.getenv("NUTRIFIT_BACKEND_URL");
        if (env != null && !env.isBlank()) {
            BACKEND_URL = env.trim();
        } else {
            BACKEND_URL = cargarDesdeProperties();
        }
    }

    private AppConfig() {}

    public static String getBackendUrl() {
        return BACKEND_URL;
    }

    private static String cargarDesdeProperties() {
        Properties props = new Properties();
        try (InputStream is = AppConfig.class.getResourceAsStream("/config.properties")) {
            if (is != null) {
                props.load(is);
                String url = props.getProperty("backend.url", "http://localhost:8080").trim();
                return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
            }
        } catch (IOException ignored) {
            // Si falla la lectura, el fallback garantiza que la app arranca
        }
        return "http://localhost:8080";
    }
}
