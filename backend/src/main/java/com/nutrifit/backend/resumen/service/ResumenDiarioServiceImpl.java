package com.nutrifit.backend.resumen.service;

import com.nutrifit.backend.perfil.service.PerfilService;
import com.nutrifit.backend.resumen.dto.ResumenDiarioResponse;
import com.nutrifit.backend.resumen.repository.ResumenDiarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class ResumenDiarioServiceImpl implements ResumenDiarioService {

    private final ResumenDiarioRepository resumenDiarioRepository;
    private final PerfilService perfilService;

    public ResumenDiarioServiceImpl(ResumenDiarioRepository resumenDiarioRepository,
                                    PerfilService perfilService) {
        this.resumenDiarioRepository = resumenDiarioRepository;
        this.perfilService = perfilService;
    }

    @Override
    public ResumenDiarioResponse obtenerResumenDiario(Long usuarioId, LocalDate fecha) {
        ResumenDiarioResponse resumen = resumenDiarioRepository.obtenerResumenDiario(usuarioId, fecha);
        enriquecerConTdee(resumen, usuarioId);
        return resumen;
    }

    private String estadoDesdeBalance(double balanceReal) {
        if (balanceReal > 100) return "SUPERAVIT";
        if (balanceReal < -100) return "DEFICIT";
        return "MANTENIMIENTO";
    }

    private void enriquecerConTdee(ResumenDiarioResponse resumen, Long usuarioId) {
        double tdee = 0;
        try {
            tdee = perfilService.getPerfil(usuarioId).getTdee();
        } catch (Exception e) {
            // usuario sin perfil: TDEE = 0, balance real = balance neto
        }
        double balanceReal = resumen.getKcalTotales() - tdee - resumen.getKcalQuemadasTotales();
        resumen.setTdee(tdee);
        resumen.setBalanceReal(balanceReal);
        resumen.setEstadoBalance(estadoDesdeBalance(balanceReal));
    }
}
