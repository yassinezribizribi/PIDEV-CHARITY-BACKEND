package tn.esprit.examen.nomPrenomClasseExamen.Controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.nomPrenomClasseExamen.dto.JobApplicationDto;
import tn.esprit.examen.nomPrenomClasseExamen.dto.JobOfferDto;
import tn.esprit.examen.nomPrenomClasseExamen.entities.JobApplication;
import tn.esprit.examen.nomPrenomClasseExamen.entities.JobOffer;
import tn.esprit.examen.nomPrenomClasseExamen.services.JobApplicationServices;
import tn.esprit.examen.nomPrenomClasseExamen.services.JobOfferServices;

import java.util.List;

@RestController
@RequestMapping("/api/jobOffers")
@AllArgsConstructor
public class JobOfferController {

    private final JobOfferServices jobOfferService;
    private final JobApplicationServices jobApplicationServices;
    private static final Logger logger = LoggerFactory.getLogger(JobOfferController.class);

    @GetMapping
    public ResponseEntity<List<JobOffer>> getAllJobOffers() {
        List<JobOffer> jobOffers = jobOfferService.getAllJobOffers();
        logger.info("✅ Réponse API : {}", jobOffers);
        return ResponseEntity.ok(jobOffers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobOffer> getJobOfferById(@PathVariable Long id) {
        JobOffer jobOffer = jobOfferService.getJobOfferById(id);
        logger.info("✅ Réponse API : {}", jobOffer);
        return ResponseEntity.ok(jobOffer);
    }

    @PostMapping
    public ResponseEntity<JobOffer> createJobOffer(@RequestBody JobOfferDto jobOfferDto, HttpServletRequest request) {
        logger.info("Received headers: {}", request.getHeader("Content-Type")); // Log Content-Type
        logger.info("Received payload: {}", jobOfferDto); // Log payload

        try {
            JobOffer createdJobOffer = jobOfferService.createJobOffer(jobOfferDto);
            logger.info("✅ Réponse API : {}", createdJobOffer);
            return ResponseEntity.status(201).body(createdJobOffer);
        } catch (Exception e) {
            logger.error("❌ Error creating JobOffer: {}", e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    // JobOfferController.java
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<JobOfferDto>> getJobOffersByUser(@PathVariable Long userId) {
        logger.info("Fetching job offers for user ID: {}", userId);
        List<JobOfferDto> jobOffers = jobOfferService.getJobOffersByUser(userId);
        return ResponseEntity.ok(jobOffers);
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobOffer> updateJobOffer(@PathVariable Long id, @RequestBody JobOfferDto jobOfferDto) {
        JobOffer updatedJobOffer = jobOfferService.updateJobOffer(id, jobOfferDto);
        logger.info("✅ Réponse API : {}", updatedJobOffer);
        return ResponseEntity.ok(updatedJobOffer);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJobOffer(@PathVariable Long id) {
        jobOfferService.deleteJobOffer(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/applications")
    public ResponseEntity<List<JobApplicationDto>> getApplications(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long jobOfferId) {

        logger.info("Fetching applications for user {} or job offer {}", userId, jobOfferId);

        if (userId != null && jobOfferId != null) {
            logger.warn("Both userId and jobOfferId provided; only one is allowed");
            return ResponseEntity.badRequest().build();
        }

        try {
            if (userId != null) {
                List<JobApplicationDto> applications = jobApplicationServices.getApplicationsForUserJobOffers(userId);
                return ResponseEntity.ok(applications);
            } else if (jobOfferId != null) {
                List<JobApplicationDto> applications = jobOfferService.getApplicationsForJobOffer(jobOfferId);
                return ResponseEntity.ok(applications);
            } else {
                logger.warn("No userId or jobOfferId provided");
                return ResponseEntity.badRequest().build();
            }
        } catch (RuntimeException e) {
            logger.error("Error fetching applications: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Internal server error: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }






}