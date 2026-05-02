package br.com.wtc.domain.repository;

import br.com.wtc.domain.model.GroupChangeRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupChangeRequestRepository extends MongoRepository<GroupChangeRequest, String> {
    List<GroupChangeRequest> findByStatusOrderByCreatedAtDesc(String status);

    List<GroupChangeRequest> findByClientIdOrderByCreatedAtDesc(String clientId);

    List<GroupChangeRequest> findAllByOrderByCreatedAtDesc();
}