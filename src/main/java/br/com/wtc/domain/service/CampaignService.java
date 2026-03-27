package br.com.wtc.domain.service;

import br.com.wtc.domain.model.Campaign;
import br.com.wtc.domain.model.Group;
import br.com.wtc.domain.model.User;
import br.com.wtc.domain.repository.CampaignRepository;
import br.com.wtc.domain.repository.GroupRepository;
import br.com.wtc.domain.repository.UserRepository;
import br.com.wtc.domain.service.AuditService;
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
    private final UserRepository     userRepository;
    private final GroupRepository    groupRepository;
    private final MessageService     messageService;
    private final AuditService       auditService;

    public CampaignService(CampaignRepository campaignRepository,
                           UserRepository userRepository,
                           GroupRepository groupRepository,
                           MessageService messageService,
                           AuditService auditService) {
        this.campaignRepository = campaignRepository;
        this.userRepository     = userRepository;
        this.groupRepository    = groupRepository;
        this.messageService     = messageService;
        this.auditService       = auditService;
    }

    // ── Criar campanha (DRAFT) ────────────────────────────────────────
    public CampaignResponse create(CampaignRequest request, String operatorId) {
        if (request.title() == null || request.title().isBlank())
            throw new BusinessException("Título da campanha é obrigatório");

        boolean hasTarget = (request.targetTags()       != null && !request.targetTags().isEmpty())
                || (request.targetClientIds()  != null && !request.targetClientIds().isEmpty())
                || (request.targetGroupId()    != null && !request.targetGroupId().isBlank())
                || (request.targetDivisionId() != null && !request.targetDivisionId().isBlank());

        if (!hasTarget)
            throw new BusinessException("Defina pelo menos um alvo: tags, clientIds, groupId ou divisionId");

        Campaign campaign = Campaign.builder()
                .title(request.title())
                .body(request.body())
                .url(request.url())
                .actions(request.actions())
                .status(Campaign.CampaignStatus.DRAFT)
                .targetTags(request.targetTags())
                .targetClientIds(request.targetClientIds())
                .targetGroupId(request.targetGroupId())
                .targetDivisionId(request.targetDivisionId())
                .createdByUserId(operatorId)
                .scheduledAt(request.scheduledAt())
                .createdAt(LocalDateTime.now())
                .build();

        Campaign saved = campaignRepository.save(campaign);

        auditService.log("CREATE", "Campaign", saved.getId(), operatorId,
                "Campanha criada: \"" + saved.getTitle() + "\"");

        return CampaignResponse.from(saved);
    }

    // ── Editar campanha ───────────────────────────────────────────────
    public CampaignResponse update(String id, CampaignRequest request, String operatorId) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campanha", id));

        // Guarda estado anterior para auditoria
        String beforeTitle = campaign.getTitle();

        if (request.title() != null && !request.title().isBlank())
            campaign.setTitle(request.title());
        if (request.body() != null)
            campaign.setBody(request.body());
        if (request.url() != null)
            campaign.setUrl(request.url());
        if (request.actions() != null)
            campaign.setActions(request.actions());
        if (request.targetGroupId() != null)
            campaign.setTargetGroupId(request.targetGroupId());
        if (request.targetDivisionId() != null)
            campaign.setTargetDivisionId(request.targetDivisionId());
        if (request.targetTags() != null)
            campaign.setTargetTags(request.targetTags());
        if (request.targetClientIds() != null)
            campaign.setTargetClientIds(request.targetClientIds());

        Campaign updated = campaignRepository.save(campaign);

        // Propaga a edição para mensagens já enviadas e notifica clientes
        int affected = messageService.propagateCampaignUpdate(updated);

        auditService.log("UPDATE", "Campaign", id, operatorId,
                "Campanha atualizada: \"" + beforeTitle + "\" → \"" + updated.getTitle() + "\""
                        + (affected > 0
                        ? " | " + affected + " mensagem(ns) atualizada(s) e clientes notificados"
                        : " | Sem mensagens anteriores para propagar"));

        return CampaignResponse.from(updated);
    }

    // ── Listar todas ──────────────────────────────────────────────────
    public List<CampaignResponse> findAll() {
        return campaignRepository.findAll()
                .stream().map(CampaignResponse::from).collect(Collectors.toList());
    }

    // ── Buscar por ID ─────────────────────────────────────────────────
    public CampaignResponse findById(String id) {
        return CampaignResponse.from(campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campanha", id)));
    }

    // ── Listar por status ─────────────────────────────────────────────
    public List<CampaignResponse> findByStatus(Campaign.CampaignStatus status) {
        return campaignRepository.findByStatus(status)
                .stream().map(CampaignResponse::from).collect(Collectors.toList());
    }

    // ── Disparar campanha ─────────────────────────────────────────────
    public CampaignResponse dispatch(String id, String operatorId) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campanha", id));

        if (campaign.getStatus() == Campaign.CampaignStatus.SENT)
            throw new BusinessException("Campanha já foi enviada");

        List<User> targets = resolveTargets(campaign);
        if (targets.isEmpty())
            throw new BusinessException("Nenhum cliente encontrado para os alvos definidos");

        for (User user : targets)
            messageService.sendCampaignMessage(campaign, user.getId());

        campaign.setStatus(Campaign.CampaignStatus.SENT);
        campaign.setSentAt(LocalDateTime.now());
        Campaign saved = campaignRepository.save(campaign);

        auditService.log("DISPATCH", "Campaign", id, operatorId,
                "Campanha disparada: \"" + campaign.getTitle() + "\" para " + targets.size() + " cliente(s)");

        return CampaignResponse.from(saved);
    }

    // ── Deletar (somente DRAFT) ───────────────────────────────────────
    public void delete(String id, String operatorId) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campanha", id));
        if (campaign.getStatus() == Campaign.CampaignStatus.SENT)
            throw new BusinessException("Não é possível deletar campanha já enviada");

        auditService.log("DELETE", "Campaign", id, operatorId,
                "Campanha deletada: \"" + campaign.getTitle() + "\"");

        campaignRepository.deleteById(id);
    }

    // ── Resolve alvos ─────────────────────────────────────────────────
    private List<User> resolveTargets(Campaign campaign) {
        List<User> targets = new ArrayList<>();

        if (campaign.getTargetClientIds() != null && !campaign.getTargetClientIds().isEmpty())
            for (String userId : campaign.getTargetClientIds())
                userRepository.findById(userId).ifPresent(u -> addIfAbsent(targets, u));

        if (campaign.getTargetTags() != null && !campaign.getTargetTags().isEmpty())
            for (String tag : campaign.getTargetTags())
                userRepository.findByRoleAndTagsContaining("CLIENT", tag)
                        .forEach(u -> addIfAbsent(targets, u));

        if (campaign.getTargetGroupId() != null && !campaign.getTargetGroupId().isBlank())
            userRepository.findByRoleAndGroupId("CLIENT", campaign.getTargetGroupId())
                    .forEach(u -> addIfAbsent(targets, u));

        if (campaign.getTargetDivisionId() != null && !campaign.getTargetDivisionId().isBlank()) {
            List<Group> groupsInDivision = groupRepository.findByDivisionId(campaign.getTargetDivisionId());
            for (Group group : groupsInDivision)
                userRepository.findByRoleAndGroupId("CLIENT", group.getId())
                        .forEach(u -> addIfAbsent(targets, u));
        }

        return targets;
    }

    private void addIfAbsent(List<User> list, User user) {
        if (list.stream().noneMatch(u -> u.getId().equals(user.getId())))
            list.add(user);
    }
}