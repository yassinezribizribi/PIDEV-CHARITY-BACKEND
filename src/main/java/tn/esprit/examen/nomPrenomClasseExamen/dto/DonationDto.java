package tn.esprit.examen.nomPrenomClasseExamen.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import tn.esprit.examen.nomPrenomClasseExamen.entities.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DonationDto implements Serializable {

    private Long idDonation;

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String titre;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
    private String description;

    @Min(value = 1, message = "Requested quantity must be at least 1")
    private int quantiteDemandee;

    @Min(value = 0, message = "Donated quantity cannot be negative")
    private int quantiteDonnee;

    @NotNull(message = "Donation type is required")
    private DonationType donationType;

    @Min(value = 0, message = "Excess quantity cannot be negative")
    private int quantiteExcedentaire;

    @Size(max = 255, message = "Image URL is too long")
    private String imageUrl;

    private LocalDate startDate;

    @FutureOrPresent(message = "End date must be today or in the future")
    private Date endDate;

    private CagnotteEnligne cagnotteenligne;

    @NotNull(message = "Donation status is required")
    private DonationStatus status;

    @Min(value = 1, message = "Priority must be at least 1")
    @Max(value = 4, message = "Priority must not be greater than 4")
    private int priority;
    // DTO -> Entity
    public Donation toDonation() {
        Donation donation = new Donation();
        donation.setIdDonation(this.idDonation);
        donation.setTitre(this.titre);
        donation.setDescription(this.description);
        donation.setQuantiteDemandee(this.quantiteDemandee);
        donation.setQuantiteDonnee(this.quantiteDonnee);
//        donation.setLastUpdated(this.lastUpdated);
        donation.setDonationType(this.donationType);
        donation.setQuantiteExcedentaire(this.quantiteExcedentaire);
        donation.setImageUrl(this.imageUrl);
        // startDate is set automatically by @PrePersist
        donation.setEndDate(this.endDate);
        donation.setCagnotteenligne(this.cagnotteenligne);
        donation.setStatus(this.status);
        donation.setPriority(this.priority); // ✅ set the priority

        return donation;
    }

    // Entity -> DTO
    public static DonationDto fromDonation(Donation donation) {
        return new DonationDto(
                donation.getIdDonation(),
                donation.getTitre(),
                donation.getDescription(),
                donation.getQuantiteDemandee(),
                donation.getQuantiteDonnee(),
//                donation.getLastUpdated(),
                donation.getDonationType(),
                donation.getQuantiteExcedentaire(),
                donation.getImageUrl(),
                donation.getStartDate(),
                donation.getEndDate(),
                donation.getCagnotteenligne(),
                donation.getStatus(),
                donation.getPriority() // ✅ include in DTO
        );
    }
}
