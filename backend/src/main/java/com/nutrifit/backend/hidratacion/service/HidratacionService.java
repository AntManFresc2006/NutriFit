package com.nutrifit.backend.hidratacion.service;

import com.nutrifit.backend.hidratacion.dto.AguaRequest;
import com.nutrifit.backend.hidratacion.dto.AguaResponse;
import com.nutrifit.backend.hidratacion.dto.HidratacionDiariaResponse;
import java.time.LocalDate;

public interface HidratacionService {
    AguaResponse registrar(Long usuarioId, AguaRequest request);
    HidratacionDiariaResponse getDiario(Long usuarioId, LocalDate fecha);
    void eliminar(Long usuarioId, Long registroId);
}
