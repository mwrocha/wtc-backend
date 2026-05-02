package br.com.wtc.web.controller;

import br.com.wtc.domain.model.GroupChangeRequest;
import br.com.wtc.domain.model.User;
import br.com.wtc.domain.repository.GroupChangeRequestRepository;
import br.com.wtc.domain.repository.GroupRepository;
import br.com.wtc.domain.repository.UserRepository;
import br.com.wtc.infra.FirebasePushService;
import br.com.wtc.domain.service.AuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/group-change-requests")
public class GroupChangeRequestController {

    private final GroupChangeRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final AuditService auditService;
    private final FirebasePushService firebasePushService;

    public GroupChangeRequestController(GroupChangeRequestRepository requestRepository, UserRepository userRepository, GroupRepository groupRepository, AuditService auditService, FirebasePushService firebasePushService) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.auditService = auditService;
        this.firebasePushService = firebasePushService;
    }

    // Cliente — criar solicitação
    @PostMapping
    public ResponseEntity<?> createRequest(@RequestBody Map<String, String> body, @AuthenticationPrincipal UserDetails userDetails) {
        String newGroupId = body.get("newGroupId");
        String reason = body.getOrDefault("reason", "");

        if (newGroupId == null || newGroupId.isBlank())
            return ResponseEntity.badRequest().body(Map.of("message", "newGroupId é obrigatório"));

        User client = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (client == null) return ResponseEntity.notFound().build();

        var newGroup = groupRepository.findById(newGroupId).orElse(null);
        var currentGroup = client.getGroupId() != null ? groupRepository.findById(client.getGroupId()).orElse(null) : null;

        GroupChangeRequest request = new GroupChangeRequest();
        request.setClientId(client.getId());
        request.setClientEmail(client.getEmail());
        request.setClientName(client.getName());
        request.setCurrentGroupId(client.getGroupId());
        request.setCurrentGroupName(currentGroup != null ? currentGroup.getName() : "Nenhum");
        request.setRequestedGroupId(newGroupId);
        request.setRequestedGroupName(newGroup != null ? newGroup.getName() : newGroupId);
        request.setReason(reason);
        request.setStatus("PENDING");
        request.setCreatedAt(LocalDateTime.now());

        requestRepository.save(request);

        // Notifica todos os operadores via push FCM
        userRepository.findByRole("OPERATOR").forEach(operator -> {
            if (operator.getFcmToken() != null && !operator.getFcmToken().isBlank()) {
                firebasePushService.sendGroupChangeRequestPush(operator.getFcmToken(), request.getClientName(), request.getCurrentGroupName(), request.getRequestedGroupName());
            }
        });

        return ResponseEntity.ok(Map.of("message", "Solicitação enviada com sucesso"));
    }

    // Operador — listar todas
    @GetMapping
    public ResponseEntity<List<GroupChangeRequest>> listAll(@RequestParam(required = false) String status) {
        if (status != null) return ResponseEntity.ok(requestRepository.findByStatusOrderByCreatedAtDesc(status));
        return ResponseEntity.ok(requestRepository.findAllByOrderByCreatedAtDesc());
    }

    // Operador — aprovar
    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable String id, @AuthenticationPrincipal UserDetails userDetails) {
        return requestRepository.findById(id).map(req -> {
            userRepository.findById(req.getClientId()).ifPresent(client -> {
                client.setGroupId(req.getRequestedGroupId());
                userRepository.save(client);
            });

            req.setStatus("APPROVED");
            req.setReviewedBy(userDetails.getUsername());
            req.setReviewedAt(LocalDateTime.now());
            requestRepository.save(req);

            auditService.log("UPDATE", "GroupChangeRequest", id, userDetails.getUsername(), "Solicitação de " + req.getClientName() + " aprovada: " + req.getCurrentGroupName() + " → " + req.getRequestedGroupName());

            return ResponseEntity.ok(Map.of("message", "Solicitação aprovada"));
        }).orElse(ResponseEntity.notFound().build());
    }

    // Operador — rejeitar
    @PostMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable String id, @AuthenticationPrincipal UserDetails userDetails) {
        return requestRepository.findById(id).map(req -> {
            req.setStatus("REJECTED");
            req.setReviewedBy(userDetails.getUsername());
            req.setReviewedAt(LocalDateTime.now());
            requestRepository.save(req);

            auditService.log("UPDATE", "GroupChangeRequest", id, userDetails.getUsername(), "Solicitação de " + req.getClientName() + " rejeitada.");

            return ResponseEntity.ok(Map.of("message", "Solicitação rejeitada"));
        }).orElse(ResponseEntity.notFound().build());
    }
}