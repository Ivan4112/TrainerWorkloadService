package org.edu.fpm.trainerworkloadservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "trainer_workload")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class TrainerWorkload {
    @Id
    private Long trainingId;
    private String username;
    private String firstName;
    private String lastName;
    private boolean isActive;
    private LocalDate date;
    private int trainingHours;
}
