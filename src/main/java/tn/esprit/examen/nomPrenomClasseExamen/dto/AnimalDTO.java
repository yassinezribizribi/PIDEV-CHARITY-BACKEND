package tn.esprit.examen.nomPrenomClasseExamen.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Healthcare;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Subscriber;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AnimalDTO {

    private Long idAnimal;
    private String name;
    private String animalSpecies;
    private String race;
    private String medicalHistory;
    private Boolean isAdopted;
    private Healthcare healthcare;
    private Subscriber subscriber;
}
