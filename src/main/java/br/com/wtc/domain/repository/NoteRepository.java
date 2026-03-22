package br.com.wtc.domain.repository;

import br.com.wtc.domain.model.Note;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends MongoRepository<Note, String> {

    // Todas as anotações de um cliente, ordenadas da mais recente
    List<Note> findByClientIdOrderByCreatedAtDesc(String clientId);

    // Anotações feitas por um operador específico
    List<Note> findByOperatorId(String operatorId);

    // Remove todas as notas de um cliente (ao deletar o cliente)
    void deleteByClientId(String clientId);
}