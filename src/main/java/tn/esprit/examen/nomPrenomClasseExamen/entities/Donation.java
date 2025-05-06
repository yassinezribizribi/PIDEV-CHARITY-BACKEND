package tn.esprit.examen.nomPrenomClasseExamen.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashSet;
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

    private String titre; // Title for donation request (set by association)
    private String description; // Details about the donation request
    private int quantiteDemandee; // Total amount needed (set by the association)
    private int quantiteDonnee; // Total amount received (updated as donors contribute)

    @Enumerated(EnumType.STRING)
    private DonationType donationType;

    private String imageUrl; // for one image
    private LocalDate startDate;
    private Date endDate;

    @Min(1)
    @Max(4)
    private int priority; // 1 = Low, 2 = Medium, 3 = High, 4 = Urgent

    private int quantiteExcedentaire; // NEW: To track excess donations

    @PrePersist
    public void onCreate() {
        this.startDate = LocalDate.now(); // Set automatically on creation
    }

    @Enumerated(EnumType.STRING)
    private DonationStatus status = DonationStatus.ACTIVE; // Default when created

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "donation")
    @JsonIgnore
    private Set<Dons> dons;

    @ManyToOne
    @JsonIgnore
    private Association associationDonation;

    @OneToOne(cascade = CascadeType.ALL)
    @JsonIgnore
    private CagnotteEnligne cagnotteenligne;

    // ✅ Nouvelle relation ManyToMany avec Subscriber (pour corriger l'erreur)
    @ManyToMany
    @JoinTable(
            name = "donation_subscribers",
            joinColumns = @JoinColumn(name = "donation_id"),
            inverseJoinColumns = @JoinColumn(name = "subscriber_id")
    )
    @JsonIgnore
    private Set<Subscriber> subscribers = new HashSet<>();

    // 🔹 Update donation logic
    public void addDonation(int quantite) {
        int newTotal = this.quantiteDonnee + quantite;

        if (newTotal > this.quantiteDemandee) {
            this.quantiteExcedentaire += (newTotal - this.quantiteDemandee);
        }

        this.quantiteDonnee = newTotal;
    }
}
