package tn.esprit.examen.nomPrenomClasseExamen.Controllers;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.nomPrenomClasseExamen.dto.DonationDto;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Donation;
import tn.esprit.examen.nomPrenomClasseExamen.entities.DonationType;
import tn.esprit.examen.nomPrenomClasseExamen.services.DonationServices;

import java.util.List;


@RestController
@RequestMapping("/api/donations")
@CrossOrigin(origins = "http://localhost:4200")

@AllArgsConstructor
public class DonationController {

    private final DonationServices donationService;
    private static final Logger logger = LoggerFactory.getLogger(DonationController.class);
    // 🎯 Endpoint to contribute to an existing donation
//    @PostMapping  ("/{donationId}/contribute")
//    public ResponseEntity<DonationDto> contributeToDonation(
//            @PathVariable Long donationId,
//            @RequestBody DonationDto donationDto) {
//
//        DonationDto updatedDonation = donationService.contributeToDonation(donationId, donationDto);
//        return ResponseEntity.ok(updatedDonation);
//    }
    @GetMapping("/find/{donationType}")
    public ResponseEntity<List<DonationDto>> getDonationsByType(@PathVariable DonationType donationType) {
        List<DonationDto> donations = donationService.findByDonationType(donationType);
        return ResponseEntity.ok(donations);
    }
    @GetMapping("/getall")
    public List<Donation> getAllDonations() {
        return donationService.getAllDonations();
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<Donation> getDonationById(@PathVariable Long id) {
        return ResponseEntity.ok(donationService.getDonationById(id));
    }

    @PostMapping("/create")
    public ResponseEntity<?> createDonation(@RequestBody DonationDto donationDto) {
        try {
            logger.info("📝 Requête POST reçue pour créer une donation: {}", donationDto);
            Donation createdDonation = donationService.createDonation(donationDto);
            return ResponseEntity.ok(createdDonation);
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la création de la donation: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Erreur: " + e.getMessage());
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateDonation(@PathVariable Long id, @RequestBody DonationDto donationDto) {
        try {
            logger.info("🔄 Requête PUT reçue pour mettre à jour la donation avec ID: {}", id);
            Donation updatedDonation = donationService.updateDonation(id, donationDto);
            return ResponseEntity.ok(updatedDonation);
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la mise à jour de la donation: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Erreur: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteDonation(@PathVariable Long id) {
        logger.info("🗑 Requête DELETE reçue pour supprimer la donation avec ID: {}", id);
        donationService.deleteDonation(id);
        return ResponseEntity.noContent().build();
    }
}
