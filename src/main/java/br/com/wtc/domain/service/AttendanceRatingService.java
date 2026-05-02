package br.com.wtc.domain.service;

import br.com.wtc.domain.model.AttendanceRating;
import br.com.wtc.domain.model.AttendanceSession;
import br.com.wtc.domain.repository.AttendanceRatingRepository;
import br.com.wtc.domain.repository.AttendanceSessionRepository;
import br.com.wtc.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AttendanceRatingService {

    private static final Logger log = LoggerFactory.getLogger(AttendanceRatingService.class);

    private final AttendanceRatingRepository ratingRepository;
    private final AttendanceSessionRepository sessionRepository;
    private final UserRepository userRepository;

    public AttendanceRatingService(AttendanceRatingRepository ratingRepository, AttendanceSessionRepository sessionRepository, UserRepository userRepository) {
        this.ratingRepository = ratingRepository;
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    public AttendanceRating submitRating(String sessionId, String clientEmail, int stars, String comment) {
        AttendanceSession session = sessionRepository.findById(sessionId).orElseThrow(() -> new RuntimeException("Sessão não encontrada: " + sessionId));

        if (session.getStatus() != AttendanceSession.SessionStatus.CLOSED)
            throw new RuntimeException("Só é possível avaliar atendimentos encerrados.");

        if (!session.getClientEmail().equalsIgnoreCase(clientEmail))
            throw new RuntimeException("Você não tem permissão para avaliar esta sessão.");

        if (ratingRepository.existsBySessionIdAndClientEmail(sessionId, clientEmail))
            throw new RuntimeException("Você já avaliou este atendimento.");

        if (stars < 1 || stars > 5) throw new RuntimeException("A avaliação deve ser entre 1 e 5 estrelas.");

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

    public Map<String, Object> getMyRatingStats(String operatorEmail) {
        List<AttendanceRating> ratings = ratingRepository.findByOperatorEmail(operatorEmail);
        Map<String, Object> stats = new HashMap<>();

        if (ratings.isEmpty()) {
            stats.put("average", 0.0);
            stats.put("total", 0);
            stats.put("distribution", Map.of(1, 0, 2, 0, 3, 0, 4, 0, 5, 0));
            return stats;
        }

        double average = Math.round(ratings.stream().mapToInt(AttendanceRating::getStars).average().orElse(0.0) * 10.0) / 10.0;

        Map<Integer, Long> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            final int star = i;
            distribution.put(star, ratings.stream().filter(r -> r.getStars() == star).count());
        }

        stats.put("average", average);
        stats.put("total", ratings.size());
        stats.put("distribution", distribution);
        return stats;
    }

    public Map<String, String> getPendingRatingForClient(String clientEmail) {
        List<AttendanceSession> closed = sessionRepository.findByClientEmailAndStatus(clientEmail, AttendanceSession.SessionStatus.CLOSED);

        return closed.stream().filter(s -> s.getClosedAt() != null).filter(s -> !ratingRepository.existsBySessionIdAndClientEmail(s.getId(), clientEmail)).max((a, b) -> a.getClosedAt().compareTo(b.getClosedAt())).map(session -> {
            Map<String, String> result = new HashMap<>();
            result.put("sessionId", session.getId());
            result.put("operatorEmail", session.getOperatorEmail());
            result.put("closedAt", session.getClosedAt().toString());
            return result;
        }).orElse(null);
    }

    /**
     * Histórico de atendimentos encerrados do cliente.
     * Inclui nome do operador e avaliação dada (se houver).
     */
    public List<Map<String, Object>> getMyHistory(String clientEmail) {
        List<AttendanceSession> sessions = sessionRepository.findByClientEmailAndStatus(clientEmail, AttendanceSession.SessionStatus.CLOSED).stream().filter(s -> s.getClosedAt() != null).sorted((a, b) -> b.getClosedAt().compareTo(a.getClosedAt())).toList();

        List<Map<String, Object>> result = new ArrayList<>();

        for (AttendanceSession session : sessions) {
            String operatorName = userRepository.findByEmail(session.getOperatorEmail()).map(u -> u.getName()).orElse(session.getOperatorEmail());

            Map<String, Object> item = new HashMap<>();
            item.put("sessionId", session.getId());
            item.put("operatorEmail", session.getOperatorEmail());
            item.put("operatorName", operatorName);
            item.put("assumedAt", session.getAssumedAt() != null ? session.getAssumedAt().toString() : "");
            item.put("closedAt", session.getClosedAt().toString());

            // Avaliação dada (se houver)
            ratingRepository.findBySessionId(session.getId()).ifPresent(rating -> {
                item.put("stars", rating.getStars());
                item.put("comment", rating.getComment() != null ? rating.getComment() : "");
            });

            result.add(item);
        }

        return result;
    }
}