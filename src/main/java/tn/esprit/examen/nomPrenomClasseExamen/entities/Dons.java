package tn.esprit.examen.nomPrenomClasseExamen.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level= AccessLevel.PRIVATE)
@Entity
public class Dons implements Serializable {
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Id

    private Long idDons;
    // 🔹 DONOR contributes to an existing donation
    private String nomDoneur;
    private String prenomDoneur;
    //    private Long numCompte;//visitor
    private int quantite ;

    private LocalDateTime donationDate = LocalDateTime.now();

    private LocalDateTime scheduledDateTime; // to store scheduled drop-off/pick-up time
    private boolean associationValidated;
    private String donorNote;           // Optional: note from donor
    private DonsDeliveryMethod deliveryMethod; // DROP_OFF: The donor will bring the items themselves to a specified location.
    //PICK_UP: The association will send someone to collect the items from the donor’s address.
    private String donorAddress; // Optional: address of the donor for pick-up


    @ManyToOne
    @JsonIgnore // Prevent recursion here
    Donation donation;

    @ManyToOne
    @JsonIgnore // Prevent recursion here
    Subscriber subscriberDons;

}
