package br.com.wtc.domain.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Representa uma sessão de atendimento individual.
 * Criada quando operador assume e atualizada quando encerra.
 * Permite contagem precisa de atendimentos mesmo com reaberturas.
 */
@Document(collection = "attendance_sessions")
public class AttendanceSession {

    @Id
    private String id;

    // Referência à conversa
    private String conversationId;

    // Emails envolvidos
    private String clientEmail;
    private String operatorEmail;

    // Ciclo da sessão
    private LocalDateTime assumedAt;
    private LocalDateTime closedAt;

    // Status da sessão
    private SessionStatus status = SessionStatus.IN_PROGRESS;

    public enum SessionStatus {
        IN_PROGRESS, CLOSED
    }

    public AttendanceSession() {
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public String getId() {
        return id;
    }

    public String getConversationId() {
        return conversationId;
    }

    public String getClientEmail() {
        return clientEmail;
    }

    public String getOperatorEmail() {
        return operatorEmail;
    }

    public LocalDateTime getAssumedAt() {
        return assumedAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public SessionStatus getStatus() {
        return status;
    }

    // ── Setters ───────────────────────────────────────────────────────────────
    public void setId(String id) {
        this.id = id;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public void setClientEmail(String clientEmail) {
        this.clientEmail = clientEmail;
    }

    public void setOperatorEmail(String operatorEmail) {
        this.operatorEmail = operatorEmail;
    }

    public void setAssumedAt(LocalDateTime assumedAt) {
        this.assumedAt = assumedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }
}