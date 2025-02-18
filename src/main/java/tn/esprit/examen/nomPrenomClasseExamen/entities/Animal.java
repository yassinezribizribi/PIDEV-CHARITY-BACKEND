package tn.esprit.examen.nomPrenomClasseExamen.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level= AccessLevel.PRIVATE)
@Entity
public class Animal implements Serializable {
    @Id
    private Long IdAnimal;
    private String name;
    private String animalSpecies;
    private String race ;
    private String medicalHistory;
    private Boolean isAdopted;

    @OneToOne
    private Healthcare healthcare;

}
