package tn.esprit.examen.nomPrenomClasseExamen.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.nomPrenomClasseExamen.dto.NotificationDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Message;
import tn.esprit.examen.nomPrenomClasseExamen.jwt.JwtUtils;
import tn.esprit.examen.nomPrenomClasseExamen.services.ConversationService;
import tn.esprit.examen.nomPrenomClasseExamen.services.INotificationService;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class NotificationController {

    private final INotificationService notificationService;
    private final JwtUtils jwtUtils;
    private final ConversationService conversationService;

    // ✅ Notifications générales de l'utilisateur
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_REFUGEE', 'ROLE_ASSOCIATION')")
    public ResponseEntity<List<NotificationDTO>> getNotifications(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "").trim();
        Long userId = jwtUtils.getUserIdFromJwtToken(token);
        return ResponseEntity.ok(notificationService.getNotificationsForSubscriber(userId));
    }

    // ✅ Notifications liées aux soins médicaux
    @GetMapping("/healthcare")
    @PreAuthorize("hasAnyRole('ROLE_REFUGEE')")
    public ResponseEntity<List<NotificationDTO>> getHealthcareNotifications(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "").trim();
        Long userId = jwtUtils.getUserIdFromJwtToken(token);
        return ResponseEntity.ok(notificationService.getHealthcareNotificationsForSubscriber(userId));
    }

    // ✅ Marquer une notification comme lue
    @PostMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('ROLE_REFUGEE', 'ROLE_ASSOCIATION')")
    public ResponseEntity<Void> markNotificationAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    // ✅ Récupérer les messages non lus (conversation)
    @GetMapping("/unread/{userId}")
    public List<Message> getUnreadMessages(@PathVariable Long userId) {
        return conversationService.getUnreadMessages(userId);
    }

    // ✅ Marquer un message comme lu (conversation)
    @PutMapping("/message/{messageId}/read")
    public void markSingleMessageAsRead(@PathVariable Long messageId) {
        conversationService.markSingleMessageAsRead(messageId);
    }
}
