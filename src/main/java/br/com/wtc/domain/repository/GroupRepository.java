package br.com.wtc.domain.repository;

import br.com.wtc.domain.model.Group;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends MongoRepository<Group, String> {

    // Busca todos os grupos de uma divisão
    List<Group> findByDivisionId(String divisionId);

    // Busca grupos que contêm um cliente específico
    List<Group> findByClientIdsContaining(String clientId);

    // Busca por nome parcial (barra de pesquisa)
    List<Group> findByNameContainingIgnoreCase(String name);
}