package org.edu.fpm.trainerworkloadservice.util;

import org.edu.fpm.trainerworkloadservice.dto.ExternalTrainingServiceDTO;

import java.time.LocalDate;

public class TestDataFactory {

    public static ExternalTrainingServiceDTO externalTrainingAdd(ActionType actionType) {
        return new ExternalTrainingServiceDTO(
                1L, "trainerUsername",
                "trainerFirstName", "trainerLastName", true,
                LocalDate.now(), 10, actionType);
    }
}
