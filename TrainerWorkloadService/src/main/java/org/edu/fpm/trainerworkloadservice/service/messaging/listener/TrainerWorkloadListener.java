package org.edu.fpm.trainerworkloadservice.service.messaging.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.edu.fpm.trainerworkloadservice.dto.ExternalTrainingServiceDTO;
import org.edu.fpm.trainerworkloadservice.entity.TrainerWorkloadSummary;
import org.edu.fpm.trainerworkloadservice.service.TrainerWorkloadService;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TrainerWorkloadListener {
    private final JmsTemplate jmsTemplate;
    private final TrainerWorkloadService trainerWorkloadService;
    private final ObjectMapper objectMapper;

    public TrainerWorkloadListener(JmsTemplate jmsTemplate,
                                   TrainerWorkloadService trainerWorkloadService,
                                   ObjectMapper objectMapper) {
        this.jmsTemplate = jmsTemplate;
        this.trainerWorkloadService = trainerWorkloadService;
        this.objectMapper = objectMapper;
    }

    @JmsListener(destination = "trainer-workload-queue")
    public void receiveAllTrainingByTrainerEvent(String message) {
        try {
            ExternalTrainingServiceDTO training = objectMapper.readValue(message, ExternalTrainingServiceDTO.class);
            trainerWorkloadService.updateWorkload(training);
            log.info("Received training update for user: {}", training.trainerUsername());
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize training update", e);
        }
    }

    @JmsListener(destination = "workload-request-statistics")
    public void receiveTrainerWorkloadStatisticsRequest(String username) {
        try {
            TrainerWorkloadSummary summary = trainerWorkloadService.getMonthlyWorkloadSummary(username);
            String sendStatisticsMessage = objectMapper.writeValueAsString(summary);
            log.info("sending json type statistics: {} ", sendStatisticsMessage);
            jmsTemplate.convertAndSend("workload-response-statistics", sendStatisticsMessage);
        } catch (JsonProcessingException e) {
            sendToDeadLetterQueue(username, "JSON serialization error");
        } catch (IllegalArgumentException e) {
            sendToDeadLetterQueue(username, "Invalid username");
        } catch (Exception e) {
            sendToDeadLetterQueue(username, "Unknown error: " + e.getMessage());
        }
    }

    private void sendToDeadLetterQueue(String username, String reason) {
        try {
            jmsTemplate.convertAndSend("workload-request-dlq", username);
            log.error("Moved to DLQ (workload-request-dlq): {}", reason);
        } catch (Exception e) {
            log.error("Failed to send message to DLQ: {}", e.getMessage());
        }
    }
}
