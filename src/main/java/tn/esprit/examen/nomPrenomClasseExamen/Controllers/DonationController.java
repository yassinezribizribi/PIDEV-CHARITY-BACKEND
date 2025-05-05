package tn.esprit.examen.nomPrenomClasseExamen.Controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.nomPrenomClasseExamen.dto.DonationDto;
import tn.esprit.examen.nomPrenomClasseExamen.entities.CagnotteEnligne;
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
    @GetMapping("/cagnotte/{id}")
    public ResponseEntity<CagnotteEnligne> getCagnotteByDonationId(@PathVariable("id") Long donationId) {
        CagnotteEnligne cagnotte = donationService.getCagnotteEnLigneByDonationId(donationId);
        return ResponseEntity.ok(cagnotte);
    }

    @GetMapping("/find/{donationType}")
    public ResponseEntity<List<DonationDto>> getDonationsByType(@PathVariable DonationType donationType) {
        List<DonationDto> donations = donationService.findByDonationType(donationType);
        return ResponseEntity.ok(donations);
    }
    @GetMapping("/getall")
    public ResponseEntity<List<DonationDto>> getAllDonations() {
        try {
            logger.info("📋 Fetching all donations with their associated cagnottes");
            List<DonationDto> donations = donationService.getAllDonations();
            return ResponseEntity.ok(donations);
        } catch (Exception e) {
            logger.error("❌ Error fetching donations: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    // Get donations by association ID extracted from the JWT token
    @GetMapping("/my-donations")
    public ResponseEntity<?> getDonationsByAssociationIdFromToken(HttpServletRequest request) {
        try {
            // Extract the JWT token from the request header
            String jwtToken = request.getHeader("Authorization").substring(7);  // Remove "Bearer " prefix
            logger.info("📝 Fetching donations for the association from the token");

            // Call the service method to get the donations
            List<Donation> donations = donationService.getDonationsByAssociationIdFromToken(jwtToken);
            return ResponseEntity.ok(donations);

        } catch (Exception e) {
            logger.error("❌ Error occurred while fetching donations for the association: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
    @GetMapping("/get/{id}")
    public ResponseEntity<Donation> getDonationById(@PathVariable Long id) {
        return ResponseEntity.ok(donationService.getDonationById(id));
    }

    @PostMapping("/create")
    public ResponseEntity<?> createDonation(@RequestBody DonationDto donationDto, @RequestHeader("Authorization") String authorizationHeader) {
        try {
            // Extract the token from the Authorization header
            String jwtToken = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;

            logger.info("📝 POST request received to create a donation: {}", donationDto);

            // Pass the JWT token to the service method
            Donation createdDonation = donationService.createDonation(donationDto, jwtToken);

            return ResponseEntity.ok(createdDonation);
        } catch (Exception e) {
            logger.error("❌ Error occurred while creating donation: {}", e.getMessage(), e);

            // Handle specific exceptions (e.g., association not found, token missing, etc.)
            String errorMessage = e instanceof RuntimeException ? e.getMessage() : "Unexpected error occurred";
            return ResponseEntity.internalServerError().body("Error: " + errorMessage);
        }
    }




    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateDonation(
            @PathVariable Long id,
            @RequestBody @Valid DonationDto donationDto,
            @RequestHeader("Authorization") String authorizationHeader) {

        try {
            logger.info("🔄 PUT request received to update donation with ID: {}", id);

            // Validate Authorization header
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                logger.warn("⛔ Invalid or missing Authorization header");
                return ResponseEntity.badRequest().body("Invalid or missing Authorization token");
            }

            String jwtToken = authorizationHeader.substring(7);

            Donation updatedDonation = donationService.updateDonation(id, donationDto, jwtToken);

            // Convert to DTO before returning
            DonationDto responseDto = DonationDto.fromDonation(updatedDonation);
            return ResponseEntity.ok(responseDto);

        } catch (RuntimeException e) {
            logger.error("❌ Error updating donation ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            logger.error("❌ Unexpected error updating donation ID {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().body("An unexpected error occurred");
        }
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteDonation(@PathVariable Long id) {
        logger.info("🗑 Requête DELETE reçue pour supprimer la donation avec ID: {}", id);
        donationService.deleteDonation(id);
        return ResponseEntity.noContent().build();
    }
}
