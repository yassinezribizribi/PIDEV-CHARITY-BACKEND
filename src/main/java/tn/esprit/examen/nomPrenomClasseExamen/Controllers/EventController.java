package tn.esprit.examen.nomPrenomClasseExamen.Controllers;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.nomPrenomClasseExamen.dto.EventDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Event;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Notification;
import tn.esprit.examen.nomPrenomClasseExamen.services.IEventServices;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/events")
public class EventController {

    @Autowired
    private IEventServices eventServices;

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
}
