package tn.esprit.examen.nomPrenomClasseExamen.entities;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level= AccessLevel.PRIVATE)
@Entity
public class Paiement implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Ajout pour que l’ID soit auto-généré
    private Long idPaiement;

    private Date datePaiement;
    private int montant;

    private String stripePaymentId; // 🔐 ID unique du paiement Stripe
    private String status; // ✅ succeeded / pending / failed, etc.
    private String currency; // Ex: "usd", "eur"

    @ManyToOne
    private Event event;

    @ManyToOne
    private CagnotteEnligne cagnotte; // 🔗 Ajout pour bien relier chaque paiement à une cagnotte
    @ManyToOne


    @JoinColumn(name = "idUser")
    @JsonIgnore

    private User utilisateur;

}
