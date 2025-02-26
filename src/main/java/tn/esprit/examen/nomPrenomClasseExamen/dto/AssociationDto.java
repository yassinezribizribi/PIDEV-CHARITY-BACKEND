package tn.esprit.examen.nomPrenomClasseExamen.dto;

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

    // Store file paths instead of binary data
    private String associationLogoPath;
    private String registrationDocumentPath;
    private String legalDocumentPath;

    private Subscriber subscriber;
    private Set<Subscription> subscriptions;
    private Set<Mission> missions;
    private Set<Event> events;
    private Set<Notification> notifications;
}
