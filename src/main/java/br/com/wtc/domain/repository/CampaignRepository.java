package br.com.wtc.domain.repository;

import br.com.wtc.domain.model.Campaign;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CampaignRepository extends MongoRepository<Campaign, String> {

    // Campanhas por status — ex: todas as DRAFT ou SENT
    List<Campaign> findByStatus(Campaign.CampaignStatus status);

    // Campanhas criadas por um operador específico
    List<Campaign> findByCreatedByUserId(String userId);

    // Campanhas agendadas que ainda não foram enviadas
    // usado pelo SchedulerJob para disparar no horário certo
    List<Campaign> findByStatusAndScheduledAtBefore(
            Campaign.CampaignStatus status,
            LocalDateTime dateTime
    );

    // Campanhas que contêm uma tag de segmentação
    List<Campaign> findByTargetTagsContaining(String tag);
}