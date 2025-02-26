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

    // 🔹 Ajouter un Healthcare
    @PostMapping("/add")
    public ResponseEntity<Healthcare> createHealthcare(@RequestBody HealthcareDTO healthcareDTO) {
        Healthcare healthcare = healthcareService.addHealthcare(healthcareDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(healthcare);
    }

    // 🔹 Modifier un Healthcare
    @PutMapping("/{id}")
    public ResponseEntity<Healthcare> updateHealthcare(@PathVariable Long id, @RequestBody HealthcareDTO healthcareDTO) {
        try {
            Healthcare updatedHealthcare = healthcareService.updateHealthcare(id, healthcareDTO);
            return ResponseEntity.ok(updatedHealthcare);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 🔹 Supprimer un Healthcare
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHealthcare(@PathVariable Long id) {
        try {
            healthcareService.deleteHealthcare(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 🔹 Obtenir un Healthcare par ID
    @GetMapping("/{id}")
    public ResponseEntity<Healthcare> getHealthcareById(@PathVariable Long id) {
        Optional<Healthcare> healthcare = healthcareService.getHealthcareById(id);
        return healthcare.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 🔹 Obtenir tous les Healthcare
    @GetMapping
    public ResponseEntity<List<Healthcare>> getAllHealthcare() {
        return ResponseEntity.ok(healthcareService.getAllHealthcare());
    }
}
