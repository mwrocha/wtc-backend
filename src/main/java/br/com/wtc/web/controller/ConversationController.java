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

    /**
     * GET /api/conversations/pending
     * Lista todas as conversas aguardando atendimento.
     */
    @GetMapping("/pending")
    public ResponseEntity<List<Map<String, Object>>> getPending() {
        return ResponseEntity.ok(conversationService.getPendingConversations());
    }

    /**
     * GET /api/conversations/pending/count
     * Retorna o número de conversas pendentes — usado para o badge.
     */
    @GetMapping("/pending/count")
    public ResponseEntity<Map<String, Long>> getPendingCount() {
        return ResponseEntity.ok(Map.of("count", conversationService.countPending()));
    }

    /**
     * POST /api/conversations/{conversationId}/assume
     * Operador assume o atendimento — remove da fila para todos.
     */
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

    /**
     * POST /api/conversations/{conversationId}/close
     * Operador encerra o atendimento — status vai para CLOSED.
     * Se o cliente enviar nova mensagem, reabre como OPEN automaticamente.
     */
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

    /**
     * GET /api/conversations/{conversationId}/status
     * Retorna o status atual de uma conversa.
     */
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