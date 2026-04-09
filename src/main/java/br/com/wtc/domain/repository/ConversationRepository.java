package br.com.wtc.domain.repository;

import br.com.wtc.domain.model.Conversation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends MongoRepository<Conversation, String> {

    Optional<Conversation> findByConversationId(String conversationId);

    // Todas as conversas abertas (sem operador atribuído)
    List<Conversation> findByStatus(Conversation.ConversationStatus status);

    // Conversas de um operador específico
    List<Conversation> findByAssignedOperatorEmail(String operatorEmail);

    // Conta conversas abertas
    long countByStatus(Conversation.ConversationStatus status);
}