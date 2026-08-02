package com.school.model;

import lombok.*;
import java.time.LocalTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Period {
    private int periodId;
    private int periodNumber;
    private LocalTime startTime;
    private LocalTime endTime;
}