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

    // Busca todos os clientes
    List<User> findByRole(String role);

    // Busca clientes por grupo
    List<User> findByRoleAndGroupId(String role, String groupId);

    // Busca clientes por divisão
    List<User> findByRoleAndDivisionId(String role, String divisionId);

    // Busca clientes por tag
    List<User> findByRoleAndTagsContaining(String role, String tag);

    // Busca clientes por nome parcial
    List<User> findByRoleAndNameContainingIgnoreCase(String role, String name);
}