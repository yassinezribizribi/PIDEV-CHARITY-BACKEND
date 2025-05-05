package tn.esprit.examen.nomPrenomClasseExamen.dto;

import lombok.*;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Mission;
import tn.esprit.examen.nomPrenomClasseExamen.entities.MissionStatus;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Association;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MissionDTO implements Serializable {

    private Long idMission;
    private String title;
    private String description;
    private String location;
    private Date startDate;
    private Date endDate;
    private int volunteerCount;
    private MissionStatus status;
    private Long crisisId;
    private Set<Long> associationIds;
    private List<MissionRoleDTO> missionRoles;
    private String missionLogoPath; // ✅ NEW FIELD

    // DTO -> Entity
    public Mission toMission() {
        Mission mission = new Mission();
        mission.setIdMission(this.idMission);
        mission.setTitle(this.title);
        mission.setDescription(this.description);
        mission.setLocation(this.location);
        mission.setStartDate(this.startDate);
        mission.setEndDate(this.endDate);
        mission.setVolunteerCount(this.volunteerCount);
        mission.setStatus(this.status);
        mission.setMissionLogoPath(this.missionLogoPath); // ✅ include it

        return mission;
    }

    // Entity -> DTO (FIXED to include missionRoles)
    public static MissionDTO fromMission(Mission mission) {
        MissionDTO missionDTO = new MissionDTO();
        missionDTO.setIdMission(mission.getIdMission());
        missionDTO.setTitle(mission.getTitle());
        missionDTO.setDescription(mission.getDescription());
        missionDTO.setLocation(mission.getLocation());
        missionDTO.setStartDate(mission.getStartDate());
        missionDTO.setEndDate(mission.getEndDate());
        missionDTO.setVolunteerCount(mission.getVolunteerCount());
        missionDTO.setStatus(mission.getStatus());
        missionDTO.setMissionLogoPath(mission.getMissionLogoPath());

        missionDTO.setCrisisId(mission.getCrisis() != null ? mission.getCrisis().getIdCrisis() : null);
        // ✅ Convert associated associations to their IDs
        if (mission.getAssociations() != null) {
            Set<Long> ids = mission.getAssociations().stream()
                    .map(Association::getIdAssociation)
                    .collect(Collectors.toSet());
            missionDTO.setAssociationIds(ids);
        }

        // ✅ Include missionRoles as DTOs
        if (mission.getMissionRoles() != null) {
            missionDTO.setMissionRoles(
                    mission.getMissionRoles().stream()
                            .map(MissionRoleDTO::fromMissionRole)
                            .collect(Collectors.toList())
            );
        }

        return missionDTO;
    }
}