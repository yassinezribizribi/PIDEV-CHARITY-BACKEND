package tn.esprit.examen.nomPrenomClasseExamen.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Animal;
import tn.esprit.examen.nomPrenomClasseExamen.entities.DemandeAnimal;
import tn.esprit.examen.nomPrenomClasseExamen.services.DemandeAnimalService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/demandes")
public class DemandeAnimalController {

    private final DemandeAnimalService demandeService;

    @PostMapping("/enregistrer")
    public ResponseEntity<DemandeAnimal> enregistrer(
            @RequestParam Long userId,
            @RequestParam Long animalId
    ) {
        return ResponseEntity.ok(demandeService.enregistrerDemande(userId, animalId));
    }

    @PutMapping("/accepter/{id}")
    public ResponseEntity<?> accepter(@PathVariable Long id) {
        demandeService.accepterDemande(id);
        return ResponseEntity.ok("Demande acceptée et animal adopté.");
    }
    @GetMapping
    public List<DemandeAnimal> getall() {
        return demandeService.getall();
    }
    @GetMapping("/adopted-by-user/{userId}")
    public List<Animal> getAnimauxAdoptesParUser(@PathVariable Long userId) {
        return demandeService.getAnimauxAdoptesParUser(userId);
    }

}
