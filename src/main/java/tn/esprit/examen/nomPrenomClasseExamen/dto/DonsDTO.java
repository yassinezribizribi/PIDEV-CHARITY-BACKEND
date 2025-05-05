package tn.esprit.examen.nomPrenomClasseExamen.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Donation;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Dons;
import tn.esprit.examen.nomPrenomClasseExamen.entities.DonsDeliveryMethod;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Subscriber;
import tn.esprit.examen.nomPrenomClasseExamen.validation.ValidDonorAddress;
import tn.esprit.examen.nomPrenomClasseExamen.validation.ValidScheduledDate;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ValidDonorAddress
@ValidScheduledDate

public class DonsDTO implements Serializable {

    private Long idDons;

    //    @NotBlank(message = "First name is required")
//    @Size(min = 2, max = 50)
    private String nomDoneur;

    //    @NotBlank(message = "Last name is required")
//    @Size(min = 2, max = 50)
    private String prenomDoneur;

    @Email(message = "Invalid email format")
//    @NotBlank(message = "Email is required")
    private String donorEmail;

    @Positive(message = "Quantity must be greater than zero")
    private int quantite;

    private LocalDateTime donationDate;


    @NotNull(message = "Scheduled date is required")
    private LocalDateTime scheduledDateTime;

    private boolean associationValidated;

    @Size(max = 500, message = "Note cannot exceed 500 characters")
    private String donorNote;

    @NotNull(message = "Delivery method is required")
    private DonsDeliveryMethod deliveryMethod;

    @Size(max = 255)
    private String donorAddress;


    //    @NotNull(message = "Donation ID is required")
    private Long donationId;

    //    @NotNull(message = "Subscriber ID is required")
    private Long subscriberId;

    // 🔁 Convert DTO to Entity
    public Dons toDons() {
        Dons dons = new Dons();
        dons.setIdDons(this.idDons);
        dons.setNomDoneur(this.nomDoneur);
        dons.setPrenomDoneur(this.prenomDoneur);
        dons.setDonorEmail(this.donorEmail); // ✅ Fixed missing field
        dons.setQuantite(this.quantite);
        dons.setDonationDate(this.donationDate);
        dons.setScheduledDateTime(this.scheduledDateTime);
        dons.setAssociationValidated(this.associationValidated);
        dons.setDonorNote(this.donorNote);
        dons.setDeliveryMethod(this.deliveryMethod);
        dons.setDonorAddress(this.donorAddress);

        if (this.donationId != null) {
            Donation donation = new Donation();
            donation.setIdDonation(this.donationId);
            dons.setDonation(donation);
        }

        if (this.subscriberId != null) {
            Subscriber subscriber = new Subscriber();
            subscriber.setIdUser(this.subscriberId);
            dons.setSubscriberDons(subscriber);
        }

        return dons;
    }

    // 🔁 Convert Entity to DTO
    public static DonsDTO fromDons(Dons dons) {
        return new DonsDTO(
                dons.getIdDons(),
                dons.getNomDoneur(),
                dons.getPrenomDoneur(),
                dons.getDonorEmail(), // ✅ Fixed missing field
                dons.getQuantite(),
                dons.getDonationDate(),
                dons.getScheduledDateTime(),
                dons.isAssociationValidated(),
                dons.getDonorNote(),
                dons.getDeliveryMethod(),
                dons.getDonorAddress(),
                dons.getDonation() != null ? dons.getDonation().getIdDonation() : null,
                dons.getSubscriberDons() != null ? dons.getSubscriberDons().getIdUser() : null
        );
    }
}
