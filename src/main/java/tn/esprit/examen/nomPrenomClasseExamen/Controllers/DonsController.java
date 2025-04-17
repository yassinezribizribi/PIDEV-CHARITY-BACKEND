package tn.esprit.examen.nomPrenomClasseExamen.Controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.nomPrenomClasseExamen.dto.DonationDto;
import tn.esprit.examen.nomPrenomClasseExamen.dto.DonsDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Dons;
import tn.esprit.examen.nomPrenomClasseExamen.services.DonsServices;

import java.util.List;

@RestController
@RequestMapping("/api/dons")
@CrossOrigin(origins = "http://localhost:4200")
@AllArgsConstructor
public class DonsController {
    DonsServices donsServices;
    // ✅ Subscriber contributes to a donation
    @PostMapping("/{idDonation}/contribute")
    public ResponseEntity<Dons> contributeToDonation(
            @PathVariable Long idDonation,
            @RequestBody Dons dons // Accept Dons entity directly
    ) {
        Dons updatedDons = donsServices.contributeToDonation(idDonation, dons); // Use the method with Dons entity
        return ResponseEntity.ok(updatedDons); // Return the Dons entity directly
    }


    @PostMapping("/{idDons}/validate")
    public ResponseEntity<DonsDTO> validateDonsByAssociation(
            @PathVariable Long idDons
    ) {
        DonsDTO validatedDons = donsServices.validateDonsByAssociation(idDons);
        return ResponseEntity.ok(validatedDons);
    }
    @GetMapping("/donations/{donationId}/dons")
    public ResponseEntity<List<DonsDTO>> getDonsByDonation(@PathVariable Long donationId) {
        List<DonsDTO> dons = donsServices.getDonsByDonationId(donationId);
        return ResponseEntity.ok(dons);
    }


}
