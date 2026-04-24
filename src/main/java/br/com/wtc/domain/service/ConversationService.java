package br.com.wtc.domain.service;

import br.com.wtc.domain.model.AttendanceSession;
import br.com.wtc.domain.model.Conversation;
import br.com.wtc.domain.repository.AttendanceSessionRepository;
import br.com.wtc.domain.repository.ConversationRepository;
import br.com.wtc.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ConversationService {

    private static final Logger log = LoggerFactory.getLogger(ConversationService.class);

    private final ConversationRepository      conversationRepository;
    private final UserRepository              userRepository;
    private final AttendanceSessionRepository sessionRepository;

    public ConversationService(ConversationRepository conversationRepository,
                               UserRepository userRepository,
                               AttendanceSessionRepository sessionRepository) {
        this.conversationRepository = conversationRepository;
        this.userRepository         = userRepository;
        this.sessionRepository      = sessionRepository;
    }

    public void onClientMessageSent(String conversationId, String clientEmail,
                                    String messagePreview) {
        Optional<Conversation> existing = conversationRepository
                .findByConversationId(conversationId);

        if (existing.isPresent()) {
            Conversation conv = existing.get();
            conv.setLastMessagePreview(messagePreview);
            conv.setUpdatedAt(LocalDateTime.now());
            if (conv.getStatus() == Conversation.ConversationStatus.CLOSED) {
                conv.setStatus(Conversation.ConversationStatus.OPEN);
                conv.setAssignedOperatorEmail(null);
                conv.setAssumedAt(null);
                log.info("Conversa reaberta: {}", conversationId);
            }
            conversationRepository.save(conv);
        } else {
            Conversation conv = new Conversation();
            conv.setConversationId(conversationId);
            conv.setClientEmail(clientEmail);
            conv.setStatus(Conversation.ConversationStatus.OPEN);
            conv.setLastMessagePreview(messagePreview);
            conv.setCreatedAt(LocalDateTime.now());
            conv.setUpdatedAt(LocalDateTime.now());
            conversationRepository.save(conv);
            log.info("Nova conversa na fila: {}", conversationId);
        }
    }

    /**
     * Operador assume o atendimento.
     * Cria uma nova AttendanceSession para rastrear esta sessão.
     */
    public Conversation assumeConversation(String conversationId, String operatorEmail) {
        Conversation conv = conversationRepository
                .findByConversationId(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversa não encontrada: " + conversationId));

        if (conv.getStatus() == Conversation.ConversationStatus.IN_PROGRESS) {
            throw new RuntimeException("Conversa já está sendo atendida por "
                    + conv.getAssignedOperatorEmail());
        }

        conv.setStatus(Conversation.ConversationStatus.IN_PROGRESS);
        conv.setAssignedOperatorEmail(operatorEmail);
        conv.setAssumedAt(LocalDateTime.now());
        conv.setUpdatedAt(LocalDateTime.now());
        Conversation saved = conversationRepository.save(conv);

        // ── Cria nova sessão de atendimento ───────────────────────────────────
        try {
            AttendanceSession session = new AttendanceSession();
            session.setConversationId(conversationId);
            session.setClientEmail(conv.getClientEmail());
            session.setOperatorEmail(operatorEmail);
            session.setAssumedAt(LocalDateTime.now());
            session.setStatus(AttendanceSession.SessionStatus.IN_PROGRESS);
            sessionRepository.save(session);
            log.info("Sessão criada: {} → {}", operatorEmail, conversationId);
        } catch (Exception e) {
            log.warn("Falha ao criar sessão (não bloqueia): {}", e.getMessage());
        }

        return saved;
    }

    /**
     * Operador encerra o atendimento.
     * Fecha a AttendanceSession correspondente.
     */
    public Conversation closeConversation(String conversationId, String operatorEmail) {
        Conversation conv = conversationRepository
                .findByConversationId(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversa não encontrada: " + conversationId));

        conv.setStatus(Conversation.ConversationStatus.CLOSED);
        conv.setUpdatedAt(LocalDateTime.now());
        Conversation saved = conversationRepository.save(conv);
        log.info("Atendimento encerrado: {} por {}", conversationId, operatorEmail);

        // ── Fecha a sessão de atendimento ─────────────────────────────────────
        try {
            sessionRepository
                    .findByConversationIdAndStatus(
                            conversationId,
                            AttendanceSession.SessionStatus.IN_PROGRESS)
                    .ifPresent(session -> {
                        session.setStatus(AttendanceSession.SessionStatus.CLOSED);
                        session.setClosedAt(LocalDateTime.now());
                        sessionRepository.save(session);
                        log.info("Sessão encerrada: {} → {}", operatorEmail, conversationId);
                    });
        } catch (Exception e) {
            log.warn("Falha ao encerrar sessão (não bloqueia): {}", e.getMessage());
        }

        return saved;
    }

    public List<Map<String, Object>> getPendingConversations() {
        List<Conversation> pending = conversationRepository
                .findByStatus(Conversation.ConversationStatus.OPEN);

        return pending.stream().map(conv -> {
            var clientOpt = userRepository.findByEmail(conv.getClientEmail());
            String clientName = clientOpt.map(u -> u.getName()).orElse(conv.getClientEmail());
            String clientId   = clientOpt.map(u -> u.getId()).orElse("");

            Map<String, Object> result = new HashMap<>();
            result.put("conversationId",     conv.getConversationId());
            result.put("clientEmail",        conv.getClientEmail());
            result.put("clientName",         clientName);
            result.put("clientId",           clientId);
            result.put("lastMessagePreview", conv.getLastMessagePreview() != null
                    ? conv.getLastMessagePreview() : "");
            result.put("status",             conv.getStatus().name());
            result.put("createdAt",          conv.getCreatedAt() != null
                    ? conv.getCreatedAt().toString() : "");
            result.put("updatedAt",          conv.getUpdatedAt() != null
                    ? conv.getUpdatedAt().toString() : "");
            return result;
        }).toList();
    }

    public List<Map<String, Object>> getMyActiveConversations(String operatorEmail) {
        List<Conversation> active = conversationRepository
                .findByAssignedOperatorEmail(operatorEmail)
                .stream()
                .filter(c -> c.getStatus() == Conversation.ConversationStatus.IN_PROGRESS)
                .toList();

        return active.stream().map(conv -> {
            var clientOpt = userRepository.findByEmail(conv.getClientEmail());
            String clientName = clientOpt.map(u -> u.getName()).orElse(conv.getClientEmail());
            String clientId   = clientOpt.map(u -> u.getId()).orElse("");

            Map<String, Object> result = new HashMap<>();
            result.put("conversationId",     conv.getConversationId());
            result.put("clientEmail",        conv.getClientEmail());
            result.put("clientName",         clientName);
            result.put("clientId",           clientId);
            result.put("lastMessagePreview", conv.getLastMessagePreview() != null
                    ? conv.getLastMessagePreview() : "");
            result.put("status",             conv.getStatus().name());
            result.put("assumedAt",          conv.getAssumedAt() != null
                    ? conv.getAssumedAt().toString() : "");
            result.put("updatedAt",          conv.getUpdatedAt() != null
                    ? conv.getUpdatedAt().toString() : "");
            return result;
        }).toList();
    }

    /**
     * Estatísticas baseadas em AttendanceSessions — contagem precisa.
     * Cada assume+close = 1 sessão, independente de reaberturas.
     */
    public Map<String, Long> getMyStats(String operatorEmail) {
        long active = conversationRepository
                .findByAssignedOperatorEmail(operatorEmail)
                .stream()
                .filter(c -> c.getStatus() == Conversation.ConversationStatus.IN_PROGRESS)
                .count();

        LocalDate today     = LocalDate.now();
        YearMonth thisMonth = YearMonth.now();
        LocalDateTime monthStart = thisMonth.atDay(1).atStartOfDay();
        LocalDateTime monthEnd   = thisMonth.atEndOfMonth().atTime(23, 59, 59);

        List<AttendanceSession> closedThisMonth = sessionRepository
                .findByOperatorEmailAndStatusAndClosedAtBetween(
                        operatorEmail,
                        AttendanceSession.SessionStatus.CLOSED,
                        monthStart,
                        monthEnd
                );

        long closedToday = closedThisMonth.stream()
                .filter(s -> s.getClosedAt() != null
                        && s.getClosedAt().toLocalDate().equals(today))
                .count();

        Map<String, Long> stats = new HashMap<>();
        stats.put("active",    active);
        stats.put("today",     closedToday);
        stats.put("thisMonth", (long) closedThisMonth.size());
        return stats;
    }

    /**
     * Sessões encerradas recentes do operador — últimas 30.
     * Usada na AttendanceQueueScreen seção "Encerrados".
     */
    public List<Map<String, Object>> getMyClosedSessions(String operatorEmail) {
        return sessionRepository
                .findByOperatorEmailAndStatus(
                        operatorEmail,
                        AttendanceSession.SessionStatus.CLOSED)
                .stream()
                .filter(s -> s.getClosedAt() != null)
                .sorted((a, b) -> b.getClosedAt().compareTo(a.getClosedAt()))
                .limit(30)
                .map(session -> {
                    var clientOpt = userRepository.findByEmail(session.getClientEmail());
                    String clientName = clientOpt.map(u -> u.getName()).orElse(session.getClientEmail());
                    String clientId   = clientOpt.map(u -> u.getId()).orElse("");

                    Map<String, Object> result = new HashMap<>();
                    result.put("sessionId",      session.getId());
                    result.put("conversationId", session.getConversationId());
                    result.put("clientEmail",    session.getClientEmail());
                    result.put("clientName",     clientName);
                    result.put("clientId",       clientId);
                    result.put("assumedAt",      session.getAssumedAt() != null
                            ? session.getAssumedAt().toString() : "");
                    result.put("closedAt",       session.getClosedAt() != null
                            ? session.getClosedAt().toString() : "");
                    return result;
                })
                .toList();
    }

    public long countPending() {
        return conversationRepository.countByStatus(Conversation.ConversationStatus.OPEN);
    }

    public Optional<Conversation> getByConversationId(String conversationId) {
        return conversationRepository.findByConversationId(conversationId);
    }
}