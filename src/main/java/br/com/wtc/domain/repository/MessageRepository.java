package br.com.wtc.domain.repository;

import br.com.wtc.domain.model.Message;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<Message, String> {

    List<Message> findByConversationIdOrderByCreatedAtAsc(String conversationId);

    List<Message> findByRecipientIdAndReadFalse(String recipientId);

    List<Message> findByGroupId(String groupId);

    List<Message> findByType(Message.MessageType type);

    long countByRecipientIdAndReadFalse(String recipientId);

    List<Message> findBySenderIdOrRecipientIdOrderByCreatedAtAsc(
            String senderId, String recipientId);

    // Campanhas recebidas por um destinatário específico
    List<Message> findByRecipientIdAndTypeOrderByCreatedAtDesc(
            String recipientId, Message.MessageType type);
}