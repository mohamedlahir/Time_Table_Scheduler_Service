package com.laby.scheduling.scheduling_service.service;

import com.laby.scheduling.scheduling_service.entity.ClassRoom;
import com.laby.scheduling.scheduling_service.entity.School;
import com.laby.scheduling.scheduling_service.repository.ClassRoomRepository;
import com.laby.scheduling.scheduling_service.repository.SchoolRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class InternalGenerationService {

    private SchoolRepository schoolRepository;
    private ClassRoomRepository classRoomRepository;

    public InternalGenerationService(SchoolRepository schoolRepository) {
        this.schoolRepository = schoolRepository;
        this.classRoomRepository = classRoomRepository;
    }


        public String generateTimetable(Long schoolId, LocalDate weekStartDate) {

            Optional<School> school = schoolRepository.findById(schoolId);
            if (school.isEmpty()) {
                return "School not found";
            }

            List<ClassRoom> classes = classRoomRepository.findBySchoolId(schoolId);
            if (classes.isEmpty()) {
                return "No class has created yet";
            }



            return null;
        }

}
