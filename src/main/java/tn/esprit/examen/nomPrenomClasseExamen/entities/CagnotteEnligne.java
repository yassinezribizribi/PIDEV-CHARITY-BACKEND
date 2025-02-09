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
    private Long IdCagnotte;
    private String Title;
    private String Description;
    private float GoalAmount;
    private float CurrentAmount;

    @ManyToMany(mappedBy="cagnotteenlignes", cascade = CascadeType.ALL)
    private Set<Donation> donations;
}
