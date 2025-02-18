package tn.esprit.examen.nomPrenomClasseExamen.entities;
import jakarta.persistence.*;
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
public class CagnotteEnligne implements Serializable {

    @Id
    private Long idCagnotte;
    private String title;
    private String description;
    private float goalAmount;
    private float currentAmount;

    @ManyToMany(mappedBy="cagnotteenlignes", cascade = CascadeType.ALL)
    private Set<Donation> donations;
}
