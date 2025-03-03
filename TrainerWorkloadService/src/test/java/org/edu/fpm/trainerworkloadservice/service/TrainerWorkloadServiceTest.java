package org.edu.fpm.trainerworkloadservice.service;

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
import org.springframework.data.mongodb.core.MongoTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verifyNoInteractions;

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

    @BeforeEach
    void setUp() {
        externalTrainingAdd = TestDataFactory.externalTrainingAdd(ActionType.ADD);
        externalTrainingDelete = TestDataFactory.externalTrainingAdd(ActionType.DELETE);
    }

    /*@Test
    void updateWorkloadAdd_Test() {
        TrainerWorkloadService trainerWorkloadServiceMock = Mockito.spy(trainerWorkloadService);
        doReturn(1).when(trainerWorkloadServiceMock).getMinTrainingDuration();
        doReturn(5).when(trainerWorkloadServiceMock).getMaxTrainingDuration();

        when(workloadRepository.findByUsername("trainer1")).thenReturn(Optional.empty());
        trainerWorkloadService.updateWorkload(externalTrainingAdd);
        verify(mongoTemplate, times(1)).findAndReplace(any(), any());
    }

    @Test
    void updateWorkloadDelete_Test() {
        doNothing().when(workloadRepository).deleteById("1L");
        trainerWorkloadService.updateWorkload(externalTrainingDelete);
        verify(workloadRepository, times(1)).deleteById("1L");
    }*/

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
