package tn.esprit.examen.nomPrenomClasseExamen.services;

import tn.esprit.examen.nomPrenomClasseExamen.dto.AnimalDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Animal;

import java.util.List;
import java.util.Optional;

public interface IAnimalServices {
    Animal addAnimal(AnimalDTO animalDTO);

    Animal updateAnimal(Long id, Animal updatedAnimal);

    void deleteAnimal(Long id);

    Optional<Animal> getAnimalById(Long id);

    List<Animal> getAllAnimals();

    // Méthode pour récupérer les animaux associés à un Subscriber via l'ID User
    List<Animal> getAnimalsBySubscriber(Long userId);
    List<Animal> getNonAdoptedAnimals();
}
