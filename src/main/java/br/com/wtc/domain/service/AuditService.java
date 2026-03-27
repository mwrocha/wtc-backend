package br.com.wtc.domain.service;

import br.com.wtc.domain.model.AuditLog;
import br.com.wtc.domain.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(String action, String entity, String entityId,
                    String performedBy, String description) {
        AuditLog log = new AuditLog(action, entity, entityId, performedBy, description);
        auditLogRepository.save(log);
    }

    public void log(String action, String entity, String entityId,
                    String performedBy, String description,
                    Object before, Object after) {
        AuditLog log = new AuditLog(action, entity, entityId, performedBy, description);
        log.setBefore(before);
        log.setAfter(after);
        auditLogRepository.save(log);
    }

    public List<AuditLog> findAll() {
        return auditLogRepository.findAllByOrderByTimestampDesc();
    }

    public List<AuditLog> findByOperator(String email) {
        return auditLogRepository.findByPerformedByOrderByTimestampDesc(email);
    }

    public List<AuditLog> findByEntity(String entity) {
        return auditLogRepository.findByEntityOrderByTimestampDesc(entity);
    }

    public List<AuditLog> findByEntityAndId(String entity, String entityId) {
        return auditLogRepository.findByEntityAndEntityIdOrderByTimestampDesc(entity, entityId);
    }
}