package br.com.wtc.web.controller;

import br.com.wtc.domain.model.Message;
import br.com.wtc.domain.repository.MessageRepository;
import br.com.wtc.domain.repository.UserRepository;
import br.com.wtc.domain.service.MessageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    public MessageController(MessageService messageService, UserRepository userRepository, MessageRepository messageRepository) {
        this.messageService = messageService;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
    }

    // POST /api/messages/direct
    @PostMapping("/direct")
    public ResponseEntity<Message> sendDirect(@RequestBody Map<String, String> body, @AuthenticationPrincipal UserDetails userDetails) {

        String recipientId = body.get("recipientId");
        String title = body.get("title");
        String text = body.get("body");

        if (recipientId == null || text == null) return ResponseEntity.badRequest().build();

        return ResponseEntity.status(HttpStatus.CREATED).body(messageService.sendDirect(userDetails.getUsername(), recipientId, title, text));
    }

    // POST /api/messages/group
    @PostMapping("/group")
    public ResponseEntity<Message> sendGroup(@RequestBody Map<String, String> body, @AuthenticationPrincipal UserDetails userDetails) {

        String groupId = body.get("groupId");
        String text = body.get("body");
        String title = body.get("title");

        if (groupId == null || text == null) return ResponseEntity.badRequest().build();

        return ResponseEntity.status(HttpStatus.CREATED).body(messageService.sendGroup(userDetails.getUsername(), groupId, title, text));
    }

    // GET /api/messages/conversation/{conversationId}
    // Ao buscar a conversa, marca automaticamente as mensagens recebidas como DELIVERED
    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<List<Message>> getConversation(@PathVariable String conversationId, @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails != null ? userDetails.getUsername() : "";
        return ResponseEntity.ok(messageService.getConversation(conversationId, email));
    }

    // GET /api/messages/my-conversations
    @GetMapping("/my-conversations")
    public ResponseEntity<List<Message>> getMyConversations(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        return ResponseEntity.ok(messageService.getMyConversations(email, email));
    }

    // GET /api/messages/unread
    @GetMapping("/unread")
    public ResponseEntity<List<Message>> getUnread(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(messageService.getUnread(userDetails.getUsername()));
    }

    // GET /api/messages/unread/count
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> countUnread(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(Map.of("count", messageService.countUnread(userDetails.getUsername())));
    }

    // PATCH /api/messages/{id}/read → marca como READ
    @PatchMapping("/{id}/read")
    public ResponseEntity<Message> markAsRead(@PathVariable String id) {
        return ResponseEntity.ok(messageService.markAsRead(id));
    }

    // PATCH /api/messages/conversation/{conversationId}/read → marca conversa inteira como READ
    @PatchMapping("/conversation/{conversationId}/read")
    public ResponseEntity<Void> markConversationAsRead(@PathVariable String conversationId, @AuthenticationPrincipal UserDetails userDetails) {
        messageService.markConversationAsRead(conversationId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    // PUT /api/messages/{id} — editar mensagem (somente dono, até 5 minutos)
    @PutMapping("/{id}")
    public ResponseEntity<?> editMessage(@PathVariable String id, @RequestBody Map<String, String> body, @AuthenticationPrincipal UserDetails userDetails) {

        String newContent = body.get("content");
        if (newContent == null || newContent.isBlank())
            return ResponseEntity.badRequest().body(Map.of("message", "Conteúdo não pode ser vazio"));

        return messageRepository.findById(id).map(message -> {
            if (!message.getSenderId().equals(userDetails.getUsername()))
                return ResponseEntity.status(403).<Object>body(Map.of("message", "Sem permissão"));

            if (message.getCreatedAt() != null && Duration.between(message.getCreatedAt(), LocalDateTime.now()).toMinutes() > 5)
                return ResponseEntity.status(403).<Object>body(Map.of("message", "Prazo de edição expirado (5 minutos)"));

            message.setBody(newContent);
            message.setEdited(true);
            return ResponseEntity.ok((Object) messageRepository.save(message));
        }).orElse(ResponseEntity.notFound().<Object>build());
    }

    // DELETE /api/messages/{id} — excluir mensagem (somente dono, até 5 minutos)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMessage(@PathVariable String id, @AuthenticationPrincipal UserDetails userDetails) {

        return messageRepository.findById(id).map(message -> {
            if (!message.getSenderId().equals(userDetails.getUsername()))
                return ResponseEntity.status(403).<Object>body(Map.of("message", "Sem permissão"));

            if (message.getCreatedAt() != null && Duration.between(message.getCreatedAt(), LocalDateTime.now()).toMinutes() > 5)
                return ResponseEntity.status(403).<Object>body(Map.of("message", "Prazo de exclusão expirado (5 minutos)"));

            messageRepository.deleteById(id);
            return ResponseEntity.noContent().<Object>build();
        }).orElse(ResponseEntity.notFound().<Object>build());
    }
}