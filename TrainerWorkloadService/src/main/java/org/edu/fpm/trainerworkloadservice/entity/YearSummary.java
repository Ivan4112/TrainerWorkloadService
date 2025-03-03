package org.edu.fpm.trainerworkloadservice.entity;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class YearSummary {
    private int year;
    private List<MonthSummary> months;
}
