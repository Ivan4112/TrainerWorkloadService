package org.edu.fpm.trainerworkloadservice.entity;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Document(collection = "trainer_workload")
public class TrainerWorkloadSummary {
    @Id
    @Indexed(unique = true)
    private String username;
    private String firstName;
    private String lastName;
    private boolean isActive;
    private List<YearSummary> years;
}
