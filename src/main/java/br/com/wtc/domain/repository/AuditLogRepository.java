package br.com.wtc.domain.repository;

import br.com.wtc.domain.model.AuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AuditLogRepository extends MongoRepository<AuditLog, String> {
    List<AuditLog> findByPerformedByOrderByTimestampDesc(String performedBy);
    List<AuditLog> findByEntityOrderByTimestampDesc(String entity);
    List<AuditLog> findByEntityAndEntityIdOrderByTimestampDesc(String entity, String entityId);
    List<AuditLog> findAllByOrderByTimestampDesc();
}