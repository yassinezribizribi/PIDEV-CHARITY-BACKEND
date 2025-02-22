package tn.esprit.examen.nomPrenomClasseExamen.Controllers;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.nomPrenomClasseExamen.dto.TrainingDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Training;
import tn.esprit.examen.nomPrenomClasseExamen.services.ITrainingServices;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/trainings")
public class TrainingController {

    @Autowired
    private ITrainingServices trainingServices;

    // 🟠 Ajouter une formation
    @PostMapping("/add")
    public ResponseEntity<Training> createTraining(@Valid @RequestBody TrainingDTO trainingDTO) {
        try {
            Training training = trainingServices.addTraining(trainingDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(training);
        } catch (IllegalArgumentException e) {
            log.error("Erreur lors de l'ajout de la formation : {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // 🟠 Mettre à jour une formation
    @PutMapping("/{id}")
    public ResponseEntity<Training> updateTraining(@Valid @PathVariable Long id, @RequestBody Training updatedTraining) {
        try {
            Training training = trainingServices.updateTraining(id, updatedTraining);
            return ResponseEntity.ok(training);
        } catch (RuntimeException e) {
            log.error("Erreur lors de la mise à jour de la formation : {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // 🟠 Supprimer une formation
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTraining(@Valid @PathVariable Long id) {
        try {
            trainingServices.deleteTraining(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Erreur lors de la suppression de la formation : {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // 🟠 Obtenir une formation par son ID
    @GetMapping("/{id}")
    public ResponseEntity<Training> getTrainingById(@Valid @PathVariable Long id) {
        Optional<Training> training = trainingServices.getTrainingById(id);
        return training.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 🟠 Obtenir toutes les formations
    @GetMapping
    public ResponseEntity<List<Training>> getAllTrainings() {
        List<Training> trainings = trainingServices.getAllTrainings();
        return ResponseEntity.ok(trainings);
    }

    // 🟠 Obtenir les formations par abonné
    @GetMapping("/subscriber/{subscriberId}")
    public ResponseEntity<List<Training>> getTrainingsBySubscriber(@Valid @PathVariable Long subscriberId) {
        List<Training> trainings = trainingServices.getTrainingsBySubscriber(subscriberId);
        return ResponseEntity.ok(trainings);
    }

    // 🟠 Ajouter un abonné à une formation
    @PostMapping("/{trainingId}/subscribers/{subscriberId}/add")
    public ResponseEntity<Training> addSubscriberToTraining(@Valid @PathVariable Long trainingId, @PathVariable Long subscriberId) {
        try {
            Training training = trainingServices.addSubscriberToTraining(trainingId, subscriberId);
            return ResponseEntity.ok(training);
        } catch (IllegalArgumentException | NoSuchElementException e) {
            log.error("Erreur lors de l'ajout de l'abonné à la formation : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}
