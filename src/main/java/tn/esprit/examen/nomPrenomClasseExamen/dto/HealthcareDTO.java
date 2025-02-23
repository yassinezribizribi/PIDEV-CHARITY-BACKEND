package tn.esprit.examen.nomPrenomClasseExamen.dto;

import lombok.Data;
import java.util.Date;

@Data
public class HealthcareDTO {
    private String history;
    private String treatmentPlan;
    private String terminalDisease;
    private Date bookingDate;
    private Long subscriberId;  // ID du Subscriber
}
