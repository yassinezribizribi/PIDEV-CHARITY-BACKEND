package tn.esprit.examen.nomPrenomClasseExamen.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.nomPrenomClasseExamen.entities.JobApplication;
import tn.esprit.examen.nomPrenomClasseExamen.services.JobApplicationServices;

import java.util.List;

@RestController
@RequestMapping("/api/jobApplications")
@RequiredArgsConstructor
public class JobApplicationController {

    private final JobApplicationServices jobApplicationService;

    @GetMapping
    public ResponseEntity<List<JobApplication>> getAllJobApplications() {
        return ResponseEntity.ok(jobApplicationService.getAllJobApplications());
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobApplication> getJobApplicationById(@PathVariable Long id) {
        return ResponseEntity.ok(jobApplicationService.getJobApplicationById(id));
    }

    @GetMapping("/subscriber/{subscriberId}")
    public ResponseEntity<List<JobApplication>> getBySubscriber(@PathVariable Long subscriberId) {
        return ResponseEntity.ok(jobApplicationService.getApplicationsBySubscriber(subscriberId));
    }

    @PostMapping
    public ResponseEntity<JobApplication> createJobApplication(@RequestBody JobApplication jobApplication) {
        return ResponseEntity.ok(jobApplicationService.createJobApplication(jobApplication));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJobApplication(@PathVariable Long id) {
        jobApplicationService.deleteJobApplication(id);
        return ResponseEntity.noContent().build();
    }
}
