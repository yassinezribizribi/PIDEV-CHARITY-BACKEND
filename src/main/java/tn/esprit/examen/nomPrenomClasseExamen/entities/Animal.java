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
    private String Name;
    private String AnimalSpecies;
    private String Race ;
    private String MedicalHistory;
    private Boolean IsAdopted;

    @OneToOne
    private Healthcare healthcare;

}
