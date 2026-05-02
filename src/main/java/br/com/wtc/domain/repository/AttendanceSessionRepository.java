package br.com.wtc.domain.repository;

import br.com.wtc.domain.model.AttendanceSession;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceSessionRepository extends MongoRepository<AttendanceSession, String> {

    // Sessões de um operador
    List<AttendanceSession> findByOperatorEmail(String operatorEmail);

    // Sessão IN_PROGRESS de uma conversa específica (para encerrar)
    Optional<AttendanceSession> findByConversationIdAndStatus(String conversationId, AttendanceSession.SessionStatus status);

    // Sessões encerradas de um operador em um período
    List<AttendanceSession> findByOperatorEmailAndStatusAndClosedAtBetween(String operatorEmail, AttendanceSession.SessionStatus status, LocalDateTime from, LocalDateTime to);

    // Sessões encerradas de um operador (todas)
    List<AttendanceSession> findByOperatorEmailAndStatus(String operatorEmail, AttendanceSession.SessionStatus status);

    // Sessões encerradas de um cliente (para avaliação)
    List<AttendanceSession> findByClientEmailAndStatus(String clientEmail, AttendanceSession.SessionStatus status);
}