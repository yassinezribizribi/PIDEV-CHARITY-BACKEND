package tn.esprit.examen.nomPrenomClasseExamen.dto;

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
    private Date datePaiement;
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
