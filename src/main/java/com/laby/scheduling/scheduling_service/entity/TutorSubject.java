package com.laby.scheduling.scheduling_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "tutor_subjects",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"tutorId", "subjectId"})
        }
)
@Getter @Setter @NoArgsConstructor
public class TutorSubject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tutorId;   // authUserId
    private Long subjectId;
}
