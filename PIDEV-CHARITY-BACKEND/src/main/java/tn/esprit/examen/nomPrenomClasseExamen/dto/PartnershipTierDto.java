package tn.esprit.examen.nomPrenomClasseExamen.dto;

import lombok.*;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Association;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PartnershipTierDto {
    private Association.PartnershipTier tier;
    private int score;
    private int nextTierThreshold;
    private int progress;
    private int maxPartners;
    private int currentPartners;
}