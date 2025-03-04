package tn.esprit.examen.nomPrenomClasseExamen.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Animal;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Healthcare;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Subscriber;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AnimalDTO {

    private Long IdAnimal;
    private String name;
    private String animalSpecies;
    private String race;
    private String medicalHistory;
    private Boolean isAdopted;
    private Healthcare healthcare;
    private Subscriber subscriber;
    private Long subscriberId;
    // Constructor to map from Animal entity
    public AnimalDTO(Animal animal) {
        this.IdAnimal = animal.getIdAnimal();
        this.name = animal.getName();
        this.animalSpecies = animal.getAnimalSpecies();
        this.race = animal.getRace();
        this.medicalHistory = animal.getMedicalHistory();
        this.isAdopted = animal.getIsAdopted();
        // Get subscriberId from subscriber entity (idUser)
        this.subscriberId = (animal.getSubscriber() != null) ? animal.getSubscriber().getIdUser() : null;
    }

    // Getters and Setters (if necessary)
    public Long getSubscriberId() {
        return subscriberId;
    }

    public void setSubscriberId(Long subscriberId) {
        this.subscriberId = subscriberId;
    }


}
