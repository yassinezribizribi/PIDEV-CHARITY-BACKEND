package tn.esprit.examen.nomPrenomClasseExamen.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Paiement;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaiementDTO {
    private Long idPaiement;

    @NotNull(message = "Payment date is required")
    @PastOrPresent(message = "Payment date cannot be in the future")
    private Date datePaiement;

    @Positive(message = "Amount must be greater than zero")
    private int montant;

    public Paiement toPaiement() {
        Paiement paiement = new Paiement();
        paiement.setIdPaiement(this.idPaiement);
        paiement.setDatePaiement(this.datePaiement);
        paiement.setMontant(this.montant);
        return paiement;
    }

    public static PaiementDTO fromPaiement(Paiement paiement) {
        return new PaiementDTO(
                paiement.getIdPaiement(),
                paiement.getDatePaiement(),
                paiement.getMontant()
        );
    }
}
