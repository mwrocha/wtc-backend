package br.com.wtc.web.controller;

import br.com.wtc.domain.model.Campaign;
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
@RequestMapping("/api/campaigns")
public class CampaignController {

    private final CampaignService campaignService;

    public CampaignController(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    // POST /api/campaigns
    // Cria a campanha como DRAFT
    @PostMapping
    public ResponseEntity<CampaignResponse> create(
            @RequestBody @Valid CampaignRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(campaignService.create(request, userDetails.getUsername()));
    }

    // GET /api/campaigns
    // GET /api/campaigns?status=DRAFT
    // GET /api/campaigns?status=SENT
    @GetMapping
    public ResponseEntity<List<CampaignResponse>> list(
            @RequestParam(required = false) String status) {

        if (status != null) {
            Campaign.CampaignStatus s = Campaign.CampaignStatus.valueOf(status.toUpperCase());
            return ResponseEntity.ok(campaignService.findByStatus(s));
        }

        return ResponseEntity.ok(campaignService.findAll());
    }

    // GET /api/campaigns/{id}
    @GetMapping("/{id}")
    public ResponseEntity<CampaignResponse> findById(@PathVariable String id) {
        return ResponseEntity.ok(campaignService.findById(id));
    }

    // POST /api/campaigns/{id}/dispatch
    // Dispara a campanha imediatamente para todos os alvos
    @PostMapping("/{id}/dispatch")
    public ResponseEntity<CampaignResponse> dispatch(@PathVariable String id) {
        return ResponseEntity.ok(campaignService.dispatch(id));
    }

    // DELETE /api/campaigns/{id}
    // Só permite deletar campanhas com status DRAFT
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        campaignService.delete(id);
        return ResponseEntity.noContent().build();
    }
}