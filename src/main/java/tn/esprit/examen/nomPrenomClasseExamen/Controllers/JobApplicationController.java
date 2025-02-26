package tn.esprit.examen.nomPrenomClasseExamen.Controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.nomPrenomClasseExamen.dto.JobApplicationDto;
import tn.esprit.examen.nomPrenomClasseExamen.services.JobApplicationServices;
import tn.esprit.examen.nomPrenomClasseExamen.jwt.JwtUtils;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/jobApplications")
@RequiredArgsConstructor
public class JobApplicationController {

    private final JobApplicationServices jobApplicationService;
    private final JwtUtils jwtUtils;

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

    @CrossOrigin(origins = "http://localhost:4200")  // Allow requests from your frontend's domain
    @PostMapping("/{jobOfferId}")
    public ResponseEntity<JobApplicationDto> applyForJob(
            @PathVariable Long jobOfferId,
            @RequestHeader("Authorization") String token) {

        // Extract idUser from JWT Token
        String jwtToken = token.replace("Bearer ", "");
        Long applicantId = jwtUtils.getUserIdFromJwtToken(jwtToken);

        logger.info("User with ID {} is applying for job offer {}", applicantId, jobOfferId);

        // Apply for the job
        JobApplicationDto createdApplication = jobApplicationService.applyForJob(jobOfferId, applicantId);

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
