package org.edu.fpm.trainerworkloadservice.service;

import lombok.SneakyThrows;
import org.edu.fpm.trainerworkloadservice.dto.ExternalTrainingServiceDTO;
import org.edu.fpm.trainerworkloadservice.entity.TrainerWorkloadSummary;
import org.edu.fpm.trainerworkloadservice.repository.TrainerWorkloadRepository;
import org.edu.fpm.trainerworkloadservice.util.ActionType;
import org.edu.fpm.trainerworkloadservice.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.FindAndReplaceOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadServiceTest {
    @Mock
    private TrainerWorkloadRepository workloadRepository;
    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private TrainerWorkloadService trainerWorkloadService;

    private ExternalTrainingServiceDTO externalTrainingAdd;
    private ExternalTrainingServiceDTO externalTrainingDelete;
    private TrainerWorkloadSummary trainer;
    @BeforeEach
    @SneakyThrows
    void setUp()  {
        externalTrainingAdd = TestDataFactory.externalTrainingAdd(ActionType.ADD);
        externalTrainingDelete = TestDataFactory.externalTrainingAdd(ActionType.DELETE);
        trainer = new TrainerWorkloadSummary("trainer1", "John", "Doe", true, new ArrayList<>());

        Field minDurationField = TrainerWorkloadService.class.getDeclaredField("minTrainingDuration");
        minDurationField.setAccessible(true);
        minDurationField.set(trainerWorkloadService, 1);

        Field maxDurationField = TrainerWorkloadService.class.getDeclaredField("maxTrainingDuration");
        maxDurationField.setAccessible(true);
        maxDurationField.set(trainerWorkloadService, 5);
    }
    @Test
    void updateWorkload_NewTrainer_Test() {
        when(workloadRepository.findByUsername(externalTrainingAdd.trainerUsername())).thenReturn(Optional.empty());
        trainerWorkloadService.updateWorkload(externalTrainingAdd);
        verify(mongoTemplate).findAndReplace(any(Query.class), any(TrainerWorkloadSummary.class), any(FindAndReplaceOptions.class));
    }

    @Test
    void updateWorkload_ExistingTrainer_Test() {
        when(workloadRepository.findByUsername(externalTrainingAdd.trainerUsername())).thenReturn(Optional.of(trainer));
        trainerWorkloadService.updateWorkload(externalTrainingAdd);
        verify(mongoTemplate).findAndReplace(any(Query.class), any(TrainerWorkloadSummary.class), any(FindAndReplaceOptions.class));
    }

    @Test
    void updateWorkload_RemoveTraining_Test() {
        when(workloadRepository.findByUsername(externalTrainingAdd.trainerUsername())).thenReturn(Optional.of(trainer));
        trainerWorkloadService.updateWorkload(externalTrainingDelete);
        verify(mongoTemplate).findAndReplace(any(Query.class), any(TrainerWorkloadSummary.class), any(FindAndReplaceOptions.class));
    }

    @Test
    void getMonthlyWorkloadSummary_ExistingTrainer_Test() {
        when(workloadRepository.findByUsername("trainer1")).thenReturn(Optional.of(trainer));
        TrainerWorkloadSummary result = trainerWorkloadService.getMonthlyWorkloadSummary("trainer1");
        assertEquals(trainer, result);
    }

    @Test
    void getMonthlyWorkloadSummary_NonExistingTrainer_Test() {
        when(workloadRepository.findByUsername("trainer1")).thenReturn(Optional.empty());
        TrainerWorkloadSummary result = trainerWorkloadService.getMonthlyWorkloadSummary("trainer1");
        assertNotNull(result);
        assertEquals("trainer1", result.getUsername());
    }

    @Test
    void ValidateTrainingData_Valid_Test() {
        assertDoesNotThrow(() -> trainerWorkloadService.updateWorkload(externalTrainingAdd));
    }

    @Test
    void validateTrainingData_InvalidDuration_Test() {
        ExternalTrainingServiceDTO inexternalTrainingAdd = new ExternalTrainingServiceDTO("trainer1",
                "John", "Doe", true, LocalDate.now().plusDays(2), 10, ActionType.ADD);
        assertThrows(IllegalArgumentException.class, () -> trainerWorkloadService.updateWorkload(inexternalTrainingAdd));
    }

    @Test
    void validateTrainingData_InvalidDate_Test() {
        ExternalTrainingServiceDTO inexternalTrainingAdd = new ExternalTrainingServiceDTO("trainer1",
                "John", "Doe", true,LocalDate.now(), 3,  ActionType.ADD);
        assertThrows(IllegalArgumentException.class, () -> trainerWorkloadService.updateWorkload(inexternalTrainingAdd));
    }

    @Test
    void fallbackUpdateWorkload_Test() {
        trainerWorkloadService.fallbackUpdateWorkload(externalTrainingAdd, new RuntimeException("Error"));
        verifyNoInteractions(workloadRepository);
    }

    @Test
    void fallbackGetMonthlyWorkloadSummary_Test() {
        TrainerWorkloadSummary summary = trainerWorkloadService.fallbackGetMonthlyWorkloadSummary("trainer1", new RuntimeException("Error"));
        assertNotNull(summary);
        assertEquals("trainer1", summary.getUsername());
        assertEquals("N/A", summary.getFirstName());
    }
}
