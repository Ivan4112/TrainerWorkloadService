package org.edu.fpm.trainerworkloadservice.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.edu.fpm.trainerworkloadservice.dto.ExternalTrainingServiceDTO;
import org.edu.fpm.trainerworkloadservice.entity.TrainerWorkload;
import org.edu.fpm.trainerworkloadservice.entity.TrainerWorkloadSummary;
import org.edu.fpm.trainerworkloadservice.repository.TrainerWorkloadRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class TrainerWorkloadService {
    private final TrainerWorkloadRepository repository;

    @Transactional
    @CircuitBreaker(name = "trainerService", fallbackMethod = "fallbackUpdateWorkload")
    public void updateWorkload(ExternalTrainingServiceDTO externalTraining) {
        log.info("Received data from external service -> {}", externalTraining);

        int year=0, month=0;
        if (externalTraining.trainingDate() != null) {
            year = externalTraining.trainingDate().getYear();
            month = externalTraining.trainingDate().getMonthValue();
        } else {
            log.error("Дата є null. Перевірте джерело даних.");
        }

        TrainerWorkload workload = repository.findByUsernameAndYearAndMonth(externalTraining.trainerUsername(), year, month)
                .orElseGet(() -> TrainerWorkload.builder()
                        .username(externalTraining.trainerUsername())
                        .firstName(externalTraining.trainerFirstName())
                        .lastName(externalTraining.trainerLastName())
                        .isActive(externalTraining.isActive())
                        .date(externalTraining.trainingDate())
                        .totalTrainingHours(0)
                        .build()
                );
        boolean isAdding = externalTraining.actionType().equalsIgnoreCase("add");

        log.info("Workload -> {}", workload);


        if (isAdding) {
            log.info("Adding training workload");
            workload.setTotalTrainingHours(workload.getTotalTrainingHours() + externalTraining.trainingDuration());
        } else {
            log.info("Deleting training workload");
            workload.setTotalTrainingHours(Math.max(0, workload.getTotalTrainingHours() - externalTraining.trainingDuration()));
        }
        log.info("Successfully saved workload: {}", workload);
        repository.save(workload);

        log.info("All workloads: {}", repository.findAll());

    }


    @Transactional
    @CircuitBreaker(name = "trainerService", fallbackMethod = "fallbackGetMonthlyWorkloadSummary")
    public TrainerWorkloadSummary getMonthlyWorkloadSummary(String username) {
        log.info("Calculating monthly workload summary for all trainers.");
        List<TrainerWorkload> workloads = repository.findByUsername(username);
//        List<TrainerWorkload> workloads = repository.findAll();

        if (workloads.isEmpty()) {
            log.info("No workload data found for trainer with username: {}", username);
            return new TrainerWorkloadSummary(username, "", "", false, new HashMap<>());
        }

        TrainerWorkload firstWorkload = workloads.getFirst();
        TrainerWorkloadSummary summary = new TrainerWorkloadSummary(
                username, firstWorkload.getFirstName(),
                firstWorkload.getLastName(), firstWorkload.isActive(),
                new HashMap<>()
        );

        workloads.forEach(workload -> {
            int year = workload.getDate().getYear();
            int month = workload.getDate().getMonthValue();
            summary.getMonthlySummary()
                    .computeIfAbsent(year, y -> new HashMap<>())
                    .merge(month, workload.getTotalTrainingHours(), Integer::sum);
        });

        log.info("Successfully calculated monthly workload summary: {} for trainer: {}", summary, username);
        return summary;
    }

    public ResponseEntity<String> fallbackUpdateWorkload(ExternalTrainingServiceDTO externalTraining, Throwable throwable) {
        log.error("🔄 Circuit Breaker activated for updateWorkload. Returning fallback response. Error: {}", throwable.getMessage());

        String fallbackMessage = String.format("⚠️ Failed to update workload for %s %s (%s) on %s. Please try again later.",
                externalTraining.trainerFirstName(),
                externalTraining.trainerLastName(),
                externalTraining.trainerUsername(),
                externalTraining.trainingDate());

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(fallbackMessage);
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
