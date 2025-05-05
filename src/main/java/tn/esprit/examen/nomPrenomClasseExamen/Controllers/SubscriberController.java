package tn.esprit.examen.nomPrenomClasseExamen.Controllers;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Subscriber;
import tn.esprit.examen.nomPrenomClasseExamen.services.SubscriberServices;

import java.util.List;

@RestController
@RequestMapping("/api/missions")
@CrossOrigin(origins = "http://localhost:4200")

@RequiredArgsConstructor
public class SubscriberController {
    private static final Logger logger = LoggerFactory.getLogger(MissionController.class);
    private final SubscriberServices subscriberService;
    // In SubscriberController.java

    @GetMapping("/me")
    public ResponseEntity<Subscriber> getCurrentSubscriber(Authentication authentication) {
        String username = authentication.getName(); // Assuming username is used for login
        Subscriber subscriber = subscriberService.findByUsername(username);
        if (subscriber == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(subscriber);
    }

    @GetMapping("/pending-volunteers")
    public ResponseEntity<List<Subscriber>> getPendingVolunteers() {
        List<Subscriber> pendingVolunteers = subscriberService.getPendingVolunteers();
        return ResponseEntity.ok(pendingVolunteers);
    }


}
