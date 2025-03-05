package tn.esprit.examen.nomPrenomClasseExamen.Controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.nomPrenomClasseExamen.dto.JobOfferDto;
import tn.esprit.examen.nomPrenomClasseExamen.entities.JobOffer;
import tn.esprit.examen.nomPrenomClasseExamen.services.JobOfferServices;

import java.util.List;

@RestController
@RequestMapping("/api/jobOffers")
@AllArgsConstructor
public class JobOfferController {

    private final JobOfferServices jobOfferService;
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
}