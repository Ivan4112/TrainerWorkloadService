package org.edu.fpm.trainerworkloadservice.entity;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MonthSummary {
    private int month;
    private int trainingSummaryDuration;
}
