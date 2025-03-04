package tn.esprit.examen.nomPrenomClasseExamen.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.Date;

@Getter
@Setter
public class NotificationDTO {
    private Long idNotification;
    private String message;
    private Date createdAt;
    private Long subscriberId; // ✅ ID du patient concerné
}
