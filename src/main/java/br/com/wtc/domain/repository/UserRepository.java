package br.com.wtc.domain.repository;

import br.com.wtc.domain.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRole(String role);

    List<User> findByRoleAndGroupId(String role, String groupId);

    List<User> findByRoleAndDivisionId(String role, String divisionId);

    List<User> findByRoleAndTagsContaining(String role, String tag);

    List<User> findByRoleAndNameContainingIgnoreCase(String role, String name);

    List<User> findAllByFcmToken(String fcmToken);

    // ← necessário para buscar membros do grupo ao enviar push
    List<User> findByGroupId(String groupId);
}