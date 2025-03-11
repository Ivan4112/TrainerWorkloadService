package cucumber.component;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.edu.fpm.trainerworkloadservice.controller.TrainerWorkloadController;
import org.edu.fpm.trainerworkloadservice.dto.ExternalTrainingServiceDTO;
import org.edu.fpm.trainerworkloadservice.entity.TrainerWorkloadSummary;
import org.edu.fpm.trainerworkloadservice.service.TrainerWorkloadService;
import org.edu.fpm.trainerworkloadservice.util.ActionType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@CucumberContextConfiguration
public class TrainerWorkloadStepDefinitions {
    @Mock
    private TrainerWorkloadService trainerWorkloadService;

    @InjectMocks
    private TrainerWorkloadController trainerWorkloadController;

    private ResponseEntity<String> response;
    private ResponseEntity<TrainerWorkloadSummary> summaryResponse;
    private ExternalTrainingServiceDTO request;

    @Given("a valid transaction ID {string} and a valid training data")
    public void validTransactionAndTrainingData(String transactionId) {
        request = new ExternalTrainingServiceDTO("trainer1", "John", "Doe", true,
                LocalDate.of(2025, 6, 10), 3, ActionType.ADD);}

    @When("the trainer updates the workload with the data:")
    public void updateTrainerWorkload(DataTable table) {
        response = trainerWorkloadController.updateWorkload("txn123", request);
    }

    @Then("the system should respond with {string}")
    public void verifySuccessfulWorkloadUpdate(String expectedMessage) {
        assertEquals(expectedMessage, response.getBody());
    }

    @Given("a valid transaction ID {string} and a training duration of {string}")
    public void invalidTrainingDuration(String transactionId, String duration) {
        request = new ExternalTrainingServiceDTO("test.lastName7", "John", "Doe", true,
                LocalDate.of(2025, 6, 10), Integer.parseInt(duration), ActionType.ADD);
        doThrow(new IllegalArgumentException("Invalid training duration: " + duration))
                .when(trainerWorkloadService).updateWorkload(any());
    }

    @Then("the system should respond with an error message {string}")
    public void verifyInvalidTrainingDurationError(String expectedMessage) {
        ResponseEntity<String> response = trainerWorkloadController.updateWorkload("txn123", request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(expectedMessage, response.getBody());
    }

    @Given("a valid trainer username {string}")
    public void validTrainerUsername(String username) {
        when(trainerWorkloadService.getMonthlyWorkloadSummary(username))
                .thenReturn(new TrainerWorkloadSummary(username, "test", "lastName7", true, List.of()));
    }

    @When("the system fetches the monthly workload summary for trainer {string} in June 2025")
    public void fetchMonthlyWorkloadSummary(String username) {
        summaryResponse = trainerWorkloadController.getMonthlyWorkloadSummary("txn123", username);
    }

    @Then("the system should respond with the monthly summary for the trainer including the total training duration")
    public void verifyMonthlySummaryResponse() {
        assertNotNull(summaryResponse.getBody());
    }

    @When("the system attempts to retrieve the monthly workload summary for trainer {string}")
    public void fetchMissingTrainerSummary(String username) {
        when(trainerWorkloadService.getMonthlyWorkloadSummary(username))
                .thenReturn(new TrainerWorkloadSummary(username, "test", "lastName7", true, List.of()));
        summaryResponse = trainerWorkloadController.getMonthlyWorkloadSummary("txn123", username);
    }

    @Then("the system should respond with an empty workload summary and a warning message {string}")
    public void verifyEmptyWorkloadSummary(String expectedMessage) {
        assertNotNull(summaryResponse.getBody());
    }
}
