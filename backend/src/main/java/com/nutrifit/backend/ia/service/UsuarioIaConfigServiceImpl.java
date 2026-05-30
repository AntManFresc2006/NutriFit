package com.nutrifit.backend.ia.service;

import com.nutrifit.backend.ia.dto.IaTestResponse;
import com.nutrifit.backend.ia.dto.UsuarioIaConfigRequest;
import com.nutrifit.backend.ia.dto.UsuarioIaConfigResponse;
import com.nutrifit.backend.ia.model.UsuarioIaConfig;
import com.nutrifit.backend.ia.repository.UsuarioIaConfigRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;

/**
 * Implementación del servicio de configuración de IA.
 * Proporciona conversión entre modelos y DTOs.
 */
@Service
public class UsuarioIaConfigServiceImpl implements UsuarioIaConfigService {

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    private final UsuarioIaConfigRepository repository;

    public UsuarioIaConfigServiceImpl(UsuarioIaConfigRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UsuarioIaConfigResponse> getConfig(Long usuarioId) {
        return repository.findByUsuarioId(usuarioId)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public UsuarioIaConfigResponse saveConfig(Long usuarioId, UsuarioIaConfigRequest request) {
        validateProxyUrl(request.getProxyUrl());
        UsuarioIaConfig config = toModel(usuarioId, request);
        repository.save(usuarioId, config);
        return toResponse(config);
    }

    @Override
    @Transactional
    public void deleteConfig(Long usuarioId) {
        repository.deleteByUsuarioId(usuarioId);
    }

    @Override
    public IaTestResponse testConfig(UsuarioIaConfigRequest request) {
        validateProxyUrl(request.getProxyUrl());
        String baseUrl = request.getProxyUrl().trim().replaceAll("/+$", "");
        String url = baseUrl + "/chat/completions";
        String body = "{\"model\":\"%s\",\"messages\":[{\"role\":\"user\",\"content\":\"hi\"}],\"max_tokens\":1}"
                .formatted(request.getModel().trim().replace("\"", "\\\""));
        try {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + request.getApiKey().trim())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(10))
                    .build();
            HttpResponse<String> response = HTTP_CLIENT
                    .send(httpRequest, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            if (status >= 200 && status < 300) {
                return new IaTestResponse(true, null);
            }
            if (status == 401 || status == 403) {
                return new IaTestResponse(false, "API Key incorrecta o sin permisos (HTTP " + status + ")");
            }
            if (status == 404) {
                return new IaTestResponse(false, "URL del proxy o modelo no encontrado (HTTP 404)");
            }
            if (status == 400) {
                return new IaTestResponse(false, "El modelo no es válido o la petición es incorrecta (HTTP 400)");
            }
            return new IaTestResponse(false, "Error del servidor remoto (HTTP " + status + ")");
        } catch (java.net.ConnectException e) {
            return new IaTestResponse(false, "No se puede conectar al proxy. Verifica la URL.");
        } catch (java.net.UnknownHostException e) {
            return new IaTestResponse(false, "URL del proxy no válida o inaccesible.");
        } catch (IllegalArgumentException e) {
            return new IaTestResponse(false, "La URL del proxy tiene un formato incorrecto.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new IaTestResponse(false, "La petición fue interrumpida.");
        } catch (Exception e) {
            return new IaTestResponse(false, "Error inesperado al probar la conexión.");
        }
    }

    private UsuarioIaConfig toModel(Long usuarioId, UsuarioIaConfigRequest request) {
        UsuarioIaConfig config = new UsuarioIaConfig();
        config.setUsuarioId(usuarioId);
        config.setProxyUrl(request.getProxyUrl().trim());
        config.setModel(request.getModel().trim());
        config.setApiKey(request.getApiKey().trim());
        return config;
    }

    private UsuarioIaConfigResponse toResponse(UsuarioIaConfig config) {
        return new UsuarioIaConfigResponse(
                config.getProxyUrl(),
                config.getModel(),
                config.getApiKey()
        );
    }

    private static final Set<String> BLOCKED_HOST_PREFIXES = Set.of(
            "localhost", "127.", "10.", "172.16.", "172.17.", "172.18.", "172.19.",
            "172.20.", "172.21.", "172.22.", "172.23.", "172.24.", "172.25.", "172.26.",
            "172.27.", "172.28.", "172.29.", "172.30.", "172.31.",
            "192.168.", "169.254.", "::1", "0."
    );

    private void validateProxyUrl(String rawUrl) {
        if (rawUrl == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "proxyUrl es obligatoria");
        }
        URI uri;
        try {
            uri = URI.create(rawUrl.trim());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "proxyUrl tiene un formato inválido");
        }
        if (!"https".equalsIgnoreCase(uri.getScheme())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "proxyUrl debe usar HTTPS");
        }
        String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase();
        boolean blocked = BLOCKED_HOST_PREFIXES.stream().anyMatch(host::startsWith)
                || host.equals("::1");
        if (blocked) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "proxyUrl apunta a una dirección no permitida");
        }
    }
}
