package com.nutrifit.backend.hidratacion.service;

import com.nutrifit.backend.hidratacion.dto.AguaRequest;
import com.nutrifit.backend.hidratacion.dto.AguaResponse;
import com.nutrifit.backend.hidratacion.dto.HidratacionDiariaResponse;
import com.nutrifit.backend.hidratacion.repository.JdbcAguaRepository;
import com.nutrifit.backend.common.exception.UnauthorizedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class HidratacionServiceImpl implements HidratacionService {

    private static final int OBJETIVO_ML = 2000;

    private final JdbcAguaRepository repository;

    public HidratacionServiceImpl(JdbcAguaRepository repository) {
        this.repository = repository;
    }

    @Override
    public AguaResponse registrar(Long usuarioId, AguaRequest request) {
        return repository.save(usuarioId, request);
    }

    @Override
    public HidratacionDiariaResponse getDiario(Long usuarioId, LocalDate fecha) {
        List<AguaResponse> registros = repository.findByUsuarioAndFecha(usuarioId, fecha);

        int totalMl = registros.stream().mapToInt(AguaResponse::getCantidadMl).sum();
        int porcentaje = Math.min(100, (totalMl * 100) / OBJETIVO_ML);

        return new HidratacionDiariaResponse(fecha, totalMl, OBJETIVO_ML, porcentaje, registros);
    }

    @Override
    public void eliminar(Long usuarioId, Long registroId) {
        Long duenioId = repository.findUsuarioIdByRegistroId(registroId);
        if (duenioId == null || !duenioId.equals(usuarioId)) {
            throw new UnauthorizedException("Acceso denegado");
        }
        repository.deleteById(registroId);
    }
}
