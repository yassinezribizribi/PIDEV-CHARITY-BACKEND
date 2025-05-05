package tn.esprit.examen.nomPrenomClasseExamen.entities;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Association implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAssociation;

    private String associationAddress;

    private Double latitude;
    private Double longitude;
    private String associationName;
    private String description;

    @Enumerated(EnumType.STRING)
    private AssociationStatus status = AssociationStatus.PENDING;

    private String associationLogoPath;
    private String registrationDocumentPath;
    private String legalDocumentPath;

    @OneToOne
    private Subscriber subscriber;

    @ManyToMany(cascade = CascadeType.ALL)
    private Set<Subscription> subscriptions = new HashSet<>();

    @ManyToMany(cascade = CascadeType.ALL)
    private Set<Mission> missions = new HashSet<>();

    @OneToMany(mappedBy = "association", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Event> events = new HashSet<>();

    @OneToMany(mappedBy = "association", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Set<Notification> notifications = new HashSet<>();


    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE,CascadeType.REMOVE})
    @JoinTable(
            name = "association_partnerships",
            joinColumns = @JoinColumn(name = "association_id"),
            inverseJoinColumns = @JoinColumn(name = "partner_id")
    )
    private Set<Association> partners = new HashSet<>();

    private Integer partnershipScore = 0;

    @Enumerated(EnumType.STRING)
    private PartnershipTier partnershipTier = PartnershipTier.BRONZE;

    public enum AssociationStatus {
        PENDING, APPROVED, REJECTED
    }

    public enum PartnershipTier {
        BRONZE, SILVER, GOLD
    }
}