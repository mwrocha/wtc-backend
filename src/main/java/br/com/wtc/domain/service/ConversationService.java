package br.com.wtc.domain.service;

import br.com.wtc.domain.model.Conversation;
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

    private final ConversationRepository conversationRepository;
    private final UserRepository         userRepository;

    public ConversationService(ConversationRepository conversationRepository,
                               UserRepository userRepository) {
        this.conversationRepository = conversationRepository;
        this.userRepository         = userRepository;
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
        return conversationRepository.save(conv);
    }

    public Conversation closeConversation(String conversationId, String operatorEmail) {
        Conversation conv = conversationRepository
                .findByConversationId(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversa não encontrada: " + conversationId));

        conv.setStatus(Conversation.ConversationStatus.CLOSED);
        conv.setUpdatedAt(LocalDateTime.now());
        log.info("Atendimento encerrado: {} por {}", conversationId, operatorEmail);
        return conversationRepository.save(conv);
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
     * Estatísticas de atendimento do operador logado:
     * - active:    conversas IN_PROGRESS agora
     * - today:     conversas CLOSED com updatedAt hoje
     * - thisMonth: conversas CLOSED com updatedAt neste mês
     */
    public Map<String, Long> getMyStats(String operatorEmail) {
        List<Conversation> all = conversationRepository
                .findByAssignedOperatorEmail(operatorEmail);

        LocalDate today     = LocalDate.now();
        YearMonth thisMonth = YearMonth.now();

        long active = all.stream()
                .filter(c -> c.getStatus() == Conversation.ConversationStatus.IN_PROGRESS)
                .count();

        long closedToday = all.stream()
                .filter(c -> c.getStatus() == Conversation.ConversationStatus.CLOSED
                        && c.getUpdatedAt() != null
                        && c.getUpdatedAt().toLocalDate().equals(today))
                .count();

        long closedThisMonth = all.stream()
                .filter(c -> c.getStatus() == Conversation.ConversationStatus.CLOSED
                        && c.getUpdatedAt() != null
                        && YearMonth.from(c.getUpdatedAt()).equals(thisMonth))
                .count();

        Map<String, Long> stats = new HashMap<>();
        stats.put("active",       active);
        stats.put("today",        closedToday);
        stats.put("thisMonth",    closedThisMonth);
        return stats;
    }

    public long countPending() {
        return conversationRepository.countByStatus(Conversation.ConversationStatus.OPEN);
    }

    public Optional<Conversation> getByConversationId(String conversationId) {
        return conversationRepository.findByConversationId(conversationId);
    }
}