package org.edu.fpm.trainerworkloadservice.repository;

import org.edu.fpm.trainerworkloadservice.entity.TrainerWorkloadSummary;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrainerWorkloadRepository extends MongoRepository<TrainerWorkloadSummary, String> {
    Optional<TrainerWorkloadSummary> findByUsername(String username);
}
