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
    @ManyToOne
    @JsonIgnore // Prevent recursion here
    Donation donation;
    @ManyToOne
    Subscriber subscriberDons;

}
