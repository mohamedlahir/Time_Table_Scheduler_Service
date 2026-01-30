package com.laby.scheduling.scheduling_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "subjects",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"schoolId", "name"})
        }
)
@Getter @Setter @NoArgsConstructor
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Long schoolId;

    private int weeklyRequiredPeriods;

    private boolean active = true;
}
