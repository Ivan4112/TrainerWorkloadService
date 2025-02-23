package org.edu.fpm.trainerworkloadservice.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.edu.fpm.trainerworkloadservice.dto.ExternalTrainingServiceDTO;
import org.edu.fpm.trainerworkloadservice.entity.TrainerWorkload;
import org.edu.fpm.trainerworkloadservice.entity.TrainerWorkloadSummary;
import org.edu.fpm.trainerworkloadservice.repository.TrainerWorkloadRepository;
import org.edu.fpm.trainerworkloadservice.util.ActionType;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TrainerWorkloadService {
    private final TrainerWorkloadRepository workloadRepository;

    @Transactional
    @CircuitBreaker(name = "trainerService", fallbackMethod = "fallbackUpdateWorkload")
    public void updateWorkload(ExternalTrainingServiceDTO externalTraining) {
        log.info("Received data from external service -> {}", externalTraining);
        if(externalTraining.actionType().equals(ActionType.ADD)) {
            TrainerWorkload workload = toTrainerWorkload(externalTraining);

            workloadRepository.save(workload);
            log.info("Training saved successful");
        }else {
            workloadRepository.deleteById(externalTraining.trainingId());
            log.info("Training deleted successful");
        }
    }


    @Transactional
    @CircuitBreaker(name = "trainerService", fallbackMethod = "fallbackGetMonthlyWorkloadSummary")
    public TrainerWorkloadSummary getMonthlyWorkloadSummary(String username) {
        log.info("Calculating number of working hours for trainer");
        List<TrainerWorkload> workloads = workloadRepository.findByUsername(username);

        if (workloads.isEmpty()) {
            log.info("No workload data found for trainer with username: {}", username);
            return new TrainerWorkloadSummary(username, "", "", false, new HashMap<>());
        }

        TrainerWorkload first = workloads.stream().findFirst().orElseThrow();
        TrainerWorkloadSummary summary = new TrainerWorkloadSummary(
                username, first.getFirstName(), first.getLastName(),
                first.isActive(), new HashMap<>()
        );

        workloads.forEach(workload -> {
            int year = workload.getDate().getYear();
            int month = workload.getDate().getMonthValue();
            summary.getMonthlySummary()
                    .computeIfAbsent(year, y -> new HashMap<>())
                    .merge(month, workload.getTrainingHours(), Integer::sum);
        });

        log.info("Successfully calculated monthly workload summary: {} for trainer: {}", summary, username);
        return summary;
    }

    public void saveAllTrainings(List<ExternalTrainingServiceDTO> trainingData) {
        List<TrainerWorkload> workloads = trainingData.stream()
                .map(this::toTrainerWorkload).collect(Collectors.toList());
        workloadRepository.saveAll(workloads);
    }

    private TrainerWorkload toTrainerWorkload(ExternalTrainingServiceDTO dto) {
        return new TrainerWorkload(
                dto.trainingId(),
                dto.trainerUsername(),
                dto.trainerFirstName(),
                dto.trainerLastName(),
                dto.isActive(),
                dto.trainingDate(),
                dto.trainingDuration()
        );
    }

    public void fallbackUpdateWorkload(ExternalTrainingServiceDTO externalTraining, Throwable throwable) {
        log.error("🔄 Circuit Breaker activated for updateWorkload. Returning fallback response. Error: {}", throwable.getMessage());

        log.warn("⚠️ Failed to update workload for {} {} ({}) on {}. Please try again later.",
                externalTraining.trainerFirstName(),
                externalTraining.trainerLastName(),
                externalTraining.trainerUsername(),
                externalTraining.trainingDate());
    }

    public TrainerWorkloadSummary fallbackGetMonthlyWorkloadSummary(String username, Throwable throwable) {
        log.error("🔄 Circuit Breaker activated for getMonthlyWorkloadSummary. Error: {}", throwable.getMessage());

        TrainerWorkloadSummary fallbackSummary = new TrainerWorkloadSummary(
                username, "N/A", "N/A", false, new HashMap<>()
        );

        log.warn("Returning fallback workload summary for username: {}", username);
        return fallbackSummary;
    }
}
