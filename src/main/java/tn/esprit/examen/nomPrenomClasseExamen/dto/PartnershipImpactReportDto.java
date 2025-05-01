package tn.esprit.examen.nomPrenomClasseExamen.dto;

import lombok.*;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Association;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class PartnershipImpactReportDto {
    private Long associationId;
    private int jointMissionsCompleted;
    private int volunteersShared;
    private double efficiencyImprovement;
    private int partnershipScore;
    private Association.PartnershipTier tier;
    private List<String> recommendations;
}