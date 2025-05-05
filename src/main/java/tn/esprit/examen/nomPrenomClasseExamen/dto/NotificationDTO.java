package tn.esprit.examen.nomPrenomClasseExamen.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;

@Data
public class NotificationDTO {
    private Long idNotification;
    private String message;
    private Date createdAt;
    private Long subscriberId;

    // Facultatifs pour Healthcare
    private String terminalDisease;
    private String treatmentPlan;
    private Date bookingDate;
    private String meetingUrl;
}
