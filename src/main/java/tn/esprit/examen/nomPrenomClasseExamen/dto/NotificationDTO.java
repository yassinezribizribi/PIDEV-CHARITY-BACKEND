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

    // 🔒 Type & Action
    private String type;
    private String actionUrl;

    // 📦 Relations
    private Long subscriberId;
    private Long senderId;
    private Long healthcareId;
    private Long associationId;

    // 🏥 Optionnel: pour les notifications liées à des soins de santé
    private String terminalDisease;
    private String treatmentPlan;
    private Date bookingDate;
    private String meetingUrl;
}
