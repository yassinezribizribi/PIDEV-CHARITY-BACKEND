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
public class Donation implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDonation;
    private String quantite ;
    private Boolean availability;
    private Date lastUpdated;
    private DonationType donationType;
    private Long numCompte;//visitor

    @OneToOne
    private Paiement paiement;


    @ManyToMany(cascade = CascadeType.ALL)
    private Set<Subscriber> subscribers;

    @ManyToMany(cascade = CascadeType.ALL)
    private Set<CagnotteEnligne> cagnotteenlignes;

}
