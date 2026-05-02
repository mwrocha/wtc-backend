package br.com.wtc.web.controller;

import br.com.wtc.domain.model.AuditLog;
import br.com.wtc.domain.service.AuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditLogController {

    private final AuditService auditService;

    public AuditLogController(AuditService auditService) {
        this.auditService = auditService;
    }

    // Todos os logs — apenas operador
    @GetMapping
    public ResponseEntity<List<AuditLog>> getAllLogs() {
        return ResponseEntity.ok(auditService.findAll());
    }

    // Logs do próprio operador logado
    @GetMapping("/me")
    public ResponseEntity<List<AuditLog>> getMyLogs(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(auditService.findByOperator(userDetails.getUsername()));
    }

    // Logs por entidade (Campaign, Group, etc.)
    @GetMapping("/entity/{entity}")
    public ResponseEntity<List<AuditLog>> getByEntity(@PathVariable String entity) {
        return ResponseEntity.ok(auditService.findByEntity(entity));
    }

    // Logs de um item específico
    @GetMapping("/entity/{entity}/{entityId}")
    public ResponseEntity<List<AuditLog>> getByEntityAndId(@PathVariable String entity, @PathVariable String entityId) {
        return ResponseEntity.ok(auditService.findByEntityAndId(entity, entityId));
    }
}