package com.laby.scheduling.scheduling_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tutors")
@Getter @Setter @NoArgsConstructor
public class Tutor {

    @Id
    @Column(name = "auth_user_id")
    private String authUserId; // from auth-service (PK)

    private Long schoolId;

    private int maxClassesPerDay;

    private String name;

    private boolean active = true;
}
