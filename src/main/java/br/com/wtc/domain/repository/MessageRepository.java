package br.com.wtc.domain.repository;

import br.com.wtc.domain.model.Message;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<Message, String> {

    // Histórico de uma conversa (chat 1:1 ou grupo) ordenado por data
    List<Message> findByConversationIdOrderByCreatedAtAsc(String conversationId);

    // Mensagens não lidas de um destinatário — badge de notificação
    List<Message> findByRecipientIdAndReadFalse(String recipientId);

    // Mensagens de um grupo
    List<Message> findByGroupId(String groupId);

    // Mensagens por tipo — ex: todas as CAMPAIGN enviadas
    List<Message> findByType(Message.MessageType type);

    // Contagem de mensagens não lidas — usado no badge do app
    long countByRecipientIdAndReadFalse(String recipientId);

    // Todas as mensagens onde o usuário é remetente ou destinatário
    // Usado pelo cliente para buscar o histórico com o operador
    List<Message> findBySenderIdOrRecipientIdOrderByCreatedAtAsc(
            String senderId, String recipientId);
}