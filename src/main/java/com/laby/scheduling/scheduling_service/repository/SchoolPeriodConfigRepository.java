package com.laby.scheduling.scheduling_service.repository;

import com.laby.scheduling.scheduling_service.entity.SchoolPeriodConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SchoolPeriodConfigRepository
        extends JpaRepository<SchoolPeriodConfig, Long> {

    List<SchoolPeriodConfig> findBySchoolIdOrderByPeriodNumber(Long schoolId);

}
