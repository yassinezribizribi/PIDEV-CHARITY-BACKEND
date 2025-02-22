package tn.esprit.examen.nomPrenomClasseExamen.dto;

import lombok.Value;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Event;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Mission;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Notification;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Subscription;

import java.io.Serializable;
import java.util.Set;

/**
 * DTO for {@link tn.esprit.examen.nomPrenomClasseExamen.entities.Association}
 */

public class AssociationDto implements Serializable {
    Long idAssociation;
    String associationName;
    String description;
    Boolean validated;
    Set<Subscription> subscriptions;
    Set<Mission> missions;
    Set<Event> events;
    Set<Notification> notifications;
}