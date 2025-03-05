package tn.esprit.examen.nomPrenomClasseExamen.entities;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)


    @Id
    private Long idCagnotte;
    private String title;
    private String description;
    private float goalAmount;
    private float currentAmount;

    @OneToOne(mappedBy="cagnotteenligne",cascade = CascadeType.ALL)
    @JsonIgnore // Prevent recursion here

    private Donation donation;


    @OneToMany(cascade = CascadeType.ALL)
    @JsonIgnore // Prevent recursion here

    private Set<Paiement> Paiements;
}
