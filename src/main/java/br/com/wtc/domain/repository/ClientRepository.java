package br.com.wtc.domain.repository;

import br.com.wtc.domain.model.Client;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends MongoRepository<Client, String> {

    // Busca por e-mail — usado para evitar duplicatas
    Optional<Client> findByEmail(String email);

    // Filtro por status: "ACTIVE", "INACTIVE", "LEAD"
    List<Client> findByStatus(String status);

    // Filtro por score mínimo — segmentação de campanhas
    List<Client> findByScoreGreaterThanEqual(int minScore);

    // Busca por tag — ex: findByTagsContaining("vip")
    List<Client> findByTagsContaining(String tag);

    // Busca por grupo
    List<Client> findByGroupId(String groupId);

    // Busca por divisão
    List<Client> findByDivisionId(String divisionId);

    // Busca por nome (parcial, case-insensitive) — barra de pesquisa do CRM
    List<Client> findByNameContainingIgnoreCase(String name);
}