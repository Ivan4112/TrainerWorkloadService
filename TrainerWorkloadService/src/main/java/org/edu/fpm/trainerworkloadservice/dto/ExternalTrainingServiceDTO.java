package org.edu.fpm.trainerworkloadservice.dto;

import org.edu.fpm.trainerworkloadservice.util.ActionType;

import java.time.LocalDate;

public record ExternalTrainingServiceDTO (String trainerUsername, String trainerFirstName,
                                          String trainerLastName, boolean isActive,
                                          LocalDate trainingDate, int trainingDuration, ActionType actionType) { }
