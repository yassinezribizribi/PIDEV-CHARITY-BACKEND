package tn.esprit.examen.nomPrenomClasseExamen.services;

import tn.esprit.examen.nomPrenomClasseExamen.dto.EventDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface IEventServices {
    Event addEvent(EventDTO eventDTO) ;
    Event updateEvent(Long id, Event updatedEvent);
    void deleteEvent(Long id);
    Optional<Event> getEventById(Long id);
    List<Event> getAllEvents();
    List<Event> getEventsByAssociation(Long associationId);
    Event addNotificationToEvent(Long eventId, Notification notification);

    Event markSubscriberAsInterested(Long eventId, Long userId);

    Set<Subscriber> getEventSubscribers(Long eventId);



    User getProfil(Principal connectedUser);

    void affectSubscriberToEvent(Long eventId, Long userId);
}
