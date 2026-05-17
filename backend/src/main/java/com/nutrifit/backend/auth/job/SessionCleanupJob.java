package com.nutrifit.backend.auth.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SessionCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(SessionCleanupJob.class);

    private final JdbcTemplate jdbcTemplate;

    public SessionCleanupJob(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Scheduled(fixedRate = 3_600_000)
    public void eliminarSesionesExpiradas() {
        int eliminadas = jdbcTemplate.update(
                "DELETE FROM sesiones WHERE expires_at < NOW()"
        );
        if (eliminadas > 0) {
            log.info("SessionCleanupJob: {} sesion(es) expirada(s) eliminada(s)", eliminadas);
        }
    }
}
