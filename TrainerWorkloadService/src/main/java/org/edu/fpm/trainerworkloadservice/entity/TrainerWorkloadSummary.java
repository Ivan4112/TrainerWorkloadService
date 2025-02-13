package org.edu.fpm.trainerworkloadservice.entity;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TrainerWorkloadSummary {
    private String username;
    private String firstName;
    private String lastName;
    private boolean isActive;
    private Map<Integer, Map<Integer, Integer>> monthlySummary;
}
