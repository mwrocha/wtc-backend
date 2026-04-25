package br.com.wtc.domain.service;

import br.com.wtc.domain.model.AttendanceRating;
import br.com.wtc.domain.model.AttendanceSession;
import br.com.wtc.domain.repository.AttendanceRatingRepository;
import br.com.wtc.domain.repository.AttendanceSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AttendanceRatingService {

    private static final Logger log = LoggerFactory.getLogger(AttendanceRatingService.class);

    private final AttendanceRatingRepository  ratingRepository;
    private final AttendanceSessionRepository sessionRepository;

    public AttendanceRatingService(AttendanceRatingRepository ratingRepository,
                                   AttendanceSessionRepository sessionRepository) {
        this.ratingRepository  = ratingRepository;
        this.sessionRepository = sessionRepository;
    }

    /**
     * Submete a avaliação do cliente para uma sessão encerrada.
     * Valida: sessão existe, está CLOSED, cliente correto, não avaliada ainda.
     */
    public AttendanceRating submitRating(String sessionId, String clientEmail,
                                         int stars, String comment) {
        // Valida sessão
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Sessão não encontrada: " + sessionId));

        if (session.getStatus() != AttendanceSession.SessionStatus.CLOSED) {
            throw new RuntimeException("Só é possível avaliar atendimentos encerrados.");
        }

        if (!session.getClientEmail().equalsIgnoreCase(clientEmail)) {
            throw new RuntimeException("Você não tem permissão para avaliar esta sessão.");
        }

        if (ratingRepository.existsBySessionIdAndClientEmail(sessionId, clientEmail)) {
            throw new RuntimeException("Você já avaliou este atendimento.");
        }

        if (stars < 1 || stars > 5) {
            throw new RuntimeException("A avaliação deve ser entre 1 e 5 estrelas.");
        }

        AttendanceRating rating = new AttendanceRating();
        rating.setSessionId(sessionId);
        rating.setConversationId(session.getConversationId());
        rating.setClientEmail(clientEmail);
        rating.setOperatorEmail(session.getOperatorEmail());
        rating.setStars(stars);
        rating.setComment(comment != null ? comment.trim() : null);
        rating.setCreatedAt(LocalDateTime.now());

        AttendanceRating saved = ratingRepository.save(rating);
        log.info("Avaliação registrada: sessão={} stars={} cliente={}", sessionId, stars, clientEmail);
        return saved;
    }

    /**
     * Estatísticas de avaliação do operador logado.
     * Retorna: média, total de avaliações, distribuição por estrelas.
     */
    public Map<String, Object> getMyRatingStats(String operatorEmail) {
        List<AttendanceRating> ratings = ratingRepository.findByOperatorEmail(operatorEmail);

        Map<String, Object> stats = new HashMap<>();

        if (ratings.isEmpty()) {
            stats.put("average",      0.0);
            stats.put("total",        0);
            stats.put("distribution", Map.of(1, 0, 2, 0, 3, 0, 4, 0, 5, 0));
            return stats;
        }

        double average = ratings.stream()
                .mapToInt(AttendanceRating::getStars)
                .average()
                .orElse(0.0);

        // Arredonda para 1 casa decimal
        average = Math.round(average * 10.0) / 10.0;

        Map<Integer, Long> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            final int star = i;
            distribution.put(star, ratings.stream()
                    .filter(r -> r.getStars() == star)
                    .count());
        }

        stats.put("average",      average);
        stats.put("total",        ratings.size());
        stats.put("distribution", distribution);
        return stats;
    }

    /**
     * Busca a sessão mais recente encerrada do cliente para exibir o dialog de avaliação.
     * Retorna null se não houver sessão pendente de avaliação.
     */
    public Map<String, String> getPendingRatingForClient(String clientEmail) {
        List<AttendanceSession> closed = sessionRepository
                .findByClientEmailAndStatus(clientEmail, AttendanceSession.SessionStatus.CLOSED);

        return closed.stream()
                .filter(s -> s.getClosedAt() != null)
                .filter(s -> !ratingRepository.existsBySessionIdAndClientEmail(
                        s.getId(), clientEmail))
                .max((a, b) -> a.getClosedAt().compareTo(b.getClosedAt()))
                .map(session -> {
                    Map<String, String> result = new HashMap<>();
                    result.put("sessionId",     session.getId());
                    result.put("operatorEmail", session.getOperatorEmail());
                    result.put("closedAt",      session.getClosedAt().toString());
                    return result;
                })
                .orElse(null);
    }
}