package com.laby.scheduling.scheduling_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "class_room_subjects",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"classRoomId", "subjectId"})
        }
)
@Getter
@Setter
@NoArgsConstructor
public class ClassRoomSubject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long classRoomId;
    private Long subjectId;

    private int weeklyRequiredPeriods;

    private boolean active = true;
}
