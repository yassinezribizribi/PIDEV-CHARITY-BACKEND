package tn.esprit.examen.nomPrenomClasseExamen.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.Set;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level= AccessLevel.PRIVATE)
@Entity
public class JobOffer implements Serializable {
    @Id
    private Long idJobOffer;
    private String title ;
    private String description;
    private String requirments;
    private Boolean isActive;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<Forum> forums;


}
