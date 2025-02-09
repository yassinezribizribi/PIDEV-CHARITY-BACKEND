package tn.esprit.examen.nomPrenomClasseExamen.entities;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level= AccessLevel.PRIVATE)
@Entity
public class Crisis implements Serializable {
    @Id
    private Long idCrisis;
    private Categorie categorie;
    private String Location;
    private String Updates;
    private String Description;
    private Date CrisisDate;

    @OneToMany(cascade = CascadeType.ALL, mappedBy="crisis")
    private Set<Mission> Missions;
}
