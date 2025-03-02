package org.edu.fpm.trainerworkloadservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.edu.fpm.trainerworkloadservice.dto.ExternalTrainingServiceDTO;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

@Service
public class DeadLetterQueueListener {
    private final ObjectMapper objectMapper;

    public DeadLetterQueueListener(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @JmsListener(destination = "trainer-workload-dlq")
    public void processDeadLetterMessage(String message) {
        try {
            ExternalTrainingServiceDTO failedMessage = objectMapper.readValue(message, ExternalTrainingServiceDTO.class);
            System.err.println("Received message in DLQ: " + failedMessage);
        } catch (Exception e) {
            System.err.println("Failed to process DLQ message: " + e.getMessage());
        }
    }
}
