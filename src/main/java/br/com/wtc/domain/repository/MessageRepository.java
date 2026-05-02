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

    // Conta mensagens 1:1 CHAT não lidas
    long countByRecipientIdAndReadFalseAndType(String recipientId, Message.MessageType type);

    // ← Conta mensagens de grupo CHAT não lidas (excluindo o próprio remetente)
    long countByGroupIdInAndReadFalseAndTypeAndSenderIdNot(List<String> groupIds, Message.MessageType type, String senderId);

    List<Message> findBySenderIdOrRecipientIdOrderByCreatedAtAsc(String senderId, String recipientId);

    List<Message> findByRecipientIdAndTypeOrderByCreatedAtDesc(String recipientId, Message.MessageType type);
}