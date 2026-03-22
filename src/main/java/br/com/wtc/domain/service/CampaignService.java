package br.com.wtc.domain.service;

import br.com.wtc.domain.model.Campaign;
import br.com.wtc.domain.model.Message;
import br.com.wtc.domain.model.User;
import br.com.wtc.domain.repository.CampaignRepository;
import br.com.wtc.domain.repository.UserRepository;
import br.com.wtc.web.dto.CampaignRequest;
import br.com.wtc.web.dto.CampaignResponse;
import br.com.wtc.web.exception.BusinessException;
import br.com.wtc.web.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final UserRepository userRepository;
    private final MessageService messageService;

    public CampaignService(CampaignRepository campaignRepository,
                           UserRepository userRepository,
                           MessageService messageService) {
        this.campaignRepository = campaignRepository;
        this.userRepository     = userRepository;
        this.messageService     = messageService;
    }

    // ── Criar campanha (DRAFT) ────────────────────────────────────────
    public CampaignResponse create(CampaignRequest request, String operatorId) {

        if (request.title() == null || request.title().isBlank()) {
            throw new BusinessException("Título da campanha é obrigatório");
        }

        boolean hasTarget = (request.targetTags()      != null && !request.targetTags().isEmpty())
                || (request.targetClientIds() != null && !request.targetClientIds().isEmpty())
                || (request.targetGroupId()   != null && !request.targetGroupId().isBlank());

        if (!hasTarget) {
            throw new BusinessException("Define pelo menos um alvo: tags, clientIds ou groupId");
        }

        Campaign campaign = Campaign.builder()
                .title(request.title())
                .body(request.body())
                .url(request.url())
                .actions(request.actions())
                .status(Campaign.CampaignStatus.DRAFT)
                .targetTags(request.targetTags())
                .targetClientIds(request.targetClientIds())
                .targetGroupId(request.targetGroupId())
                .createdByUserId(operatorId)
                .scheduledAt(request.scheduledAt())
                .createdAt(LocalDateTime.now())
                .build();

        return CampaignResponse.from(campaignRepository.save(campaign));
    }

    // ── Listar todas ──────────────────────────────────────────────────
    public List<CampaignResponse> findAll() {
        return campaignRepository.findAll()
                .stream()
                .map(CampaignResponse::from)
                .collect(Collectors.toList());
    }

    // ── Buscar por ID ─────────────────────────────────────────────────
    public CampaignResponse findById(String id) {
        return CampaignResponse.from(campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campanha", id)));
    }

    // ── Listar por status ─────────────────────────────────────────────
    public List<CampaignResponse> findByStatus(Campaign.CampaignStatus status) {
        return campaignRepository.findByStatus(status)
                .stream()
                .map(CampaignResponse::from)
                .collect(Collectors.toList());
    }

    // ── Disparar campanha ─────────────────────────────────────────────
    public CampaignResponse dispatch(String id) {

        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campanha", id));

        if (campaign.getStatus() == Campaign.CampaignStatus.SENT) {
            throw new BusinessException("Campanha já foi enviada");
        }

        List<User> targets = resolveTargets(campaign);

        if (targets.isEmpty()) {
            throw new BusinessException("Nenhum cliente encontrado para os alvos definidos");
        }

        for (User user : targets) {
            messageService.sendCampaignMessage(campaign, user.getId());
        }

        campaign.setStatus(Campaign.CampaignStatus.SENT);
        campaign.setSentAt(LocalDateTime.now());

        return CampaignResponse.from(campaignRepository.save(campaign));
    }

    // ── Deletar (somente DRAFT) ───────────────────────────────────────
    public void delete(String id) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campanha", id));

        if (campaign.getStatus() == Campaign.CampaignStatus.SENT) {
            throw new BusinessException("Não é possível deletar campanha já enviada");
        }

        campaignRepository.deleteById(id);
    }

    // ── Resolve alvos usando UserRepository (coleção users) ──────────
    private List<User> resolveTargets(Campaign campaign) {
        List<User> targets = new ArrayList<>();

        // Por IDs específicos
        if (campaign.getTargetClientIds() != null && !campaign.getTargetClientIds().isEmpty()) {
            for (String userId : campaign.getTargetClientIds()) {
                userRepository.findById(userId).ifPresent(targets::add);
            }
        }

        // Por tags — users com role=CLIENT que contenham a tag
        if (campaign.getTargetTags() != null && !campaign.getTargetTags().isEmpty()) {
            for (String tag : campaign.getTargetTags()) {
                List<User> byTag = userRepository.findByRoleAndTagsContaining("CLIENT", tag);
                byTag.stream()
                        .filter(u -> !targets.contains(u))
                        .forEach(targets::add);
            }
        }

        // Por grupo — users com role=CLIENT e groupId correspondente
        if (campaign.getTargetGroupId() != null && !campaign.getTargetGroupId().isBlank()) {
            List<User> byGroup = userRepository.findByRoleAndGroupId("CLIENT", campaign.getTargetGroupId());
            byGroup.stream()
                    .filter(u -> !targets.contains(u))
                    .forEach(targets::add);
        }

        return targets;
    }
}