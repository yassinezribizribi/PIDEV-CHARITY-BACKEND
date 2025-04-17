package tn.esprit.examen.nomPrenomClasseExamen.dto;

import lombok.*;
import tn.esprit.examen.nomPrenomClasseExamen.entities.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import tn.esprit.examen.nomPrenomClasseExamen.dto.CagnotteDTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DonationDto implements Serializable {

    private Long idDonation;
    private String titre;
    private String description;
    private int quantiteDemandee;
    private int quantiteDonnee;
    //    private Boolean availability;
    private LocalDateTime lastUpdated;
    private DonationType donationType;
    private int quantiteExcedentaire;

    private CagnotteEnligne cagnotteenligne;

    // 🔹 Convert DTO to Entity
    public Donation toDonation() {
        Donation donation = new Donation();
        donation.setIdDonation(this.idDonation);
        donation.setTitre(this.titre);
        donation.setDescription(this.description);
        donation.setQuantiteDemandee(this.quantiteDemandee);
        donation.setQuantiteDonnee(this.quantiteDonnee);
//        donation.setAvailability(this.availability);
        donation.setLastUpdated(this.lastUpdated);
        donation.setDonationType(this.donationType);
        donation.setQuantiteExcedentaire(this.quantiteExcedentaire);
        donation.setCagnotteenligne(this.cagnotteenligne);

        // Association will be handled in the service based on the user from the token
        return donation;
    }

    // 🔹 Convert Entity to DTO
    public static DonationDto fromDonation(Donation donation) {
        return new DonationDto(
                donation.getIdDonation(),
                donation.getTitre(),
                donation.getDescription(),
                donation.getQuantiteDemandee(),
                donation.getQuantiteDonnee(),
//                donation.getAvailability(),
                donation.getLastUpdated(),
                donation.getDonationType(),
                donation.getQuantiteExcedentaire(),
                donation.getCagnotteenligne()
        );
    }
}
