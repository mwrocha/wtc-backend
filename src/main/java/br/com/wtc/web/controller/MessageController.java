package br.com.wtc.web.controller;

import br.com.wtc.domain.model.Message;
import br.com.wtc.domain.repository.UserRepository;
import br.com.wtc.domain.service.MessageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;
    private final UserRepository userRepository;

    public MessageController(MessageService messageService,
                             UserRepository userRepository) {
        this.messageService = messageService;
        this.userRepository = userRepository;
    }

    // POST /api/messages/direct — mensagem 1:1
    @PostMapping("/direct")
    public ResponseEntity<Message> sendDirect(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {

        String recipientId = body.get("recipientId");
        String title = body.get("title");
        String text = body.get("body");

        if (recipientId == null || text == null) {
            return ResponseEntity.badRequest().build();
        }

        Message message = messageService.sendDirect(
                userDetails.getUsername(), recipientId, title, text
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    // POST /api/messages/group — mensagem de grupo
    // Body: { "groupId": "xxx", "body": "Olá grupo!" }
    @PostMapping("/group")
    public ResponseEntity<Message> sendGroup(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {

        String groupId = body.get("groupId");
        String text = body.get("body");
        String title = body.get("title");

        if (groupId == null || text == null) {
            return ResponseEntity.badRequest().build();
        }

        Message message = messageService.sendGroup(
                userDetails.getUsername(), groupId, title, text
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    // GET /api/messages/conversation/{conversationId}
    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<List<Message>> getConversation(
            @PathVariable String conversationId) {
        return ResponseEntity.ok(messageService.getConversation(conversationId));
    }

    // GET /api/messages/my-conversations
    @GetMapping("/my-conversations")
    public ResponseEntity<List<Message>> getMyConversations(
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        return userRepository.findByEmail(email)
                .map(user -> ResponseEntity.ok(
                        messageService.getMyConversations(email, user.getId())
                ))
                .orElse(ResponseEntity.ok(List.of()));
    }

    // GET /api/messages/unread
    @GetMapping("/unread")
    public ResponseEntity<List<Message>> getUnread(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                messageService.getUnread(userDetails.getUsername())
        );
    }

    // GET /api/messages/unread/count
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> countUnread(
            @AuthenticationPrincipal UserDetails userDetails) {
        long count = messageService.countUnread(userDetails.getUsername());
        return ResponseEntity.ok(Map.of("count", count));
    }

    // PATCH /api/messages/{id}/read
    @PatchMapping("/{id}/read")
    public ResponseEntity<Message> markAsRead(@PathVariable String id) {
        return ResponseEntity.ok(messageService.markAsRead(id));
    }

    // PATCH /api/messages/conversation/{conversationId}/read
    @PatchMapping("/conversation/{conversationId}/read")
    public ResponseEntity<Void> markConversationAsRead(
            @PathVariable String conversationId,
            @AuthenticationPrincipal UserDetails userDetails) {
        messageService.markConversationAsRead(
                conversationId, userDetails.getUsername()
        );
        return ResponseEntity.noContent().build();
    }
}