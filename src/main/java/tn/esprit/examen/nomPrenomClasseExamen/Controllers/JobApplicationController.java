package tn.esprit.examen.nomPrenomClasseExamen.Controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.nomPrenomClasseExamen.dto.JobApplicationDto;
import tn.esprit.examen.nomPrenomClasseExamen.services.JobApplicationServices;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/jobApplications")
@RequiredArgsConstructor
public class JobApplicationController {

    private final JobApplicationServices jobApplicationService;

    private static final Logger logger = LoggerFactory.getLogger(JobApplicationController.class);

    @GetMapping
    public ResponseEntity<List<JobApplicationDto>> getAllJobApplications() {
        logger.info("Getting all job applications");
        List<JobApplicationDto> applications = jobApplicationService.getAllJobApplications();
        logger.info("Fetched {} job applications", applications.size());
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobApplicationDto> getJobApplicationById(@PathVariable Long id) {
        logger.info("Getting job application with id: {}", id);
        JobApplicationDto jobApplication = jobApplicationService.getJobApplicationById(id);
        if (jobApplication != null) {
            logger.info("Found job application with id: {}", id);
            return ResponseEntity.ok(jobApplication);
        } else {
            logger.warn("Job application with id: {} not found", id);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/subscriber/{subscriberId}")
    public ResponseEntity<List<JobApplicationDto>> getBySubscriber(@PathVariable Long subscriberId) {
        logger.info("Getting job applications for subscriber with id: {}", subscriberId);
        List<JobApplicationDto> applications = jobApplicationService.getApplicationsBySubscriber(subscriberId);
        logger.info("Fetched {} job applications for subscriber id: {}", applications.size(), subscriberId);
        return ResponseEntity.ok(applications);
    }

    @PostMapping
    public ResponseEntity<JobApplicationDto> createJobApplication(@Valid @RequestBody JobApplicationDto jobApplicationDto) {
        logger.info("Creating a new job application for applicant id: {}", jobApplicationDto.getApplicantId());

        // Set applicationDate to current date and time if it's null or set it on the backend directly
        if (jobApplicationDto.getApplicationDate() == null) {
            jobApplicationDto.setApplicationDate(LocalDateTime.now()); // Current date and time
        }

        JobApplicationDto createdApplication = jobApplicationService.createJobApplication(jobApplicationDto);
        logger.info("Job application created with id: {}", createdApplication.getIdApplication());

        return ResponseEntity.status(201).body(createdApplication);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJobApplication(@PathVariable Long id) {
        logger.info("Deleting job application with id: {}", id);
        jobApplicationService.deleteJobApplication(id);
        logger.info("Job application with id: {} deleted", id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}
