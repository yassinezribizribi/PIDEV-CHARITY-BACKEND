package tn.esprit.examen.nomPrenomClasseExamen.Controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.AssociationRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.SubscriberRepository;
import tn.esprit.examen.nomPrenomClasseExamen.dto.EventDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.*;
import tn.esprit.examen.nomPrenomClasseExamen.jwt.JwtUtils;
import tn.esprit.examen.nomPrenomClasseExamen.services.IEventServices;

import java.security.Principal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

@Slf4j
@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/events")
public class EventController {

    @Autowired
    private IEventServices eventServices;

    private final JwtUtils jwtUtils;
    private final SubscriberRepository subscriberRepository;

    private final AssociationRepository associationRepository;

    public EventController(JwtUtils jwtUtils, SubscriberRepository subscriberRepository, AssociationRepository associationRepository) {
        this.jwtUtils = jwtUtils;
        this.subscriberRepository = subscriberRepository;
        this.associationRepository = associationRepository;
    }
    @PostMapping("/affect")
    public ResponseEntity<String> affectSubscriberToEvent(@RequestParam Long eventId, @RequestParam Long userId) {
        eventServices.affectSubscriberToEvent(eventId, userId);
        return ResponseEntity.ok("Subscriber affectation toggled successfully");
    }

    // 🟠 Ajouter un événement
    @PostMapping("/add")
    public ResponseEntity<Event> createEvent(@Valid @RequestBody EventDTO eventDTO) {
        try {
            Event event = eventServices.addEvent(eventDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(event);
        } catch (IllegalArgumentException e) {
            log.error("Erreur lors de l'ajout de l'événement : {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // 🟠 Mettre à jour un événement
    @PutMapping("/updateEvent/{id}")
    public ResponseEntity<Event> updateEvent(@PathVariable Long id,@Valid @RequestBody Event updatedEvent) {
        try {
            Event event = eventServices.updateEvent(id, updatedEvent);
            return ResponseEntity.ok(event);
        } catch (RuntimeException e) {
            log.error("Erreur lors de la mise à jour de l'événement : {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // 🟠 Supprimer un événement
    @DeleteMapping("/deleteEvent/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        try {
            eventServices.deleteEvent(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Erreur lors de la suppression de l'événement : {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // 🟠 Obtenir un événement par son ID
    @GetMapping("/getEventById/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        Optional<Event> event = eventServices.getEventById(id);
        return event.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 🟠 Obtenir tous les événements
    @GetMapping("/getAllEvents")
    public ResponseEntity<List<Event>> getAllEvents() {
        List<Event> events = eventServices.getAllEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/getProfil")
    public ResponseEntity<?> getProfil(Principal connectedUser) {
        User user = eventServices.getProfil(connectedUser);
        Optional<Association> association = associationRepository.findById(user.getIdUser());
        if (association != null) {
            return ResponseEntity.ok(association.get().getIdAssociation());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

//    @GetMapping("/association-id")
//    public ResponseEntity<Long> getAssociationId(HttpServletRequest request) {
//        String token = extractJwtFromHeader(request);
//        if (token == null) {
//            return ResponseEntity.badRequest().build();
//        }
//
//        Long userId = jwtUtils.getUserIdFromJwtToken(token);
//        Subscriber user = subscriberRepository.findById(userId)
//                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
//
//        if (user.getAssociationRole() == null) {
//            return ResponseEntity.badRequest().body(null);
//        }
//
//        return ResponseEntity.ok(user.getAssociation().getId());
//
//    }

    private String extractJwtFromHeader(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    // 🟠 Obtenir les événements par l'ID de l'association
    @GetMapping("/association/{associationId}")
    public ResponseEntity<List<Event>> getEventsByAssociation(@PathVariable Long associationId) {
        List<Event> events = eventServices.getEventsByAssociation(associationId);
        return ResponseEntity.ok(events);
    }

    // 🟠 Ajouter une notification à un événement
    @PostMapping("/addNotificationToEvent/{eventId}/notifications/add")
    public ResponseEntity<Event> addNotificationToEvent(@PathVariable Long eventId,@Valid @RequestBody Notification notification) {
        try {
            Event updatedEvent = eventServices.addNotificationToEvent(eventId, notification);
            return ResponseEntity.ok(updatedEvent);
        } catch (IllegalArgumentException | NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/{eventId}")
    public ResponseEntity<EventSubscriber> subscribeToEvent(@PathVariable Long eventId, @RequestParam Long userId) {
        EventSubscriber updatedEvent = eventServices.markSubscriberAsInterested(eventId, userId);
        return ResponseEntity.ok(updatedEvent);
    }

    @GetMapping("/{eventId}/subscribers")
    public ResponseEntity<Set<Subscriber>> getEventSubscribers(@PathVariable Long eventId) {
        Set<Subscriber> subscribers = eventServices.getEventSubscribers(eventId);
        return ResponseEntity.ok(subscribers);
    }
    @GetMapping("/{userId}/getevents")
    public ResponseEntity<List<Event>> getEventInteressetedByUser(@PathVariable Long userId) {
        List<Event> subscribers = eventServices.getEventInteressetedByUser(userId);
        return ResponseEntity.ok(subscribers);
    }

}
