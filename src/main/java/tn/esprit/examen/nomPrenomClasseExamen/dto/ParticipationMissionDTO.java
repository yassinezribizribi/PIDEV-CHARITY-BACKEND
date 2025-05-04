package tn.esprit.examen.nomPrenomClasseExamen.dto;

import lombok.*;
import tn.esprit.examen.nomPrenomClasseExamen.entities.MissionStatus;

import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ParticipationMissionDTO {
    private Long participationId;

    // Mission info
    private String missionTitle;
    private String missionDescription;
    private String location;
    private Date startDate;
    private Date endDate;
    private MissionStatus status;

    // Role info
    private String roleName;
    private int numberNeeded;
    private int numberAccepted;
    private boolean requiresValidation;
}
