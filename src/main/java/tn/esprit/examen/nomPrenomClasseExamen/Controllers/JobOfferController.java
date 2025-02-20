package tn.esprit.examen.nomPrenomClasseExamen.Controllers;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.nomPrenomClasseExamen.entities.JobOffer;
import tn.esprit.examen.nomPrenomClasseExamen.services.JobOfferServices;

import java.util.List;

@RestController
@RequestMapping("/api/jobOffers")
@AllArgsConstructor
public class JobOfferController {

    JobOfferServices jobOfferService;
    private static final Logger logger = LoggerFactory.getLogger(JobOfferController.class);

    @GetMapping
    public ResponseEntity<List<JobOffer>> getAllJobOffers() {
        return ResponseEntity.ok(jobOfferService.getAllJobOffers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobOffer> getJobOfferById(@PathVariable Long id) {
        return ResponseEntity.ok(jobOfferService.getJobOfferById(id));
    }

    @PostMapping
    public ResponseEntity<?> createJobOffer(@RequestBody JobOffer jobOffer) {
        try {
            logger.info("📝 Requête POST reçue pour créer une offre: {}", jobOffer);
            return ResponseEntity.ok(jobOfferService.createJobOffer(jobOffer));
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la création de l'offre: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Erreur: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobOffer> updateJobOffer(@PathVariable Long id, @RequestBody JobOffer jobOffer) {
        logger.info("🔄 Requête PUT reçue pour mettre à jour l'offre avec ID: {}", id);
        return ResponseEntity.ok(jobOfferService.updateJobOffer(id, jobOffer));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJobOffer(@PathVariable Long id) {
        logger.info("🗑 Requête DELETE reçue pour supprimer l'offre avec ID: {}", id);
        jobOfferService.deleteJobOffer(id);
        return ResponseEntity.noContent().build();
    }
}
