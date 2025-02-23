package tn.esprit.examen.nomPrenomClasseExamen.Controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.nomPrenomClasseExamen.dto.AnimalDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Animal;
import tn.esprit.examen.nomPrenomClasseExamen.services.IAnimalServices;

import java.util.List;
import java.util.Optional;

@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/animals")
public class AnimalController {

    @Autowired
    private IAnimalServices animalService;

    // Créer un animal
    @PostMapping("/add")
    public ResponseEntity<Animal> createAnimal(@RequestBody AnimalDTO animalDTO) {
        try {
            Animal animal = animalService.addAnimal(animalDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(animal);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Mettre à jour un animal
    @PutMapping("/updateAnimal/{id}")
    public ResponseEntity<Animal> updateAnimal(@PathVariable Long id, @RequestBody Animal updatedAnimal) {
        try {
            Animal updated = animalService.updateAnimal(id, updatedAnimal);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Supprimer un animal
    @DeleteMapping("/deleteAnimal/{id}")
    public ResponseEntity<Void> deleteAnimal(@PathVariable Long id) {
        try {
            animalService.deleteAnimal(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Obtenir un animal par ID
    @GetMapping("/getAnimalById/{id}")
    public ResponseEntity<Animal> getAnimalById(@PathVariable Long id) {
        Optional<Animal> animal = animalService.getAnimalById(id);
        return animal.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Obtenir tous les animaux
    @GetMapping("/getAllAnimals")
    public ResponseEntity<List<Animal>> getAllAnimals() {
        return ResponseEntity.ok(animalService.getAllAnimals());
    }

    // Obtenir les animaux par l'ID de l'utilisateur (Subscriber)
    @GetMapping("/getAnimalsByUserId/{userId}")
    public ResponseEntity<List<Animal>> getAnimalsByUserId(@PathVariable Long userId) {
        List<Animal> animals = animalService.getAnimalsBySubscriber(userId);
        if (animals.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(animals);
    }
}
