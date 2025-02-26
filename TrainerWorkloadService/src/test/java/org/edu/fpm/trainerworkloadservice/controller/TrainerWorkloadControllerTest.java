package org.edu.fpm.trainerworkloadservice.controller;

import org.edu.fpm.trainerworkloadservice.dto.ExternalTrainingServiceDTO;
import org.edu.fpm.trainerworkloadservice.entity.TrainerWorkloadSummary;
import org.edu.fpm.trainerworkloadservice.service.TrainerWorkloadService;
import org.edu.fpm.trainerworkloadservice.util.ActionType;
import org.edu.fpm.trainerworkloadservice.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadControllerTest {
    @Mock
    private TrainerWorkloadService workloadService;

    @InjectMocks
    private TrainerWorkloadController workloadController;

    private ExternalTrainingServiceDTO trainingServiceAddDTO;
    private ExternalTrainingServiceDTO trainingServiceDeleteDTO;

    @BeforeEach
    void setUp() {
        trainingServiceAddDTO = TestDataFactory.externalTrainingAdd(ActionType.ADD);
        trainingServiceDeleteDTO = TestDataFactory.externalTrainingAdd(ActionType.DELETE);
    }

    @Test
    void updateWorkload_Test() {
        ResponseEntity<String> response = workloadController.updateWorkload("12345", trainingServiceAddDTO);

        verify(workloadService, times(1)).updateWorkload(trainingServiceAddDTO);
        assertEquals("Workload updated successfully.", response.getBody());
    }

    @Test
    void getMonthlyWorkloadSummary_Test() {
        String username = "trainerUsername";
        TrainerWorkloadSummary expectedSummary = new TrainerWorkloadSummary(
                username, "First", "Last", true, null);
        when(workloadService.getMonthlyWorkloadSummary(username)).thenReturn(expectedSummary);

        ResponseEntity<TrainerWorkloadSummary> response = workloadController.getMonthlyWorkloadSummary(
                "12345", username);

        verify(workloadService, times(1)).getMonthlyWorkloadSummary(username);
        assertEquals(expectedSummary, response.getBody());
    }

    @Test
    void initializeTrainerWorkload_Test() {
        List<ExternalTrainingServiceDTO> trainingData = List.of(trainingServiceAddDTO, trainingServiceDeleteDTO);

        ResponseEntity<String> response = workloadController.initializeTrainerWorkload("12345", trainingData);

        verify(workloadService, times(1)).saveAllTrainings(trainingData);
        assertEquals("Trainer workload initialized successfully", response.getBody());
    }
}
