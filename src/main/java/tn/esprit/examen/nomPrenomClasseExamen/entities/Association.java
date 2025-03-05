package tn.esprit.examen.nomPrenomClasseExamen.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.util.Arrays;
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

    @Enumerated(EnumType.STRING) // Storing as string in the database
    private AssociationStatus status = AssociationStatus.PENDING; // Default status is PENDING

    // Association.java
    public enum AssociationStatus {
        PENDING,
        APPROVED,
        REJECTED;

        @JsonCreator
        public static AssociationStatus fromValue(String value) {
            return Arrays.stream(values())
                    .filter(status -> status.name().equalsIgnoreCase(value))
                    .findFirst()
                    .orElse(PENDING); // Default to PENDING if the value is invalid
        }
    }


    // Instead of storing large binary data, store file paths (more efficient)
    private String associationLogoPath;
    private String registrationDocumentPath;
    private String legalDocumentPath;

    // ✅ Relationship with Subscriber (1-1)
    @OneToOne
    private Subscriber subscriber;

    // ✅ Other relationships
    @ManyToMany(cascade = CascadeType.ALL)
    @JsonManagedReference
    private Set<Subscription> subscriptions;

    @ManyToMany(cascade = CascadeType.ALL)
    @JsonManagedReference
    private Set<Mission> missions;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "association")
    @JsonManagedReference
    private Set<Event> events;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "association")
    @JsonManagedReference
    private Set<Notification> notifications;
}
