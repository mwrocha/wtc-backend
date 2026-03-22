package br.com.wtc.domain.repository;

import br.com.wtc.domain.model.Division;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DivisionRepository extends MongoRepository<Division, String> {

    List<Division> findByNameContainingIgnoreCase(String name);
}