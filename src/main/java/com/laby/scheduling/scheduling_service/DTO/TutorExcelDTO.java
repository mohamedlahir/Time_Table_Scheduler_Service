package com.laby.scheduling.scheduling_service.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TutorExcelDTO {

    private String tutorId;
    private String tutorName;
    private String email;
    private int maxWeeklyHours;
}
