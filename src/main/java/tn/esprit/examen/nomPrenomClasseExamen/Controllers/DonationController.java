package tn.esprit.examen.nomPrenomClasseExamen.Controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.nomPrenomClasseExamen.Utlis.ValidationUtils;
import tn.esprit.examen.nomPrenomClasseExamen.dto.DonationDto;
import tn.esprit.examen.nomPrenomClasseExamen.dto.DonationExtensionSuggestionDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.CagnotteEnligne;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Donation;
import tn.esprit.examen.nomPrenomClasseExamen.entities.DonationType;
import tn.esprit.examen.nomPrenomClasseExamen.services.DonationServices;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


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

//    @GetMapping("/{id}/prolong")
//    public ResponseEntity<?> smartProlongDonation(@PathVariable Long id) {
//        return donationService.smartProlongDonation(id);
//    }
//    // 2️⃣ Endpoint to apply the extension based on previous suggestion
//    @PostMapping("/{id}/apply-extension")
//    public ResponseEntity<?> applySmartExtension(@PathVariable Long id) {
//        return donationService.applySmartProlongation(id);
//    }

    @GetMapping("/donation/{id}/suggest-extension")
    public ResponseEntity<DonationExtensionSuggestionDTO> suggestExtension(@PathVariable Long id) {
        DonationExtensionSuggestionDTO suggestion = donationService.suggestDonationExtension(id);
        return ResponseEntity.ok(suggestion);
    }

    @PostMapping("/donation/{id}/apply-extension")
    public ResponseEntity<Map<String, String>> applyExtension(@PathVariable Long id, @RequestBody Map<String, Long> payload) {
        long daysToExtend = payload.get("daysToExtend");
        String result = donationService.applyDonationExtension(id, daysToExtend);

        Map<String, String> response = new HashMap<>();
        response.put("message", result);

        return ResponseEntity.ok(response);
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
    public ResponseEntity<?> createDonation(@Valid @RequestBody DonationDto donationDto,
                                            BindingResult bindingResult,
                                            @RequestHeader("Authorization") String authorizationHeader) {

        ResponseEntity<?> errorResponse = ValidationUtils.handleValidationErrors(bindingResult);
        if (errorResponse != null) return errorResponse;

        try {
            String jwtToken = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;
            Donation createdDonation = donationService.createDonation(donationDto, jwtToken);
            return ResponseEntity.ok(createdDonation);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }


    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateDonation(@PathVariable Long id,
                                            @Valid @RequestBody DonationDto donationDto,
                                            BindingResult bindingResult,
                                            @RequestHeader("Authorization") String authorizationHeader) {

        ResponseEntity<?> errorResponse = ValidationUtils.handleValidationErrors(bindingResult);
        if (errorResponse != null) return errorResponse;

        try {
            String jwtToken = authorizationHeader.substring(7);
            Donation updatedDonation = donationService.updateDonation(id, donationDto, jwtToken);
            return ResponseEntity.ok(DonationDto.fromDonation(updatedDonation));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("An unexpected error occurred");
        }
    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteDonation(@PathVariable Long id) {
        logger.info("🗑 Requête DELETE reçue pour supprimer la donation avec ID: {}", id);
        donationService.deleteDonation(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/sorted-active")
    public ResponseEntity<?> getSortedActiveDonations() {
        try {
            List<DonationDto> sortedDonations = donationService.getSortedActiveDonations();
            return ResponseEntity.ok(sortedDonations);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Error fetching donations: " + e.getMessage());
        }
    }


}
