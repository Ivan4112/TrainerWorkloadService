package org.edu.fpm.trainerworkloadservice.service;

import org.edu.fpm.trainerworkloadservice.dto.ExternalTrainingServiceDTO;
import org.edu.fpm.trainerworkloadservice.entity.TrainerWorkload;
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

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadServiceTest {
    @Mock
    private TrainerWorkloadRepository workloadRepository;

    @InjectMocks
    private TrainerWorkloadService trainerWorkloadService;

    private ExternalTrainingServiceDTO externalTrainingAdd;
    private ExternalTrainingServiceDTO externalTrainingDelete;

    @BeforeEach
    void setUp() {
        externalTrainingAdd = TestDataFactory.externalTrainingAdd(ActionType.ADD);
        externalTrainingDelete = TestDataFactory.externalTrainingAdd(ActionType.DELETE);
    }

    @Test
    void updateWorkloadAdd_Test() {
        when(workloadRepository.save(any(TrainerWorkload.class))).thenReturn(null);
        trainerWorkloadService.updateWorkload(externalTrainingAdd);
        verify(workloadRepository, times(1)).save(any(TrainerWorkload.class));
    }

    @Test
    void updateWorkloadDelete_Test() {
        doNothing().when(workloadRepository).deleteById(1L);
        trainerWorkloadService.updateWorkload(externalTrainingDelete);
        verify(workloadRepository, times(1)).deleteById(1L);
    }

    @Test
    void getMonthlyWorkloadSummary_Test() {
        List<TrainerWorkload> workloads = List.of(
                new TrainerWorkload(1L, "trainer1", "John",
                        "Doe", true, LocalDate.now(), 5)
        );
        when(workloadRepository.findByUsername("trainer1")).thenReturn(workloads);

        TrainerWorkloadSummary summary = trainerWorkloadService.getMonthlyWorkloadSummary("trainer1");

        assertNotNull(summary);
        assertEquals("trainer1", summary.getUsername());
        assertEquals(1, summary.getMonthlySummary().size());
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

    @Test
    void saveAllTrainings_Test() {
        List<ExternalTrainingServiceDTO> trainingData = Collections.singletonList(externalTrainingAdd);
        trainerWorkloadService.saveAllTrainings(trainingData);
        verify(workloadRepository, times(1)).saveAll(anyList());
    }

}
