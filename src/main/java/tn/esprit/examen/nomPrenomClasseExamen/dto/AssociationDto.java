package tn.esprit.examen.nomPrenomClasseExamen.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Association;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Event;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Mission;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Notification;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Subscription;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Subscriber;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssociationDto {
    private Long idAssociation;
    private String associationAddress;
    private String associationName;
    private String description;
    private Association.AssociationStatus status;
    private String associationLogoPath;
    private String registrationDocumentPath;
    private String legalDocumentPath;

    // Use @JsonIgnore to avoid circular references
    @JsonIgnore
    private Subscriber subscriber;

    // Use @JsonIgnore to avoid circular references
    @JsonIgnore
    private Set<Subscription> subscriptions;

    @JsonIgnore
    private Set<Mission> missions;

    @JsonIgnore
    private Set<Event> events;

    @JsonIgnore
    private Set<Notification> notifications;
}