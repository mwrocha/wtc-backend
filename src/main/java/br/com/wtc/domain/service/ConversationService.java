package br.com.wtc.domain.service;

import br.com.wtc.domain.model.Conversation;
import br.com.wtc.domain.repository.ConversationRepository;
import br.com.wtc.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    /**
     * Cria ou atualiza a entrada de conversa quando um cliente envia mensagem.
     * Se estava CLOSED, reabre como OPEN para qualquer operador ver.
     */
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
     * Operador assume o atendimento de uma conversa.
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
        return conversationRepository.save(conv);
    }

    /**
     * Operador encerra o atendimento.
     */
    public Conversation closeConversation(String conversationId, String operatorEmail) {
        Conversation conv = conversationRepository
                .findByConversationId(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversa não encontrada: " + conversationId));

        conv.setStatus(Conversation.ConversationStatus.CLOSED);
        conv.setUpdatedAt(LocalDateTime.now());
        log.info("Atendimento encerrado: {} por {}", conversationId, operatorEmail);
        return conversationRepository.save(conv);
    }

    /**
     * Lista conversas OPEN com clientId incluído para navegação direta no app.
     * Usa HashMap para suportar mais de 10 campos (limite do Map.of).
     */
    public List<Map<String, Object>> getPendingConversations() {
        List<Conversation> pending = conversationRepository
                .findByStatus(Conversation.ConversationStatus.OPEN);

        return pending.stream().map(conv -> {
            // Busca o cliente pelo email para obter o id e o nome
            var clientOpt = userRepository.findByEmail(conv.getClientEmail());
            String clientName = clientOpt.map(u -> u.getName()).orElse(conv.getClientEmail());
            String clientId   = clientOpt.map(u -> u.getId()).orElse("");

            Map<String, Object> result = new HashMap<>();
            result.put("conversationId",     conv.getConversationId());
            result.put("clientEmail",        conv.getClientEmail());
            result.put("clientName",         clientName);
            result.put("clientId",           clientId);   // ← novo campo para navegação direta
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

    /**
     * Conta conversas abertas — badge do dashboard.
     */
    public long countPending() {
        return conversationRepository.countByStatus(Conversation.ConversationStatus.OPEN);
    }

    /**
     * Status atual de uma conversa.
     */
    public Optional<Conversation> getByConversationId(String conversationId) {
        return conversationRepository.findByConversationId(conversationId);
    }
}