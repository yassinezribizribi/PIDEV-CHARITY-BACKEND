package tn.esprit.examen.nomPrenomClasseExamen.dto;

import lombok.Data;
import java.util.Date;

@Data
public class HealthcareDTO {
    private Long id;
    private String history;
    private String treatmentPlan;
    private String terminalDisease;
    private Date bookingDate;
    private String meetingUrl;
    private Long subscriberId;
    private Long doctorId;
    private String status;
}
