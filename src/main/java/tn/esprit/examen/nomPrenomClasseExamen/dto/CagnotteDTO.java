package tn.esprit.examen.nomPrenomClasseExamen.dto;

import lombok.*;
import tn.esprit.examen.nomPrenomClasseExamen.entities.CagnotteEnligne;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Paiement;

import java.io.Serializable;
import java.util.Set;

/**
 * DTO for {@link tn.esprit.examen.nomPrenomClasseExamen.entities.CagnotteEnligne}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CagnotteDTO implements Serializable {
    private Long idCagnotte;
    private String title;
    private String description;
    private float goalAmount;
    private float currentAmount;
    private Set<Paiement> paiements; // Assuming you have a Set of Paiement in CagnotteEnligne

    // Convert DTO to Entity
    public CagnotteEnligne toCagnotteEnligne() {
        CagnotteEnligne cagnotteEnligne = new CagnotteEnligne();
        cagnotteEnligne.setIdCagnotte(this.idCagnotte);
        cagnotteEnligne.setTitle(this.title);
        cagnotteEnligne.setDescription(this.description);
        cagnotteEnligne.setGoalAmount(this.goalAmount);
        cagnotteEnligne.setCurrentAmount(this.currentAmount);
        cagnotteEnligne.setPaiements(this.paiements);
        return cagnotteEnligne;
    }

    // Convert Entity to DTO
    public static CagnotteDTO fromCagnotteEnligne(CagnotteEnligne cagnotteEnligne) {
        return new CagnotteDTO(
                cagnotteEnligne.getIdCagnotte(),
                cagnotteEnligne.getTitle(),
                cagnotteEnligne.getDescription(),
                cagnotteEnligne.getGoalAmount(),
                cagnotteEnligne.getCurrentAmount(),
                cagnotteEnligne.getPaiements()
        );
    }
}
