package tn.esprit.examen.nomPrenomClasseExamen.Controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.SubscriberRepository;
import tn.esprit.examen.nomPrenomClasseExamen.dto.CrisisDTO;
import tn.esprit.examen.nomPrenomClasseExamen.dto.MessageResponseDto;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Crisis;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Subscriber;
import tn.esprit.examen.nomPrenomClasseExamen.services.ICrisisServices;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@AllArgsConstructor
@RequestMapping("/api/crises")
public class CrisisController {

    ICrisisServices crisisService;
    SubscriberRepository subscriberRepository;

    @PostMapping("/add")
    public ResponseEntity<Crisis> createCrisis(@RequestBody CrisisDTO crisisDTO, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Get the logged-in user based on their email/username
        String email = principal.getName();
        Subscriber subscriber = subscriberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        crisisDTO.setIdUser(subscriber.getIdUser()); // Ensure the user ID is set

        Crisis crisis = crisisService.addCrisis(crisisDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(crisis);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Crisis> updateCrisis(@PathVariable Long id, @RequestBody Crisis crisis) {
        try {
            Crisis updatedCrisis = crisisService.updateCrisis(id, crisis);
            return ResponseEntity.ok(updatedCrisis);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteCrisis(@PathVariable Long id) {
        try {
            crisisService.deleteCrisis(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Crisis> getCrisisById(@PathVariable Long id) {
        Optional<Crisis> crisis = crisisService.getCrisisById(id);
        return crisis.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 🟠 Obtenir toutes les crises
    @GetMapping("/all")
    public ResponseEntity<List<Crisis>> getAllCrises() {
        return ResponseEntity.ok(crisisService.getAllCrises());
    }





}
