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

    // ← Conta apenas mensagens de CHAT não lidas (exclui campanhas)
    long countByRecipientIdAndReadFalseAndType(
            String recipientId, Message.MessageType type);

    List<Message> findBySenderIdOrRecipientIdOrderByCreatedAtAsc(
            String senderId, String recipientId);

    List<Message> findByRecipientIdAndTypeOrderByCreatedAtDesc(
            String recipientId, Message.MessageType type);
}