package br.com.wtc.web.controller;

import br.com.wtc.domain.model.Conversation;
import br.com.wtc.domain.service.ConversationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    // GET /api/conversations/pending
    @GetMapping("/pending")
    public ResponseEntity<List<Map<String, Object>>> getPending() {
        return ResponseEntity.ok(conversationService.getPendingConversations());
    }

    // GET /api/conversations/pending/count
    @GetMapping("/pending/count")
    public ResponseEntity<Map<String, Long>> getPendingCount() {
        return ResponseEntity.ok(Map.of("count", conversationService.countPending()));
    }

    // GET /api/conversations/my-active
    @GetMapping("/my-active")
    public ResponseEntity<List<Map<String, Object>>> getMyActive(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                conversationService.getMyActiveConversations(userDetails.getUsername()));
    }

    /**
     * GET /api/conversations/my-stats
     * Retorna métricas de atendimento do operador logado:
     * { active, today, thisMonth }
     */
    @GetMapping("/my-stats")
    public ResponseEntity<Map<String, Long>> getMyStats(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                conversationService.getMyStats(userDetails.getUsername()));
    }

    // POST /api/conversations/{conversationId}/assume
    @PostMapping("/{conversationId}/assume")
    public ResponseEntity<?> assume(
            @PathVariable String conversationId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Conversation conv = conversationService.assumeConversation(
                    conversationId, userDetails.getUsername());
            return ResponseEntity.ok(Map.of(
                    "message",          "Atendimento assumido com sucesso.",
                    "conversationId",   conv.getConversationId(),
                    "assignedOperator", conv.getAssignedOperatorEmail(),
                    "status",           conv.getStatus().name()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // POST /api/conversations/{conversationId}/close
    @PostMapping("/{conversationId}/close")
    public ResponseEntity<?> close(
            @PathVariable String conversationId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Conversation conv = conversationService.closeConversation(
                    conversationId, userDetails.getUsername());
            return ResponseEntity.ok(Map.of(
                    "message",        "Atendimento encerrado com sucesso.",
                    "conversationId", conv.getConversationId(),
                    "status",         conv.getStatus().name()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // GET /api/conversations/{conversationId}/status
    @GetMapping("/{conversationId}/status")
    public ResponseEntity<?> getStatus(@PathVariable String conversationId) {
        return conversationService.getByConversationId(conversationId)
                .map(conv -> ResponseEntity.ok(Map.of(
                        "conversationId",        conv.getConversationId(),
                        "status",                conv.getStatus().name(),
                        "assignedOperatorEmail", conv.getAssignedOperatorEmail() != null
                                ? conv.getAssignedOperatorEmail() : "",
                        "assumedAt",             conv.getAssumedAt() != null
                                ? conv.getAssumedAt().toString() : ""
                )))
                .orElse(ResponseEntity.ok(Map.of(
                        "conversationId",        conversationId,
                        "status",                "OPEN",
                        "assignedOperatorEmail", ""
                )));
    }
}