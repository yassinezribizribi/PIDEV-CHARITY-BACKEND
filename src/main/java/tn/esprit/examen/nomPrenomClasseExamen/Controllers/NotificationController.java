package tn.esprit.examen.nomPrenomClasseExamen.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.nomPrenomClasseExamen.dto.NotificationDTO;
import tn.esprit.examen.nomPrenomClasseExamen.jwt.JwtUtils;
import tn.esprit.examen.nomPrenomClasseExamen.services.INotificationService;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class NotificationController {

    private final INotificationService notificationService;
    private final JwtUtils jwtUtils;

    // ✅ Récupérer les notifications à partir du token (via Header Authorization)
    @GetMapping("/page")
    @PreAuthorize("hasRole('ROLE_REFUGEE')")
    public ResponseEntity<List<NotificationDTO>> getNotifications(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "").trim();
        Long userId = jwtUtils.getUserIdFromJwtToken(token);
        return ResponseEntity.ok(notificationService.getNotificationsForSubscriber(userId));
    }
}
