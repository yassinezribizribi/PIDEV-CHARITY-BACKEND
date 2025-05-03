package tn.esprit.examen.nomPrenomClasseExamen.Controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.examen.nomPrenomClasseExamen.dto.AnimalDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Animal;
import tn.esprit.examen.nomPrenomClasseExamen.services.IAnimalServices;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    public ResponseEntity<Animal> createAnimal(
            @RequestPart("animal") AnimalDTO animalDTO,
            @RequestPart("image") MultipartFile imageFile,
            @RequestParam("iduser") Long iduser) {

        Animal savedAnimal = animalService.addAnimal(animalDTO, iduser, imageFile);
        return ResponseEntity.ok(savedAnimal);
    }

    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        try {
            Path imagePath = Paths.get("upload").resolve(filename).normalize();
            Resource resource = new UrlResource(imagePath.toUri());

            if (resource.exists()) {
                String contentType = Files.probeContentType(imagePath);
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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
    public ResponseEntity<AnimalDTO> getAnimalById(@PathVariable Long id) {
        Optional<Animal> animal = animalService.getAnimalById(id);
        return animal.map(a -> ResponseEntity.ok(new AnimalDTO(a))) // Convert Animal to AnimalDTO with subscriberId
                .orElseGet(() -> ResponseEntity.notFound().build());
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

    @GetMapping("/non-adopted")
    public List<Animal> getNonAdoptedAnimals() {
        return animalService.getNonAdoptedAnimals();
    }
}
