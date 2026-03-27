package br.com.wtc.web.controller;

import br.com.wtc.domain.model.Campaign;
import br.com.wtc.domain.model.Message;
import br.com.wtc.domain.repository.MessageRepository;
import br.com.wtc.domain.repository.UserRepository;
import br.com.wtc.domain.service.CampaignService;
import br.com.wtc.web.dto.CampaignRequest;
import br.com.wtc.web.dto.CampaignResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CampaignController {

    private final CampaignService   campaignService;
    private final MessageRepository messageRepository;
    private final UserRepository    userRepository;

    public CampaignController(CampaignService campaignService,
                              MessageRepository messageRepository,
                              UserRepository userRepository) {
        this.campaignService   = campaignService;
        this.messageRepository = messageRepository;
        this.userRepository    = userRepository;
    }

    // ── Operador ──────────────────────────────────────────────────────

    @PostMapping("/campaigns")
    public ResponseEntity<CampaignResponse> create(
            @RequestBody @Valid CampaignRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(campaignService.create(request, userDetails.getUsername()));
    }

    @GetMapping("/campaigns")
    public ResponseEntity<List<CampaignResponse>> list(
            @RequestParam(required = false) String status) {
        if (status != null) {
            Campaign.CampaignStatus s = Campaign.CampaignStatus.valueOf(status.toUpperCase());
            return ResponseEntity.ok(campaignService.findByStatus(s));
        }
        return ResponseEntity.ok(campaignService.findAll());
    }

    @GetMapping("/campaigns/{id}")
    public ResponseEntity<CampaignResponse> findById(@PathVariable String id) {
        return ResponseEntity.ok(campaignService.findById(id));
    }

    @PutMapping("/campaigns/{id}")
    public ResponseEntity<CampaignResponse> update(
            @PathVariable String id,
            @RequestBody @Valid CampaignRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(campaignService.update(id, request, userDetails.getUsername()));
    }

    @PostMapping("/campaigns/{id}/dispatch")
    public ResponseEntity<CampaignResponse> dispatch(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(campaignService.dispatch(id, userDetails.getUsername()));
    }

    @DeleteMapping("/campaigns/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        campaignService.delete(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    // ── Cliente — campanhas recebidas ─────────────────────────────────

    @GetMapping("/campaigns-received")
    public ResponseEntity<List<Message>> getMyCampaigns(
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();

        List<Message> campaigns = messageRepository
                .findByRecipientIdAndTypeOrderByCreatedAtDesc(
                        email, Message.MessageType.CAMPAIGN);

        if (campaigns.isEmpty()) {
            var userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                campaigns = messageRepository
                        .findByRecipientIdAndTypeOrderByCreatedAtDesc(
                                userOpt.get().getId(), Message.MessageType.CAMPAIGN);
            }
        }
        return ResponseEntity.ok(campaigns);
    }
}