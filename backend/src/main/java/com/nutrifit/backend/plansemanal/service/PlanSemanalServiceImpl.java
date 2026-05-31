package com.nutrifit.backend.plansemanal.service;

import com.nutrifit.backend.ia.dto.UsuarioIaConfigResponse;
import com.nutrifit.backend.ia.service.UsuarioIaConfigService;
import com.nutrifit.backend.perfil.dto.PerfilResponse;
import com.nutrifit.backend.perfil.service.PerfilService;
import com.nutrifit.backend.plansemanal.dto.PlanSemanalResponse;
import com.nutrifit.backend.plansemanal.repository.PlanSemanalRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Implementación del servicio de plan semanal.
 * Registra el plan como GENERANDO de forma inmediata y delega la generación IA
 * en {@link PlanGeneradorAsync} para que se ejecute en segundo plano.
 */
@Service
public class PlanSemanalServiceImpl implements PlanSemanalService {

    private static final String MODEL_PRIMARY  = "google/gemma-4-31b-it:free";
    private static final String MODEL_FALLBACK = "openai/gpt-oss-20b:free";

    @Value("${openrouter.gemma.api.key}")
    private String gemmaApiKey;

    @Value("${openrouter.deepseek.api.key}")
    private String deepseekApiKey;

    private final PlanSemanalRepository repository;
    private final PerfilService perfilService;
    private final UsuarioIaConfigService usuarioIaConfigService;
    private final PlanGeneradorAsync planGeneradorAsync;

    public PlanSemanalServiceImpl(PlanSemanalRepository repository,
                                  PerfilService perfilService,
                                  UsuarioIaConfigService usuarioIaConfigService,
                                  PlanGeneradorAsync planGeneradorAsync) {
        this.repository = repository;
        this.perfilService = perfilService;
        this.usuarioIaConfigService = usuarioIaConfigService;
        this.planGeneradorAsync = planGeneradorAsync;
    }

    @Override
    @Transactional
    public PlanSemanalResponse generarORecuperarPlan(Long usuarioId, LocalDate semanaInicio) {
        Optional<PlanSemanalResponse> existente = repository.findByUsuarioAndSemana(usuarioId, semanaInicio);

        // Si ya está listo o generándose, devolver el estado actual
        if (existente.isPresent() && !"ERROR".equals(existente.get().getEstado())) {
            return existente.get();
        }

        PerfilResponse perfil = perfilService.getPerfil(usuarioId);
        String prompt = buildPrompt(semanaInicio, perfil);
        Optional<UsuarioIaConfigResponse> userIaConfig = usuarioIaConfigService.getConfig(usuarioId);

        Long planId = repository.createGenerando(usuarioId, semanaInicio);

        planGeneradorAsync.generar(
                planId, prompt, userIaConfig.orElse(null),
                MODEL_PRIMARY, gemmaApiKey,
                MODEL_FALLBACK, deepseekApiKey
        );

        return repository.findByUsuarioAndSemana(usuarioId, semanaInicio).orElseThrow();
    }

    @Override
    @Transactional(readOnly = true)
    public PlanSemanalResponse getPlan(Long usuarioId, LocalDate semanaInicio) {
        return repository.findByUsuarioAndSemana(usuarioId, semanaInicio).orElse(null);
    }

    @Override
    @Transactional
    public void eliminarPlan(Long usuarioId, LocalDate semanaInicio) {
        repository.deleteByUsuarioAndSemana(usuarioId, semanaInicio);
    }

    private String buildPrompt(LocalDate semanaInicio, PerfilResponse perfil) {
        double proteinasDiarias = perfil.getPesoKgActual() * 0.8;

        return "Eres un nutricionista experto. Genera un plan de alimentación semanal para 7 días (lunes a domingo) comenzando el " + semanaInicio + ".\n\n" +
                "Datos del usuario:\n" +
                "- TDEE: " + Math.round(perfil.getTdee()) + " kcal/día\n" +
                "- Objetivo calórico diario: " + Math.round(perfil.getTdee()) + " kcal\n" +
                "- Proteínas objetivo: " + String.format("%.1f", proteinasDiarias) + "g/día (aprox)\n\n" +
                "Genera el plan en formato JSON estrictamente así (sin markdown, sin explicaciones, solo el JSON):\n" +
                "{\n" +
                "  \"dias\": [\n" +
                "    {\n" +
                "      \"dia\": \"Lunes\",\n" +
                "      \"fecha\": \"YYYY-MM-DD\",\n" +
                "      \"comidas\": {\n" +
                "        \"desayuno\": { \"descripcion\": \"...\", \"kcal\": 400, \"proteinas\": 20, \"carbos\": 45, \"grasas\": 15 },\n" +
                "        \"almuerzo\": { \"descripcion\": \"...\", \"kcal\": 600, \"proteinas\": 35, \"carbos\": 60, \"grasas\": 20 },\n" +
                "        \"merienda\": { \"descripcion\": \"...\", \"kcal\": 200, \"proteinas\": 10, \"carbos\": 25, \"grasas\": 5 },\n" +
                "        \"cena\": { \"descripcion\": \"...\", \"kcal\": 500, \"proteinas\": 30, \"carbos\": 45, \"grasas\": 18 }\n" +
                "      },\n" +
                "      \"totalKcal\": 1700,\n" +
                "      \"totalProteinas\": 95,\n" +
                "      \"totalCarbos\": 175,\n" +
                "      \"totalGrasas\": 58\n" +
                "    }\n" +
                "  ]\n" +
                "}\n\n" +
                "Varía los alimentos cada día. Usa alimentos mediterráneos típicos. Las fechas de cada día deben ser correlativos comenzando en " + semanaInicio + ".";
    }
}
