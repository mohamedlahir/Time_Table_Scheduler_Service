package com.laby.scheduling.scheduling_service.controller;
import com.laby.scheduling.scheduling_service.entity.Tutor;
import com.laby.scheduling.scheduling_service.entity.TutorLeave;
import com.laby.scheduling.scheduling_service.repository.TutorLeaveRepository;
import com.laby.scheduling.scheduling_service.repository.TutorRepository;
import com.laby.scheduling.scheduling_service.service.TutorLeaveCompensationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/scheduler/api/tutor/leave")
@RequiredArgsConstructor
public class TutorLeaveController {

    private final TutorLeaveRepository tutorLeaveRepository;
    private final TutorLeaveCompensationService compensationService;

    @PostMapping
    public ResponseEntity<String> applyLeave(
            @RequestBody TutorLeave leave
    ) {
        leave.setApproved(true); // assume admin approval for now
        tutorLeaveRepository.save(leave);

        // 🔥 Trigger compensation
        compensationService.compensateTutorLeave(
                leave.getTutorId(),
                leave.getFromDate(),
                leave.getToDate()
        );

        return ResponseEntity.ok("Leave applied and timetable compensated");
    }
}

