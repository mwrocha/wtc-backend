package br.com.wtc.domain.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Document(collection = "messages")
public class Message {

    @Id
    private String id;
    private String conversationId;
    private String senderId;
    private String recipientId;
    private String groupId;
    private String title;
    private String body;
    private String url;
    private List<ActionButton> actions;
    private Map<String, String> actionUrls;
    private MessageType type;
    private boolean read;
    private boolean edited = false;   // ← novo
    private LocalDateTime createdAt;

    public Message() {}

    public Message(String id, String conversationId, String senderId,
                   String recipientId, String groupId, String title,
                   String body, String url, List<ActionButton> actions,
                   Map<String, String> actionUrls, MessageType type,
                   boolean read, LocalDateTime createdAt) {
        this.id = id; this.conversationId = conversationId;
        this.senderId = senderId; this.recipientId = recipientId;
        this.groupId = groupId; this.title = title; this.body = body;
        this.url = url; this.actions = actions; this.actionUrls = actionUrls;
        this.type = type; this.read = read; this.createdAt = createdAt;
    }

    public String getId()                           { return id; }
    public String getConversationId()               { return conversationId; }
    public String getSenderId()                     { return senderId; }
    public String getRecipientId()                  { return recipientId; }
    public String getGroupId()                      { return groupId; }
    public String getTitle()                        { return title; }
    public String getBody()                         { return body; }
    public String getUrl()                          { return url; }
    public List<ActionButton> getActions()          { return actions; }
    public Map<String, String> getActionUrls()      { return actionUrls; }
    public MessageType getType()                    { return type; }
    public boolean isRead()                         { return read; }
    public boolean isEdited()                       { return edited; }
    public LocalDateTime getCreatedAt()             { return createdAt; }

    public void setId(String id)                                { this.id = id; }
    public void setConversationId(String conversationId)        { this.conversationId = conversationId; }
    public void setSenderId(String senderId)                    { this.senderId = senderId; }
    public void setRecipientId(String recipientId)              { this.recipientId = recipientId; }
    public void setGroupId(String groupId)                      { this.groupId = groupId; }
    public void setTitle(String title)                          { this.title = title; }
    public void setBody(String body)                            { this.body = body; }
    public void setUrl(String url)                              { this.url = url; }
    public void setActions(List<ActionButton> actions)          { this.actions = actions; }
    public void setActionUrls(Map<String, String> actionUrls)   { this.actionUrls = actionUrls; }
    public void setType(MessageType type)                       { this.type = type; }
    public void setRead(boolean read)                           { this.read = read; }
    public void setEdited(boolean edited)                       { this.edited = edited; }
    public void setCreatedAt(LocalDateTime createdAt)           { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String id, conversationId, senderId, recipientId, groupId;
        private String title, body, url;
        private List<ActionButton> actions;
        private Map<String, String> actionUrls;
        private MessageType type;
        private boolean read;
        private LocalDateTime createdAt;

        public Builder id(String id)                            { this.id = id; return this; }
        public Builder conversationId(String c)                 { this.conversationId = c; return this; }
        public Builder senderId(String s)                       { this.senderId = s; return this; }
        public Builder recipientId(String r)                    { this.recipientId = r; return this; }
        public Builder groupId(String g)                        { this.groupId = g; return this; }
        public Builder title(String t)                          { this.title = t; return this; }
        public Builder body(String b)                           { this.body = b; return this; }
        public Builder url(String u)                            { this.url = u; return this; }
        public Builder actions(List<ActionButton> a)            { this.actions = a; return this; }
        public Builder actionUrls(Map<String, String> a)        { this.actionUrls = a; return this; }
        public Builder type(MessageType t)                      { this.type = t; return this; }
        public Builder read(boolean r)                          { this.read = r; return this; }
        public Builder createdAt(LocalDateTime t)               { this.createdAt = t; return this; }

        public Message build() {
            return new Message(id, conversationId, senderId, recipientId,
                    groupId, title, body, url, actions,
                    actionUrls, type, read, createdAt);
        }
    }

    public static class ActionButton {
        private String action;
        private String title;

        public ActionButton() {}
        public ActionButton(String action, String title) {
            this.action = action; this.title = title;
        }

        public String getAction()       { return action; }
        public String getTitle()        { return title; }
        public void setAction(String a) { this.action = a; }
        public void setTitle(String t)  { this.title = t; }
    }

    public enum MessageType {
        CHAT, CAMPAIGN, NOTIFICATION
    }
}