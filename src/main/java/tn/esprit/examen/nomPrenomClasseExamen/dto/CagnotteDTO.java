package tn.esprit.examen.nomPrenomClasseExamen.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;
import tn.esprit.examen.nomPrenomClasseExamen.entities.CagnotteEnligne;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Paiement;

import java.io.Serializable;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DTO for {@link tn.esprit.examen.nomPrenomClasseExamen.entities.CagnotteEnligne}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CagnotteDTO implements Serializable {
    private Long idCagnotte;

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 100)
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 500)
    private String description;

    @Positive(message = "Goal amount must be positive")
    private float goalAmount;

    @PositiveOrZero(message = "Current amount must be zero or positive")
    private float currentAmount;

    private Set<@Valid PaiementDTO> paiements;
    public CagnotteEnligne toCagnotteEnligne() {
        CagnotteEnligne cagnotteEnligne = new CagnotteEnligne();
        cagnotteEnligne.setIdCagnotte(this.idCagnotte);
        cagnotteEnligne.setTitle(this.title);
        cagnotteEnligne.setDescription(this.description);
        cagnotteEnligne.setGoalAmount(this.goalAmount);
        cagnotteEnligne.setCurrentAmount(this.currentAmount);

        if (this.paiements != null) {
            Set<Paiement> paiementsSet = this.paiements.stream()
                    .map(PaiementDTO::toPaiement)
                    .collect(Collectors.toSet());
            cagnotteEnligne.setPaiements(paiementsSet);
        }

        return cagnotteEnligne;
    }

    public static CagnotteDTO fromCagnotteEnligne(CagnotteEnligne cagnotteEnligne) {
        Set<PaiementDTO> paiementsDTO = null;
        if (cagnotteEnligne.getPaiements() != null) {
            paiementsDTO = cagnotteEnligne.getPaiements().stream()
                    .map(PaiementDTO::fromPaiement)
                    .collect(Collectors.toSet());
        }

        return new CagnotteDTO(
                cagnotteEnligne.getIdCagnotte(),
                cagnotteEnligne.getTitle(),
                cagnotteEnligne.getDescription(),
                cagnotteEnligne.getGoalAmount(),
                cagnotteEnligne.getCurrentAmount(),
                paiementsDTO
        );
    }
}
