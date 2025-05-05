package tn.esprit.examen.nomPrenomClasseExamen.Controllers;

import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.nomPrenomClasseExamen.entities.CagnotteEnligne;
import tn.esprit.examen.nomPrenomClasseExamen.services.CagnotteServices;

import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/cagnottes")
@RequiredArgsConstructor
public class CagnotteEnligneController {

    private final CagnotteServices cagnotteService;
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(CagnotteEnligneController.class);

    @GetMapping
    public ResponseEntity<List<CagnotteEnligne>> getAllCagnottes() {
        logger.info("📢 Fetching all online cagnottes");
        return ResponseEntity.ok(cagnotteService.getAllCagnottes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CagnotteEnligne> getCagnotteById(@PathVariable Long id) {
        logger.info("🔍 Fetching cagnotte with ID: {}", id);
        return ResponseEntity.ok(cagnotteService.getCagnotteById(id));
    }
    @PostMapping("/cagnottes/add/{donationId}")
    public ResponseEntity<CagnotteEnligne> createCagnotteEtAffecterADonation(@PathVariable Long donationId, @RequestBody CagnotteEnligne cagnotte) {
        try {
            CagnotteEnligne createdCagnotte = cagnotteService.createCagnotteEtAffecterADonation(donationId, cagnotte);
            return ResponseEntity.ok(createdCagnotte);
        } catch (Exception e) {
            logger.error("❌ Error creating cagnotte: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(null);
        }
    }


    @PutMapping("/{id}")
    public ResponseEntity<CagnotteEnligne> updateCagnotte(@PathVariable Long id, @RequestBody CagnotteEnligne cagnotte) {
        logger.info("🔄 Updating cagnotte with ID: {}", id);
        return ResponseEntity.ok(cagnotteService.updateCagnotte(id, cagnotte));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCagnotte(@PathVariable Long id) {
        logger.info("🗑 Deleting cagnotte with ID: {}", id);
        cagnotteService.deleteCagnotte(id);
        return ResponseEntity.noContent().build();
    }
}
