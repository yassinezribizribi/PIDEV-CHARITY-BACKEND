package tn.esprit.examen.nomPrenomClasseExamen.Controllers;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.AssociationRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.CrisisRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.SubscriberRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.UserRepository;
import tn.esprit.examen.nomPrenomClasseExamen.dto.CrisisAssignmentResponse;
import tn.esprit.examen.nomPrenomClasseExamen.dto.CrisisDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.*;
import tn.esprit.examen.nomPrenomClasseExamen.services.CrisisServices;
import tn.esprit.examen.nomPrenomClasseExamen.services.ICrisisServices;
import tn.esprit.examen.nomPrenomClasseExamen.services.SmsService;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;


import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@AllArgsConstructor
@RequestMapping("/api/crises")
public class CrisisController {

    private final ICrisisServices crisisService;
    private final SubscriberRepository subscriberRepository;
    private final SmsService smsService;
    private final CrisisServices crisisServices;
    private final CrisisRepository crisisRepository;



    // ✅ Nouvelle méthode unique pour ajouter une crise avec ou sans image
    @PostMapping("/add")
    public ResponseEntity<Crisis> createCrisis(
            @RequestPart("crisisDTO") CrisisDTO crisisDTO,
            @RequestPart(value = "file", required = false) MultipartFile file,
            Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = principal.getName();
        Subscriber subscriber = subscriberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        crisisDTO.setIdUser(subscriber.getIdUser());

        Crisis crisis = (file != null)
                ? crisisService.addCrisisWithImage(crisisDTO, file)
                : crisisService.addCrisis(crisisDTO);



        return ResponseEntity.status(HttpStatus.CREATED).body(crisis);
    }

    @PutMapping("/update/{id}")
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

    @GetMapping("/all")
    public ResponseEntity<Page<Crisis>> getAllCrises(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("crisisDate").descending());
        Page<Crisis> crises = crisisRepository.findAll(pageable);
        return ResponseEntity.ok(crises);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Crisis>> getCrisesByStatus(@PathVariable CrisisStatus status) {
        return ResponseEntity.ok(crisisService.getCrisesByStatus(status));
    }

    @GetMapping("/severity/{severity}")
    public ResponseEntity<List<Crisis>> getCrisesBySeverity(@PathVariable CrisisSeverity severity) {
        return ResponseEntity.ok(crisisService.getCrisesBySeverity(severity));
    }

    @GetMapping("/user/{idUser}")
    public ResponseEntity<List<Crisis>> getCrisesByUserId(@PathVariable Long idUser) {
        return ResponseEntity.ok(crisisService.getCrisesBySubscriber(idUser));
    }

    @GetMapping("/me")
    public ResponseEntity<List<Crisis>> getMyCrises(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String email = principal.getName();
        Subscriber subscriber = subscriberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(crisisService.getCrisesBySubscriber(subscriber.getIdUser()));
    }

    @GetMapping("/categories")
    public List<String> getCategories() {
        return Arrays.stream(Categorie.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Crisis> updateCrisisStatus(@PathVariable Long id, @RequestParam CrisisStatus status) {
        try {
            Crisis updatedCrisis = crisisService.updateCrisisStatus(id, status);
            return ResponseEntity.ok(updatedCrisis);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/notify-crisis-alerts")
    public ResponseEntity<?> notifyCrisisAlerts() {
        crisisService.analyzeAndNotifyUsersByCity();
        return ResponseEntity.ok("Alerts sent successfully!");
    }

    // Dans CrisisServices.java


    @PutMapping("/{crisisId}/assign-association")
    public ResponseEntity<CrisisAssignmentResponse> assignCrisisToAssociation(@PathVariable Long crisisId) {
        CrisisAssignmentResponse response = crisisService.assignCrisisToAssociation(crisisId);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/saturated-zones")
    public ResponseEntity<List<String>> getSaturatedZones(@RequestParam(defaultValue = "3") int threshold) {
        List<String> saturatedZones = crisisServices.getSaturationZones(threshold);
        return ResponseEntity.ok(saturatedZones);
    }


}
