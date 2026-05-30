package com.nutrifit.backend.detective.service;

import com.nutrifit.backend.detective.dto.DetectiveAnalisisDto;

public interface DetectiveService {

    DetectiveAnalisisDto iniciarAnalisis(Long usuarioId, int dias);

    DetectiveAnalisisDto getAnalisis(Long usuarioId);
}
