package br.com.wtc.domain.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Avaliação de atendimento feita pelo cliente ao final de uma sessão.
 * Vinculada a uma AttendanceSession pelo sessionId.
 */
@Document(collection = "attendance_ratings")
public class AttendanceRating {

    @Id
    private String id;

    // Referência à sessão avaliada
    private String sessionId;
    private String conversationId;

    // Emails envolvidos
    private String clientEmail;
    private String operatorEmail;

    // Avaliação
    private int    stars;       // 1 a 5
    private String comment;     // opcional

    private LocalDateTime createdAt;

    public AttendanceRating() {}

    // ── Getters ───────────────────────────────────────────────────────────────
    public String getId()             { return id; }
    public String getSessionId()      { return sessionId; }
    public String getConversationId() { return conversationId; }
    public String getClientEmail()    { return clientEmail; }
    public String getOperatorEmail()  { return operatorEmail; }
    public int getStars()             { return stars; }
    public String getComment()        { return comment; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // ── Setters ───────────────────────────────────────────────────────────────
    public void setId(String id)                       { this.id = id; }
    public void setSessionId(String sessionId)         { this.sessionId = sessionId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
    public void setClientEmail(String clientEmail)     { this.clientEmail = clientEmail; }
    public void setOperatorEmail(String operatorEmail) { this.operatorEmail = operatorEmail; }
    public void setStars(int stars)                    { this.stars = stars; }
    public void setComment(String comment)             { this.comment = comment; }
    public void setCreatedAt(LocalDateTime createdAt)  { this.createdAt = createdAt; }
}