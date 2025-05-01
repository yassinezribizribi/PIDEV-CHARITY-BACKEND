package tn.esprit.examen.nomPrenomClasseExamen.dto;

import lombok.*;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Mission;
import tn.esprit.examen.nomPrenomClasseExamen.entities.MissionStatus;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MissionDTO implements Serializable {

    private Long idMission;
    private String description;
    private String location;
    private Date startDate;
    private Date endDate;
    private int volunteerCount;
    private MissionStatus status;

    private Long crisisId;       // Stocker uniquement l'ID de Crisis
    private Long associationId;  // Stocker uniquement l'ID de l'Association
    private List<String> tags;  // Add this for tags

    // 🔹 Convert DTO to Entity
    public Mission toMission() {
        Mission mission = new Mission();
        mission.setIdMission(this.idMission);
        mission.setDescription(this.description);
        mission.setLocation(this.location);
        mission.setStartDate(this.startDate);
        mission.setEndDate(this.endDate);
        mission.setVolunteerCount(this.volunteerCount);
        mission.setStatus(this.status);
        mission.setTags(this.tags);

        return mission;
    }

    // 🔹 Convert Entity to DTO
    public static MissionDTO fromMission(Mission mission) {
        return new MissionDTO(
                mission.getIdMission(),
                mission.getDescription(),
                mission.getLocation(),
                mission.getStartDate(),
                mission.getEndDate(),
                mission.getVolunteerCount(),
                mission.getStatus(),
                mission.getCrisis() != null ? mission.getCrisis().getIdCrisis() : null,
                mission.getAssociations() != null && !mission.getAssociations().isEmpty()
                        ? mission.getAssociations().iterator().next().getIdAssociation()
                        : null,
                mission.getTags() // Add this line to include tags in the DTO

        );
    }
}
