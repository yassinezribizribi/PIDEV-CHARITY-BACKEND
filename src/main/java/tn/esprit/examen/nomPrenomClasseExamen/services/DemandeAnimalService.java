package tn.esprit.examen.nomPrenomClasseExamen.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.AnimalRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.DemandeAnimalRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.UserRepository;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Animal;
import tn.esprit.examen.nomPrenomClasseExamen.entities.DemandeAnimal;
import tn.esprit.examen.nomPrenomClasseExamen.entities.User;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DemandeAnimalService {

    private final DemandeAnimalRepository demandeRepo;
    private final AnimalRepository animalRepo;
    private final UserRepository userRepo;

    public DemandeAnimal enregistrerDemande(Long userId, Long animalId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Animal animal = animalRepo.findById(animalId)
                .orElseThrow(() -> new RuntimeException("Animal not found"));

        DemandeAnimal demande = new DemandeAnimal();
        demande.setUser(user);
        demande.setAnimal(animal);
        demande.setAccepted(false);

        return demandeRepo.save(demande);
    }

    public void accepterDemande(Long demandeId) {
        DemandeAnimal demande = demandeRepo.findById(demandeId)
                .orElseThrow(() -> new RuntimeException("Demande not found"));

        demande.setAccepted(true);
        demandeRepo.save(demande);

        Animal animal = demande.getAnimal();
        animal.setIsAdopted(true);
        animalRepo.save(animal);
    }
    public List<DemandeAnimal> getall(){
        return demandeRepo.findAll();
    }
    public List<Animal> getAnimauxAdoptesParUser(Long userId) {
        User user = userRepo.findById(userId).get();
        List<DemandeAnimal> demandesAcceptees = demandeRepo.findAcceptedDemandesByUser(user);
        return demandesAcceptees.stream()
                .map(DemandeAnimal::getAnimal)
                .toList();
    }
}
