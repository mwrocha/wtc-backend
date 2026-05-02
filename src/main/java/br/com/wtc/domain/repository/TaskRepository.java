package br.com.wtc.domain.repository;

import br.com.wtc.domain.model.Task;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TaskRepository extends MongoRepository<Task, String> {
    List<Task> findByOperatorIdOrderByCreatedAtDesc(String operatorId);

    List<Task> findByClientIdOrderByCreatedAtDesc(String clientId);

    List<Task> findByOperatorIdAndStatusOrderByCreatedAtDesc(String operatorId, String status);
}