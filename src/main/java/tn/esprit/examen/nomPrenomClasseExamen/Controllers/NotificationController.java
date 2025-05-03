package tn.esprit.examen.nomPrenomClasseExamen.Controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.NotificationRepository;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Notification;

@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    @Autowired
    private NotificationRepository notificationRepository;

    @GetMapping("/unadopted-animals")
    public ResponseEntity<Notification> getUnadoptedAnimalsNotification() {
        return notificationRepository.findByType("UNADOPTED_ANIMALS")
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}
