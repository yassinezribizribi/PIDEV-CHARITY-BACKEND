package tn.esprit.examen.nomPrenomClasseExamen.entities;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.LocalDateTime;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Id
    private Long idDonation;

    private String titre; // Title for donation request (set by association)
    private String description; // Details about the donation request
    private int quantiteDemandee; // Total amount needed (set by the association)
    private int quantiteDonnee ; // Total amount received (updated as donors contribute)

    //    private Boolean availability;
    private LocalDateTime lastUpdated;
    private DonationType donationType;

    private int quantiteExcedentaire ; // NEW: To track excess donations

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "donation")
    @JsonIgnore // Prevent recursion here
    private Set<Dons> dons;
    @ManyToMany
    @JoinTable(
            name = "donation_subscriber",
            joinColumns = @JoinColumn(name = "donation_id"),
            inverseJoinColumns = @JoinColumn(name = "subscriber_id")
    )
    private Set<Subscriber> subscribers;

    @ManyToOne
    @JsonIgnore // Prevent recursion here
    Association associationDonation;

    @OneToOne(cascade = CascadeType.ALL)
    @JsonIgnore // Prevent recursion here
    CagnotteEnligne cagnotteenligne;
    // 🔹 Update donation logic
    public void addDonation(int quantite) {
        int newTotal = this.quantiteDonnee + quantite;

        if (newTotal > this.quantiteDemandee) {
            this.quantiteExcedentaire += (newTotal - this.quantiteDemandee);
        }

        this.quantiteDonnee = newTotal;
        this.lastUpdated = LocalDateTime.now();
    }
}
