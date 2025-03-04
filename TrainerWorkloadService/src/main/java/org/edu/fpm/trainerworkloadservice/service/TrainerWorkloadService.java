package org.edu.fpm.trainerworkloadservice.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.edu.fpm.trainerworkloadservice.dto.ExternalTrainingServiceDTO;
import org.edu.fpm.trainerworkloadservice.entity.MonthSummary;
import org.edu.fpm.trainerworkloadservice.entity.TrainerWorkloadSummary;
import org.edu.fpm.trainerworkloadservice.entity.YearSummary;
import org.edu.fpm.trainerworkloadservice.repository.TrainerWorkloadRepository;
import org.edu.fpm.trainerworkloadservice.util.ActionType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.FindAndReplaceOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TrainerWorkloadService {
    @Value("${validation.training.duration.min}")
    private int minTrainingDuration;

    @Value("${validation.training.duration.max}")
    private int maxTrainingDuration;

    private final TrainerWorkloadRepository workloadRepository;
    private final MongoTemplate mongoTemplate;

    @Transactional
    @CircuitBreaker(name = "trainerService", fallbackMethod = "fallbackUpdateWorkload")
    public void updateWorkload(ExternalTrainingServiceDTO externalTraining) {
        log.info("Received data from external service -> {}", externalTraining);
        validateTrainingData(externalTraining);
        Optional<TrainerWorkloadSummary> trainerOpt = workloadRepository.findByUsername(externalTraining.trainerUsername());

        TrainerWorkloadSummary trainer = trainerOpt.orElseGet(() -> {
            List<YearSummary> yearSummaries = trainerOpt.map(TrainerWorkloadSummary::getYears).orElse(new ArrayList<>());
            return new TrainerWorkloadSummary(
                    externalTraining.trainerUsername(),
                    externalTraining.trainerFirstName(),
                    externalTraining.trainerLastName(),
                    externalTraining.isActive(),
                    yearSummaries
            );
        });

        if (externalTraining.actionType().equals(ActionType.ADD)) {
            updateTrainerWorkload(trainer, externalTraining);
            log.info("Training added successfully for trainer {}", externalTraining.trainerUsername());
        } else {
            removeTraining(trainer, externalTraining);
            log.info("Training removed successfully for trainer {}", externalTraining.trainerUsername());
        }
        Query query = new Query(Criteria.where("username").is(externalTraining.trainerUsername()));
        mongoTemplate.findAndReplace(query, trainer, FindAndReplaceOptions.options().upsert());
    }

    @Transactional
    @CircuitBreaker(name = "trainerService", fallbackMethod = "fallbackGetMonthlyWorkloadSummary")
    public TrainerWorkloadSummary getMonthlyWorkloadSummary(String username) {
        log.info("Fetching workload summary for trainer: {}", username);

        return workloadRepository.findByUsername(username)
                .orElseGet(() -> {
                    log.warn("No workload data found for trainer: {}", username);
                    return new TrainerWorkloadSummary(username, "", "", false, new ArrayList<>());
                });
    }

    private void updateTrainerWorkload(TrainerWorkloadSummary trainer, ExternalTrainingServiceDTO dto) {
        int year = dto.trainingDate().getYear();
        int month = dto.trainingDate().getMonthValue();
        int duration = dto.trainingDuration();

        YearSummary yearSummary = trainer.getYears().stream()
                .filter(y -> y.getYear() == year)
                .findFirst()
                .orElseGet(() -> {
                    YearSummary newYear = new YearSummary(year, new ArrayList<>());
                    trainer.getYears().add(newYear);
                    return newYear;
                });

        MonthSummary monthSummary = yearSummary.getMonths().stream()
                .filter(m -> m.getMonth() == month)
                .findFirst()
                .orElseGet(() -> {
                    MonthSummary newMonth = new MonthSummary(month, 0);
                    yearSummary.getMonths().add(newMonth);
                    return newMonth;
                });

        monthSummary.setTrainingSummaryDuration(monthSummary.getTrainingSummaryDuration() + duration);
    }

    private void removeTraining(TrainerWorkloadSummary trainer, ExternalTrainingServiceDTO dto) {
        int year = dto.trainingDate().getYear();
        int month = dto.trainingDate().getMonthValue();
        int duration = dto.trainingDuration();

        trainer.getYears().stream()
                .filter(y -> y.getYear() == year)
                .findFirst()
                .ifPresent(yearSummary -> yearSummary.getMonths().stream()
                        .filter(m -> m.getMonth() == month)
                        .findFirst()
                        .ifPresent(monthSummary -> {
                            monthSummary.setTrainingSummaryDuration(monthSummary.getTrainingSummaryDuration() - duration);
                            if (monthSummary.getTrainingSummaryDuration() <= 0) {
                                yearSummary.getMonths().remove(monthSummary);
                            }
                        }));
    }

    private void validateTrainingData(ExternalTrainingServiceDTO dto) {
        if (dto.trainingDuration() < minTrainingDuration || dto.trainingDuration() > maxTrainingDuration) {
            throw new IllegalArgumentException("Invalid training duration: " + dto.trainingDuration());
        }
        if (dto.trainingDate().isBefore(LocalDate.now().plusDays(1))) {
            throw new IllegalArgumentException("Invalid training date: " + dto.trainingDate());
        }
    }

    public void fallbackUpdateWorkload(ExternalTrainingServiceDTO externalTraining, Throwable throwable) {
        log.error("🔄 Circuit Breaker activated for updateWorkload. Returning fallback response. Error: {}", throwable.getMessage());
        log.warn("⚠️ Failed to update workload for {} on {}. Please try again later.",
                externalTraining.trainerFirstName(), externalTraining.trainingDate());
    }

    public TrainerWorkloadSummary fallbackGetMonthlyWorkloadSummary(String username, Throwable throwable) {
        log.error("🔄 Circuit Breaker activated for getMonthlyWorkloadSummary. Error: {}", throwable.getMessage());
        return new TrainerWorkloadSummary(username, null, null, false, new ArrayList<>());
    }
}
