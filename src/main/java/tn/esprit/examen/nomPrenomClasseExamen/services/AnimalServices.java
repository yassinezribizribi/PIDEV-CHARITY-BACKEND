package tn.esprit.examen.nomPrenomClasseExamen.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.AnimalRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.SubscriberRepository;
import tn.esprit.examen.nomPrenomClasseExamen.dto.AnimalDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Animal;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Subscriber;

import java.util.List;
import java.util.Optional;

@Service
@Component
@Slf4j
public class AnimalServices implements IAnimalServices {

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private SubscriberRepository subscriberRepository;

    @Override
    public Animal addAnimal(AnimalDTO animalDTO) {
        if (animalDTO == null) {
            throw new IllegalArgumentException("Les données de l'animal sont nulles.");
        }

        Animal animal = new Animal();
        animal.setName(animalDTO.getName());
        animal.setAnimalSpecies(animalDTO.getAnimalSpecies());
        animal.setRace(animalDTO.getRace());
        animal.setMedicalHistory(animalDTO.getMedicalHistory());
        animal.setIsAdopted(animalDTO.getIsAdopted());
        animal.setHealthcare(animalDTO.getHealthcare());

        // Récupérer le Subscriber via son idUser
        //        Subscriber subscriber = subscriberRepository.findById(animalDTO.getSubscriber().getIdUser())
        //              .orElseThrow(() -> new IllegalArgumentException("Subscriber (User) non trouvé avec l'ID : " + animalDTO.getSubscriber().getIdUser()));
        //    animal.setSubscriber(subscriber);
        return animalRepository.save(animal);
    }

    @Override
    public Animal updateAnimal(Long id, Animal updatedAnimal) {
        return animalRepository.findById(id)
                .map(existingAnimal -> {
                    existingAnimal.setName(updatedAnimal.getName());
                    existingAnimal.setAnimalSpecies(updatedAnimal.getAnimalSpecies());
                    existingAnimal.setRace(updatedAnimal.getRace());
                    existingAnimal.setMedicalHistory(updatedAnimal.getMedicalHistory());
                    existingAnimal.setIsAdopted(updatedAnimal.getIsAdopted());
                    existingAnimal.setHealthcare(updatedAnimal.getHealthcare());
                    existingAnimal.setSubscriber(updatedAnimal.getSubscriber());
                    return animalRepository.save(existingAnimal);
                }).orElseThrow(() -> new RuntimeException("Animal non trouvé avec l'ID : " + id));
    }

    @Override
    public void deleteAnimal(Long id) {
        if (!animalRepository.existsById(id)) {
            throw new RuntimeException("Animal non trouvé avec l'ID : " + id);
        }
        animalRepository.deleteById(id);
    }

    @Override
    public Optional<Animal> getAnimalById(Long id) {
        return animalRepository.findById(id);
    }

    @Override
    public List<Animal> getAllAnimals() {
        return animalRepository.findAll();
    }

    @Override
    // Méthode pour récupérer les animaux associés à un Subscriber via l'ID User
    public List<Animal> getAnimalsBySubscriber(Long userId) {
        return animalRepository.findBySubscriber_IdUser(userId);
    }

    @Override
    public List<Animal> getNonAdoptedAnimals() {
        return animalRepository.findByIsAdoptedFalse();
    }


}
