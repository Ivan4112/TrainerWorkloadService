@componentTest
Feature: Trainer Workload Management

  Scenario: Successfully update trainer workload
    Given a valid transaction ID "txn123" and a valid training data
    When the trainer updates the workload with the data:
      | trainerUsername | trainerFirstName | trainerLastName | trainingDate | trainingDuration | isActive | actionType |
      | test.lastName7  | test             | lastName7       | 2025-06-10   | 2                | true     | ADD        |
    Then the system should respond with "Workload updated successfully."

  Scenario: Failed to update workload due to invalid training duration
    Given a valid transaction ID "txn123" and a training duration of "50"
    When the trainer updates the workload with the data:
      | trainerUsername | trainerFirstName | trainerLastName | trainingDate | trainingDuration | isActive | actionType |
      | trainer1        | John             | Doe             | 2025-06-10   | 50               | true     | ADD        |
    Then the system should respond with an error message "Invalid training duration: 50"

  Scenario: Successfully retrieve monthly workload summary
    Given a valid trainer username "test.lastName7"
    When the system fetches the monthly workload summary for trainer "test.lastName7" in June 2025
    Then the system should respond with the monthly summary for the trainer including the total training duration

  Scenario: Failed to retrieve workload summary due to missing trainer data
    Given a valid trainer username "test.lastName7"
    When the system attempts to retrieve the monthly workload summary for trainer "trainerNotFound"
    Then the system should respond with an empty workload summary and a warning message "No workload data found for trainer: trainerNotFound"

