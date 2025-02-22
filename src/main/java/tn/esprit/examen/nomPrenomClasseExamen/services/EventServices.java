package tn.esprit.examen.nomPrenomClasseExamen.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.EventRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.AssociationRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.PaiementRepository;
import tn.esprit.examen.nomPrenomClasseExamen.dto.EventDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Event;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Association;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Notification;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Paiement;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
@Service
@Component
@Slf4j
public class EventServices implements IEventServices {

        @Autowired
        private EventRepository eventRepository;
        @Autowired
        private PaiementRepository paiementRepository;

        @Autowired
        private AssociationRepository associationRepository;

    @Autowired
    public EventServices(PaiementRepository paiementRepository) {
        this.paiementRepository = paiementRepository;
    }
        @Override
        public Event addEvent(EventDTO eventDTO) {
            if (eventDTO == null) {
                throw new IllegalArgumentException("Les données de l'événement sont nulles.");
            }

            log.info("Ajout d'un nouvel événement : {}", eventDTO.getTitle());

            // Récupérer l'association par son ID
            Association association = associationRepository.findById(eventDTO.getAssociationId())
                    .orElseThrow(() -> new IllegalArgumentException("Association non trouvée avec l'ID : " + eventDTO.getAssociationId()));

            Event event = new Event();
            event.setTitle(eventDTO.getTitle());
            event.setDescription(eventDTO.getDescription());
            event.setDateTime(eventDTO.getDateTime());
            event.setLocation(eventDTO.getLocation());
            event.setTypeEvent(eventDTO.getTypeEvent());
            event.setReservationDate(eventDTO.getReservationDate());
            event.setAssociation(association);

            return eventRepository.save(event);
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

    }


