package tn.esprit.examen.nomPrenomClasseExamen.entities;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Association implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAssociation;

    private String associationAddress;
    private String associationName;
    private String description;

    @Enumerated(EnumType.STRING)
    private AssociationStatus status = AssociationStatus.PENDING; // Default status is PENDING

    public enum AssociationStatus {
        PENDING,
        APPROVED,
        REJECTED
    }

    // Instead of storing large binary data, store file paths (more efficient)
    private String associationLogoPath;
    private String registrationDocumentPath;
    private String legalDocumentPath;

    // ✅ Relationship with Subscriber (1-1)
    @OneToOne
    @JoinColumn(name = "subscriber_id", unique = true)
    private Subscriber subscriber;

    // ✅ Other relationships
    @ManyToMany(cascade = CascadeType.ALL)
    private Set<Subscription> subscriptions;

    @ManyToMany(cascade = CascadeType.ALL)
    private Set<Mission> missions;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "association")
    private Set<Event> events;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "association")
    private Set<Notification> notifications;
}
