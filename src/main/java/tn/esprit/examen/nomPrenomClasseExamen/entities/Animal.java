package tn.esprit.examen.nomPrenomClasseExamen.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long IdAnimal;
    private String name;
    private String animalSpecies;
    private String race ;
    @Column(name = "image_path")
    private String imagePath;
    private String medicalHistory;
    private Boolean isAdopted;
    @OneToOne
    private Healthcare healthcare;

    @ManyToOne
    @JoinColumn(name = "id_user")
    private Subscriber subscriber;

    public String getImagePath() {
        return imagePath;
    }

    public Animal setImagePath(String imagePath) {
        this.imagePath = imagePath;
        return this;
    }
}
