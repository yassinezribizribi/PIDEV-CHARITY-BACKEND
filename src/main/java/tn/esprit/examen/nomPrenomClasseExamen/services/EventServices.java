package tn.esprit.examen.nomPrenomClasseExamen.services;

import io.jsonwebtoken.Jwt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.*;
import tn.esprit.examen.nomPrenomClasseExamen.dto.EventDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.*;

import java.security.Principal;
import java.util.*;

@Service
@Component
@Slf4j
public class EventServices implements IEventServices {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private SubscriberRepository subscriberRepository;

    @Autowired
    private PaiementRepository paiementRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AssociationRepository associationRepository;




    @Override
    public Event addEvent(EventDTO eventDTO) {
        if (eventDTO == null) {
            throw new IllegalArgumentException("Les données de l'événement sont nulles.");
        }
        System.out.println("Server Time: " + new Date());

        log.info("Ajout d'un nouvel événement : {}", eventDTO.getTitle());

        // Récupérer l'association par son ID
        Association association = associationRepository.findById(eventDTO.getAssociationId())
                .orElseThrow(() -> new IllegalArgumentException("Association non trouvée avec l'ID : " + eventDTO.getAssociationId()));


        Event event = new Event();
        event.setTitle(eventDTO.getTitle());
        event.setDescription(eventDTO.getDescription());
        event.setDateTime(eventDTO.getDateTime());
        event.setLocation(eventDTO.getLocation());
        event.setAssociation(associationRepository.findById(eventDTO.getAssociationId())
                .orElseThrow(() -> new IllegalArgumentException("Association non trouvée avec l'ID : " + eventDTO.getAssociationId())));

        if(eventDTO.getTypeEvent() == TypeEvent.ANIMAL_WELFARE){
            event.setTypeEvent(TypeEvent.ANIMAL_WELFARE);
        }
        if(eventDTO.getTypeEvent() == TypeEvent.EDUCATIONAL_WORKSHOP){
            event.setTypeEvent(TypeEvent.EDUCATIONAL_WORKSHOP);
        }
        if(eventDTO.getTypeEvent() == TypeEvent.VOLUNTEERING_CAMPAIGN){
            event.setTypeEvent(TypeEvent.VOLUNTEERING_CAMPAIGN);
        }
        if(eventDTO.getTypeEvent() == TypeEvent.MEDICAL){
            event.setTypeEvent(TypeEvent.MEDICAL);
        }
        if(eventDTO.getTypeEvent() == TypeEvent.COMMUNITY_AWARENESS){
            event.setTypeEvent(TypeEvent.COMMUNITY_AWARENESS);
        }
        event.setReservationDate(eventDTO.getReservationDate());
        event.setAssociation(association);

        return eventRepository.save(event);
    }



    public User getProfil(Principal connectedUser) {
        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        Optional<User> userOptional = userRepository.findById(user.getIdUser());

        return userOptional.orElse(null);
    }

    @Override
    public void affectSubscriberToEvent(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + eventId));

        subscriberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Subscriber not found with ID: " + userId));

        subscriberRepository.toggleSubscriberAffectation(userId, eventId);
    }


    @Override
    public Event updateEvent(Long id, Event updatedEvent) {
        return eventRepository.findById(id)
                .map(existingEvent -> {
                    existingEvent.setTitle(updatedEvent.getTitle());
                    existingEvent.setDescription(updatedEvent.getDescription());
                    existingEvent.setDateTime(updatedEvent.getDateTime());
                    existingEvent.setLocation(updatedEvent.getLocation());
                    existingEvent.setTypeEvent(updatedEvent.getTypeEvent());
                    existingEvent.setReservationDate(updatedEvent.getReservationDate());

                    log.info("Mise à jour de l'événement avec ID {}", id);
                    return eventRepository.save(existingEvent);
                }).orElseThrow(() -> new RuntimeException("Événement introuvable avec l'ID: " + id));
    }

    @Override
    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new RuntimeException("Événement introuvable avec l'ID: " + id);
        }
        eventRepository.deleteById(id);
        log.info("Suppression de l'événement avec ID {}", id);
    }

    @Override
    public Optional<Event> getEventById(Long id) {
        return eventRepository.findById(id);
    }

    @Override
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    @Override
    public List<Event> getEventsByAssociation(Long associationId) {
        return eventRepository.findByAssociation_IdAssociation(associationId);
    }


    public Event addNotificationToEvent(Long eventId, Notification notification) {
        if (notification == null) {
            throw new IllegalArgumentException("Notification cannot be null.");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NoSuchElementException("Event not found with ID: " + eventId));

        notification.setEvent(event);
        event.getNotifications().add(notification);

        return eventRepository.save(event);
    }
    public Event markSubscriberAsInterested(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + eventId));

        Subscriber subscriber = subscriberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Subscriber not found with ID: " + userId));

        event.getEvent_subscribers().add(subscriber);
        subscriber.getEvents().add(event);

        subscriberRepository.save(subscriber);
        return eventRepository.save(event);
    }

    public Set<Subscriber> getEventSubscribers(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + eventId));
        return event.getEvent_subscribers();
    }



}


