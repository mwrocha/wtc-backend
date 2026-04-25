package br.com.wtc.domain.repository;

import br.com.wtc.domain.model.AttendanceRating;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRatingRepository extends MongoRepository<AttendanceRating, String> {

    // Busca avaliação de uma sessão específica
    Optional<AttendanceRating> findBySessionId(String sessionId);

    // Todas as avaliações de um operador
    List<AttendanceRating> findByOperatorEmail(String operatorEmail);

    // Verifica se o cliente já avaliou essa sessão
    boolean existsBySessionIdAndClientEmail(String sessionId, String clientEmail);
}