package br.com.wtc.web.controller;

import br.com.wtc.domain.model.AttendanceRating;
import br.com.wtc.domain.service.AttendanceRatingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ratings")
public class AttendanceRatingController {

    private final AttendanceRatingService ratingService;

    public AttendanceRatingController(AttendanceRatingService ratingService) {
        this.ratingService = ratingService;
    }

    // POST /api/ratings/{sessionId}
    @PostMapping("/{sessionId}")
    public ResponseEntity<?> submitRating(@PathVariable String sessionId, @RequestBody Map<String, Object> body, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            int stars = ((Number) body.get("stars")).intValue();
            String comment = (String) body.getOrDefault("comment", null);

            AttendanceRating rating = ratingService.submitRating(sessionId, userDetails.getUsername(), stars, comment);

            return ResponseEntity.ok(Map.of("message", "Avaliação registrada com sucesso!", "sessionId", rating.getSessionId(), "stars", rating.getStars()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // GET /api/ratings/my-stats — operador consulta métricas
    @GetMapping("/my-stats")
    public ResponseEntity<Map<String, Object>> getMyStats(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ratingService.getMyRatingStats(userDetails.getUsername()));
    }

    // GET /api/ratings/pending — cliente verifica avaliação pendente
    @GetMapping("/pending")
    public ResponseEntity<?> getPending(@AuthenticationPrincipal UserDetails userDetails) {
        Map<String, String> pending = ratingService.getPendingRatingForClient(userDetails.getUsername());

        if (pending == null) {
            return ResponseEntity.ok(Map.of("hasPending", false));
        }

        return ResponseEntity.ok(Map.of("hasPending", true, "sessionId", pending.get("sessionId"), "operatorEmail", pending.get("operatorEmail"), "closedAt", pending.get("closedAt")));
    }

    /**
     * GET /api/ratings/my-history
     * Cliente consulta histórico de atendimentos encerrados.
     * Retorna: sessionId, operatorName, operatorEmail, assumedAt, closedAt, stars, comment
     */
    @GetMapping("/my-history")
    public ResponseEntity<List<Map<String, Object>>> getMyHistory(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ratingService.getMyHistory(userDetails.getUsername()));
    }
}