package com.laby.scheduling.scheduling_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.Date;

@Entity
public class InternalTimeTableGenerationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long timeTableId;
    private Long schoolId;
    private Date weekStartDate;
    private String status;
    private Date createdAt;
    private Date updatedAt;
    private Date weekEndDate;
    private String periodName;
    private String tuorName;
}
