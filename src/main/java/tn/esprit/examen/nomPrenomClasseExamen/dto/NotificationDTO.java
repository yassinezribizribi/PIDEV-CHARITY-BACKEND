package tn.esprit.examen.nomPrenomClasseExamen.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Date;

@Data
@Builder
public class NotificationDTO {
    private Long idNotification;
    private String message;
    private Date createdAt;
    private boolean read;
    private String type;
    private String actionUrl;
    private Long subscriberId;
    private Long senderId;
    private Long healthcareId;
    private Long associationId;
}