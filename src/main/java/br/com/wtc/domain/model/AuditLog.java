package br.com.wtc.domain.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "audit_logs")
public class AuditLog {

    @Id
    private String id;

    private String action;       // CREATE, UPDATE, DELETE, DISPATCH, LOGIN, etc.
    private String entity;       // Campaign, Group, Division, User, Task, etc.
    private String entityId;     // ID do objeto afetado
    private String performedBy;  // Email do operador/usuário
    private String description;  // Descrição legível da operação
    private Object before;       // Estado anterior (opcional)
    private Object after;        // Estado posterior (opcional)
    private LocalDateTime timestamp;

    public AuditLog() {}

    public AuditLog(String action, String entity, String entityId,
                    String performedBy, String description) {
        this.action      = action;
        this.entity      = entity;
        this.entityId    = entityId;
        this.performedBy = performedBy;
        this.description = description;
        this.timestamp   = LocalDateTime.now();
    }

    // Getters e Setters
    public String getId()                   { return id; }
    public void setId(String id)            { this.id = id; }
    public String getAction()               { return action; }
    public void setAction(String action)    { this.action = action; }
    public String getEntity()               { return entity; }
    public void setEntity(String entity)    { this.entity = entity; }
    public String getEntityId()             { return entityId; }
    public void setEntityId(String entityId){ this.entityId = entityId; }
    public String getPerformedBy()              { return performedBy; }
    public void setPerformedBy(String p)        { this.performedBy = p; }
    public String getDescription()              { return description; }
    public void setDescription(String d)        { this.description = d; }
    public Object getBefore()               { return before; }
    public void setBefore(Object before)    { this.before = before; }
    public Object getAfter()                { return after; }
    public void setAfter(Object after)      { this.after = after; }
    public LocalDateTime getTimestamp()         { return timestamp; }
    public void setTimestamp(LocalDateTime t)   { this.timestamp = t; }
}