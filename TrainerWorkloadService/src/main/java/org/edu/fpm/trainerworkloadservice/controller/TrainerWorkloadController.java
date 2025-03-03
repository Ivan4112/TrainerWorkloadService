package org.edu.fpm.trainerworkloadservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.edu.fpm.trainerworkloadservice.dto.ExternalTrainingServiceDTO;
import org.edu.fpm.trainerworkloadservice.entity.TrainerWorkloadSummary;
import org.edu.fpm.trainerworkloadservice.service.TrainerWorkloadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.edu.fpm.trainerworkloadservice.util.ApiPaths.TRAINER;

@Slf4j
@RestController
@RequestMapping(TRAINER)
@RequiredArgsConstructor
public class TrainerWorkloadController {
    private final TrainerWorkloadService service;

    @PostMapping("/update")
    public ResponseEntity<String> updateWorkload(
            @RequestHeader(value = "X-Transaction-Id", required = false) String transactionId,
            @RequestBody ExternalTrainingServiceDTO request) {
        log.info("Received request with TransactionId: {}", transactionId);
        log.info("Received body from main microservice {}", request);
        service.updateWorkload(request);
        return ResponseEntity.ok("Workload updated successfully.");
    }

    @GetMapping("/monthly-summary")
    public ResponseEntity<TrainerWorkloadSummary> getMonthlyWorkloadSummary(
            @RequestHeader(value = "X-Transaction-Id", required = false) String transactionId,
            @RequestParam String username) {
        log.info("Received TransactionId: {}", transactionId);
        log.info("Received request to get monthly workload summary for trainer: {}", username);
        TrainerWorkloadSummary summary = service.getMonthlyWorkloadSummary(username);
        return ResponseEntity.ok(summary);
    }
}
