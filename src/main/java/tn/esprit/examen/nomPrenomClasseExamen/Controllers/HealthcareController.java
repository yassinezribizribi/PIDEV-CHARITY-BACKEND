

package tn.esprit.examen.nomPrenomClasseExamen.Controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.nomPrenomClasseExamen.dto.HealthcareDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Healthcare;
import tn.esprit.examen.nomPrenomClasseExamen.services.IHealthcareServices;

import java.util.List;
import java.util.Optional;

@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/healthcare")
public class HealthcareController {

    @Autowired
    private IHealthcareServices healthcareService;

    // ✅ Ajouter un soin avec génération automatique du lien Jitsi
    @PostMapping("/add")
    public ResponseEntity<Healthcare> createHealthcare(@RequestBody HealthcareDTO healthcareDTO) {
        try {
            Healthcare healthcare = healthcareService.addHealthcare(healthcareDTO);
            log.info("✅ Rendez-vous ajouté avec succès. Lien vidéo : {}", healthcare.getMeetingUrl());
            return ResponseEntity.status(HttpStatus.CREATED).body(healthcare);
        } catch (Exception e) {
            log.error("❌ Erreur lors de l'ajout du soin : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // ✅ Modifier un soin existant
    @PutMapping("/{id}")
    public ResponseEntity<Healthcare> updateHealthcare(@PathVariable Long id, @RequestBody HealthcareDTO healthcareDTO) {
        try {
            Healthcare updatedHealthcare = healthcareService.updateHealthcare(id, healthcareDTO);
            log.info("✅ Rendez-vous mis à jour avec succès. ID : {}", id);
            return ResponseEntity.ok(updatedHealthcare);
        } catch (RuntimeException e) {
            log.error("❌ Erreur lors de la mise à jour du soin ID {} : {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // ✅ Modifier uniquement le statut et la date du rendez-vous
    @PatchMapping("/{id}/status")
    public ResponseEntity<Healthcare> updateHealthcareStatus(@PathVariable Long id, @RequestBody HealthcareDTO healthcareDTO) {
        try {
            Healthcare updatedHealthcare = healthcareService.updateHealthcareStatus(
                    id,
                    healthcareDTO.getStatus(),
                    healthcareDTO.getBookingDate()
            );
            log.info("✅ Statut du rendez-vous mis à jour avec succès. ID : {}", id);
            return ResponseEntity.ok(updatedHealthcare);
        } catch (RuntimeException e) {
            log.error("❌ Erreur lors de la mise à jour du statut du soin ID {} : {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // ✅ Supprimer un soin par ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHealthcare(@PathVariable Long id) {
        try {
            healthcareService.deleteHealthcare(id);
            log.info("✅ Rendez-vous supprimé avec succès. ID : {}", id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("❌ Erreur lors de la suppression du soin ID {} : {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // ✅ Récupérer un soin par ID
    @GetMapping("/{id}")
    public ResponseEntity<Healthcare> getHealthcareById(@PathVariable Long id) {
        Optional<Healthcare> healthcare = healthcareService.getHealthcareById(id);
        return healthcare.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // ✅ Récupérer tous les soins
    @GetMapping("/all")
    public ResponseEntity<List<HealthcareDTO>> getAllHealthcare() {
        List<HealthcareDTO> list = healthcareService.getAllHealthcare();
        return ResponseEntity.ok(list);
    }

}


