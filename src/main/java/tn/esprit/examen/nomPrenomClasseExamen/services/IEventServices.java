package tn.esprit.examen.nomPrenomClasseExamen.services;

import tn.esprit.examen.nomPrenomClasseExamen.dto.EventDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Event;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Notification;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Paiement;

import java.util.List;
import java.util.Optional;

public interface IEventServices {
     Event addEvent(EventDTO eventDTO) ;
    Event updateEvent(Long id, Event updatedEvent);
    void deleteEvent(Long id);
    Optional<Event> getEventById(Long id);
    List<Event> getAllEvents();
    List<Event> getEventsByAssociation(Long associationId);
     Event addNotificationToEvent(Long eventId, Notification notification);

    }
