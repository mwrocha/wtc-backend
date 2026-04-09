package br.com.wtc.domain.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Representa uma conversa 1:1 entre cliente e operador.
 * Controla o status de atendimento e o operador responsável.
 */
@Document(collection = "conversations")
public class Conversation {

    @Id
    private String id;

    // Ex: "bilopes@teste.com_maykeop@teste.com"
    private String conversationId;

    // Email do cliente
    private String clientEmail;

    // Email do operador que assumiu (null = sem atendimento)
    private String assignedOperatorEmail;

    // Status do atendimento
    private ConversationStatus status = ConversationStatus.OPEN;

    // Última mensagem para exibir na fila
    private String lastMessagePreview;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime assumedAt;

    public enum ConversationStatus {
        OPEN,        // Aguardando atendimento
        IN_PROGRESS, // Sendo atendida por um operador
        CLOSED       // Encerrada
    }

    public Conversation() {}

    // ── Getters ───────────────────────────────────────────────────────────────
    public String getId()                        { return id; }
    public String getConversationId()            { return conversationId; }
    public String getClientEmail()               { return clientEmail; }
    public String getAssignedOperatorEmail()     { return assignedOperatorEmail; }
    public ConversationStatus getStatus()        { return status; }
    public String getLastMessagePreview()        { return lastMessagePreview; }
    public LocalDateTime getCreatedAt()          { return createdAt; }
    public LocalDateTime getUpdatedAt()          { return updatedAt; }
    public LocalDateTime getAssumedAt()          { return assumedAt; }

    // ── Setters ───────────────────────────────────────────────────────────────
    public void setId(String id)                                    { this.id = id; }
    public void setConversationId(String conversationId)            { this.conversationId = conversationId; }
    public void setClientEmail(String clientEmail)                  { this.clientEmail = clientEmail; }
    public void setAssignedOperatorEmail(String email)              { this.assignedOperatorEmail = email; }
    public void setStatus(ConversationStatus status)                { this.status = status; }
    public void setLastMessagePreview(String lastMessagePreview)    { this.lastMessagePreview = lastMessagePreview; }
    public void setCreatedAt(LocalDateTime createdAt)               { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt)               { this.updatedAt = updatedAt; }
    public void setAssumedAt(LocalDateTime assumedAt)               { this.assumedAt = assumedAt; }
}