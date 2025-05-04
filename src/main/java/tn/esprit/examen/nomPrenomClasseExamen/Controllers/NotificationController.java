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

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_REFUGEE', 'ROLE_ASSOCIATION')")
    public ResponseEntity<List<NotificationDTO>> getNotifications(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "").trim();
        Long userId = jwtUtils.getUserIdFromJwtToken(token);
        return ResponseEntity.ok(notificationService.getNotificationsForSubscriber(userId));
    }

    @GetMapping("/unread/{userId}")
    public List<Message> getUnreadMessages(@PathVariable Long userId) {
        return conversationService.getUnreadMessages(userId);
    }

    @PutMapping("/{messageId}/read")
    public void markMessageAsRead(@PathVariable Long messageId) {
        conversationService.markSingleMessageAsRead(messageId);
    }

    @PostMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('ROLE_REFUGEE', 'ROLE_ASSOCIATION')")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }
}
