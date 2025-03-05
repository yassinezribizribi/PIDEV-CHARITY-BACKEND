package tn.esprit.examen.nomPrenomClasseExamen.Controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.nomPrenomClasseExamen.dto.DonationDto;
import tn.esprit.examen.nomPrenomClasseExamen.dto.DonsDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Dons;
import tn.esprit.examen.nomPrenomClasseExamen.services.DonsServices;

@RestController
@RequestMapping("/api/dons")
@CrossOrigin(origins = "http://localhost:4200")
@AllArgsConstructor
public class DonsController {
    DonsServices donsServices;
    // ✅ Subscriber contributes to a donation
    @PostMapping("/{idDonation}/contribute")
    public ResponseEntity<DonsDTO> contributeToDonation(
            @PathVariable Long idDonation,
            @RequestBody DonsDTO donationDto
    ) {
        DonsDTO updatedDonation = donsServices.contributeToDonation(idDonation, donationDto);
        return ResponseEntity.ok(updatedDonation);
    }
}
