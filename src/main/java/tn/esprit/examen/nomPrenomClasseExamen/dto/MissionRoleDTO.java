package tn.esprit.examen.nomPrenomClasseExamen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.examen.nomPrenomClasseExamen.entities.MissionRole;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionRoleDTO implements Serializable {

    private Long idMissionRole;       // ID (adapted to match 'id' in entity)
    private Long missionId;            // Only store Mission ID

    private String roleName;           // Role name (e.g., Doctor, Nurse...)
    private int numberNeeded;          // How many volunteers needed
    private int numberAccepted;        // How many accepted
    private boolean requiresValidation;// Does it require special validation?

    // 🔹 DTO -> Entity
    public MissionRole toMissionRole() {
        MissionRole missionRole = new MissionRole();
        missionRole.setId(this.idMissionRole);
        missionRole.setRoleName(this.roleName);
        missionRole.setNumberNeeded(this.numberNeeded);
        missionRole.setNumberAccepted(this.numberAccepted);
        missionRole.setRequiresValidation(this.requiresValidation);
        // Mission will be set manually later if needed (to avoid incomplete entity loading)
        return missionRole;
    }

    // 🔹 Entity -> DTO
    public static MissionRoleDTO fromMissionRole(MissionRole missionRole) {
        return new MissionRoleDTO(
                missionRole.getId(),
                missionRole.getMission() != null ? missionRole.getMission().getIdMission() : null,
                missionRole.getRoleName(),
                missionRole.getNumberNeeded(),
                missionRole.getNumberAccepted(),
                missionRole.isRequiresValidation()
        );
    }
}