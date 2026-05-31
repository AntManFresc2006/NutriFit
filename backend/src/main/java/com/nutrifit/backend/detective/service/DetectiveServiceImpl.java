package com.nutrifit.backend.detective.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutrifit.backend.detective.dto.DetectiveAnalisisDto;
import com.nutrifit.backend.detective.dto.DetectiveStatsDto;
import com.nutrifit.backend.detective.dto.HallazgoDto;
import com.nutrifit.backend.detective.repository.DetectiveRepository;
import com.nutrifit.backend.detective.repository.DiaForenseDto;
import com.nutrifit.backend.ia.dto.UsuarioIaConfigResponse;
import com.nutrifit.backend.ia.service.UsuarioIaConfigService;
import com.nutrifit.backend.perfil.dto.PerfilResponse;
import com.nutrifit.backend.perfil.service.PerfilService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DetectiveServiceImpl implements DetectiveService {

    private static final String MODEL_PRIMARY  = "google/gemma-4-31b-it:free";
    private static final String MODEL_FALLBACK = "openai/gpt-oss-20b:free";

    @Value("${openrouter.gemma.api.key}")
    private String gemmaApiKey;

    @Value("${openrouter.deepseek.api.key}")
    private String deepseekApiKey;

    private static final List<String> DIAS_SEMANA = List.of(
            "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo");

    private final DetectiveRepository repository;
    private final PerfilService perfilService;
    private final UsuarioIaConfigService usuarioIaConfigService;
    private final DetectiveIaAsync detectorIaAsync;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DetectiveServiceImpl(DetectiveRepository repository,
                                PerfilService perfilService,
                                UsuarioIaConfigService usuarioIaConfigService,
                                DetectiveIaAsync detectorIaAsync) {
        this.repository = repository;
        this.perfilService = perfilService;
        this.usuarioIaConfigService = usuarioIaConfigService;
        this.detectorIaAsync = detectorIaAsync;
    }

    @Override
    public DetectiveAnalisisDto iniciarAnalisis(Long usuarioId, int dias) {
        PerfilResponse perfil = perfilService.getPerfil(usuarioId);

        LocalDate hoy = LocalDate.now();
        LocalDate inicio = hoy.minusDays(dias - 1);
        List<DiaForenseDto> datos = repository.getDatosNutricionales(
                usuarioId, inicio.toString(), hoy.toString());

        DetectiveStatsDto stats = computarEstadisticas(datos, perfil, dias);
        List<HallazgoDto> hallazgos = generarHallazgos(stats, perfil);
        String prompt = buildPrompt(stats, perfil, dias);

        String statsJson = toJson(stats);
        String hallazgosJson = toJson(hallazgos);
        Long analisisId = repository.upsertAnalizando(usuarioId, dias, statsJson, hallazgosJson);

        Optional<UsuarioIaConfigResponse> userIaConfig = usuarioIaConfigService.getConfig(usuarioId);
        detectorIaAsync.generar(analisisId, prompt, userIaConfig.orElse(null),
                MODEL_PRIMARY, gemmaApiKey, MODEL_FALLBACK, deepseekApiKey);

        DetectiveAnalisisDto dto = new DetectiveAnalisisDto();
        dto.setId(analisisId);
        dto.setDiasAnalizados(dias);
        dto.setEstado("ANALIZANDO");
        dto.setEstadisticas(stats);
        dto.setHallazgos(hallazgos);
        return dto;
    }

    @Override
    public DetectiveAnalisisDto getAnalisis(Long usuarioId) {
        return repository.findByUsuario(usuarioId).orElse(null);
    }

    private DetectiveStatsDto computarEstadisticas(List<DiaForenseDto> datos, PerfilResponse perfil, int dias) {
        DetectiveStatsDto s = new DetectiveStatsDto();
        s.setDiasAnalizados(dias);

        List<DiaForenseDto> conRegistro = datos.stream()
                .filter(DiaForenseDto::isTieneRegistro).collect(Collectors.toList());

        s.setDiasConRegistro(conRegistro.size());
        s.setDiasSinRegistro(dias - conRegistro.size());
        s.setTasaRegistro(dias == 0 ? 0 : Math.round((conRegistro.size() * 100.0 / dias) * 10.0) / 10.0);

        double kcalMedia = conRegistro.isEmpty() ? 0 :
                conRegistro.stream().mapToDouble(DiaForenseDto::getKcal).average().orElse(0);
        s.setKcalMediaDiaria(Math.round(kcalMedia * 10.0) / 10.0);

        double tdee = perfil.getTdee();
        s.setDeficitRealMedio(Math.round((kcalMedia - tdee) * 10.0) / 10.0);
        s.setDiasSobreObjetivo((int) conRegistro.stream()
                .filter(d -> d.getKcal() > tdee).count());

        double protObjetivo = perfil.getPesoKgActual() * 1.6;
        s.setProteinaObjetivo(Math.round(protObjetivo * 10.0) / 10.0);

        double protMedia = conRegistro.isEmpty() ? 0 :
                conRegistro.stream().mapToDouble(DiaForenseDto::getProteinas).average().orElse(0);
        s.setProteinaMediaDiaria(Math.round(protMedia * 10.0) / 10.0);

        int diasCumpProt = (int) conRegistro.stream()
                .filter(d -> d.getProteinas() >= protObjetivo).count();
        s.setDiasCumpliendoProteina(diasCumpProt);
        s.setProteinaCumplimiento(conRegistro.isEmpty() ? 0 :
                Math.round((diasCumpProt * 100.0 / conRegistro.size()) * 10.0) / 10.0);

        // Kcal media por día de semana (solo días con registro)
        Map<String, List<Double>> porDia = new LinkedHashMap<>();
        for (String d : DIAS_SEMANA) porDia.put(d, new ArrayList<>());

        for (DiaForenseDto d : conRegistro) {
            String nombreDia = nombreDia(d.getFecha().getDayOfWeek());
            porDia.get(nombreDia).add(d.getKcal());
        }

        Map<String, Double> mediasPorDia = new LinkedHashMap<>();
        for (Map.Entry<String, List<Double>> e : porDia.entrySet()) {
            if (!e.getValue().isEmpty()) {
                double media = e.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0);
                mediasPorDia.put(e.getKey(), Math.round(media * 10.0) / 10.0);
            }
        }
        s.setKcalPorDiaSemana(mediasPorDia);

        if (!mediasPorDia.isEmpty()) {
            s.setPeorDiaSemana(mediasPorDia.entrySet().stream()
                    .max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null));
            s.setMejorDiaSemana(mediasPorDia.entrySet().stream()
                    .min(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null));
        }

        return s;
    }

    private List<HallazgoDto> generarHallazgos(DetectiveStatsDto s, PerfilResponse perfil) {
        List<HallazgoDto> hallazgos = new ArrayList<>();

        if (s.getDiasConRegistro() == 0) {
            hallazgos.add(new HallazgoDto("CRITICO",
                    "Sin datos suficientes",
                    "No hay comidas registradas en el período analizado. Registra tus comidas diariamente para obtener un análisis real."));
            return hallazgos;
        }

        // Registro incompleto
        if (s.getTasaRegistro() < 70) {
            hallazgos.add(new HallazgoDto("CRITICO",
                    s.getDiasSinRegistro() + " días sin registrar",
                    "El " + (int)(100 - s.getTasaRegistro()) + "% de los días no tienen registro. Los días sin anotar suelen ser los peores — el análisis real es probablemente más negativo."));
        }

        // Sin déficit real
        if (s.getDeficitRealMedio() >= 0) {
            long exceso = Math.round(s.getDeficitRealMedio());
            hallazgos.add(new HallazgoDto("CRITICO",
                    "Sin déficit real (+" + exceso + " kcal de media)",
                    "Consumes de media " + exceso + " kcal más de tu objetivo diario. No puede haber pérdida de peso sin déficit calórico."));
        } else if (s.getDeficitRealMedio() > -50) {
            long def = Math.round(Math.abs(s.getDeficitRealMedio()));
            hallazgos.add(new HallazgoDto("ADVERTENCIA",
                    "Déficit muy pequeño (" + def + " kcal/día)",
                    "Con solo " + def + " kcal de déficit diario, el progreso será muy lento. Se necesitan al menos 200-300 kcal de déficit para resultados visibles."));
        }

        // Proteína insuficiente
        if (s.getProteinaCumplimiento() < 50) {
            hallazgos.add(new HallazgoDto("ADVERTENCIA",
                    "Proteína insuficiente el " + (int)(100 - s.getProteinaCumplimiento()) + "% del tiempo",
                    "Tu objetivo es " + (int)s.getProteinaObjetivo() + "g/día pero solo lo alcanzas " + (int)s.getProteinaCumplimiento() + "% de los días. Esto favorece pérdida de músculo en lugar de grasa."));
        }

        // Patrón fin de semana
        Map<String, Double> mediasDia = s.getKcalPorDiaSemana();
        if (mediasDia != null) {
            double mediaGeneral = s.getKcalMediaDiaria();
            for (String finSemana : List.of("Sábado", "Domingo")) {
                if (mediasDia.containsKey(finSemana)) {
                    double exceso = mediasDia.get(finSemana) - mediaGeneral;
                    if (exceso > 200) {
                        hallazgos.add(new HallazgoDto("ADVERTENCIA",
                                finSemana + ": +" + Math.round(exceso) + " kcal sobre tu media",
                                "Los " + finSemana.toLowerCase() + "s consumes " + Math.round(exceso) + " kcal más que el resto de la semana. Dos días malos pueden eliminar el déficit de cinco días buenos."));
                    }
                }
            }
        }

        // Hallazgo positivo si todo va bien
        if (s.getTasaRegistro() >= 85 && s.getDeficitRealMedio() < -100) {
            long def = Math.round(Math.abs(s.getDeficitRealMedio()));
            hallazgos.add(new HallazgoDto("POSITIVO",
                    "Buen ritmo — déficit real de " + def + " kcal/día",
                    "Registras el " + (int)s.getTasaRegistro() + "% de los días y mantienes un déficit consistente. A este ritmo perderás aproximadamente " + Math.round(def * 7.0 / 7700) * 100 / 100.0 + " kg/semana."));
        }

        return hallazgos;
    }

    private String buildPrompt(DetectiveStatsDto s, PerfilResponse perfil, int dias) {
        StringBuilder sb = new StringBuilder();
        sb.append("Eres un detective nutricional experto. Analiza estos datos y descubre POR QUÉ el usuario ")
          .append("no alcanza sus objetivos nutricionales. Sé directo, empático y cita los números como evidencia irrefutable.\n\n");

        sb.append("PERFIL DEL CASO:\n");
        sb.append("- TDEE (objetivo calórico diario): ").append(Math.round(perfil.getTdee())).append(" kcal\n");
        sb.append("- Proteína objetivo: ").append(Math.round(s.getProteinaObjetivo())).append("g/día\n");
        sb.append("- Peso actual: ").append(perfil.getPesoKgActual()).append(" kg");
        if (perfil.getPesoObjetivo() != null) {
            sb.append(" | Objetivo: ").append(perfil.getPesoObjetivo()).append(" kg");
        }
        sb.append("\n\n");

        sb.append("EVIDENCIAS (últimos ").append(dias).append(" días):\n");
        sb.append("- Tasa de registro: ").append(s.getTasaRegistro()).append("% (")
          .append(s.getDiasSinRegistro()).append(" días sin datos)\n");
        sb.append("- Kcal media diaria (días registrados): ").append(Math.round(s.getKcalMediaDiaria())).append(" kcal\n");
        double deficit = s.getDeficitRealMedio();
        sb.append("- ").append(deficit < 0 ? "Déficit" : "Superávit").append(" real medio: ")
          .append(deficit < 0 ? "" : "+").append(Math.round(deficit)).append(" kcal/día\n");
        sb.append("- Días superando el TDEE: ").append(s.getDiasSobreObjetivo())
          .append("/").append(s.getDiasConRegistro()).append("\n");
        sb.append("- Proteína media: ").append(Math.round(s.getProteinaMediaDiaria())).append("g/día | ")
          .append("Cumplimiento: ").append(Math.round(s.getProteinaCumplimiento())).append("%\n\n");

        if (s.getKcalPorDiaSemana() != null && !s.getKcalPorDiaSemana().isEmpty()) {
            sb.append("PATRÓN POR DÍA DE SEMANA (kcal media):\n");
            for (Map.Entry<String, Double> e : s.getKcalPorDiaSemana().entrySet()) {
                sb.append("- ").append(e.getKey()).append(": ").append(Math.round(e.getValue())).append(" kcal\n");
            }
            sb.append("\n");
        }

        sb.append("Escribe 4-5 párrafos narrativos (NO uses listas con viñetas). Estructura:\n");
        sb.append("1. El 'culpable principal' con los datos exactos como evidencia\n");
        sb.append("2. Efecto de cada problema sobre el progreso real del usuario\n");
        sb.append("3. 2-3 correcciones concretas y alcanzables\n");
        sb.append("4. Proyección: qué pasaría si el usuario corrige el problema principal\n");
        sb.append("Estilo: empático pero directo, como Sherlock Holmes de la nutrición. Máximo 350 palabras.");

        return sb.toString();
    }

    private String nombreDia(DayOfWeek dow) {
        return switch (dow) {
            case MONDAY    -> "Lunes";
            case TUESDAY   -> "Martes";
            case WEDNESDAY -> "Miércoles";
            case THURSDAY  -> "Jueves";
            case FRIDAY    -> "Viernes";
            case SATURDAY  -> "Sábado";
            case SUNDAY    -> "Domingo";
        };
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }
}
