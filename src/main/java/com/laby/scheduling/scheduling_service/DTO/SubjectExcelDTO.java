package com.laby.scheduling.scheduling_service.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SubjectExcelDTO {
    private String name;
    private Long schoolId;
    private String grade;
    private int weeklyRequiredPeriods;
    private boolean active;
}
