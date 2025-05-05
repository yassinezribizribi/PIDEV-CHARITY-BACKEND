package tn.esprit.examen.nomPrenomClasseExamen.services;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.DonationRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.DonsRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.SubscriberRepository;
import tn.esprit.examen.nomPrenomClasseExamen.dto.DonsDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Donation;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Dons;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Subscriber;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class DonsServices {
    private final DonsRepository donsRepository;
    private final DonationRepository donationRepository;
    private final SubscriberRepository subscriberRepository;
    private static final Logger logger = LoggerFactory.getLogger(DonsServices.class);

    public Dons contributeToDonation(Long idDonation, Dons dons) {
        // Fetch the donation to associate with the contribution
        Donation donation = donationRepository.findById(idDonation)
                .orElseThrow(() -> new RuntimeException("❌ Donation not found!"));

        // Set the necessary properties on the Dons entity
        dons.setDonation(donation);
        dons.setDonationDate(LocalDateTime.now());
        dons.setAssociationValidated(false); // Not validated yet

        // If there's a subscriber ID, find the subscriber and associate them with the Dons entity
        if (dons.getSubscriberDons() != null && dons.getSubscriberDons().getIdUser() != null) {
            Subscriber subscriber = subscriberRepository.findById(dons.getSubscriberDons().getIdUser())
                    .orElseThrow(() -> new RuntimeException("❌ Subscriber not found!"));
            dons.setSubscriberDons(subscriber);
        }

        // Save the Dons entity
        donsRepository.save(dons);

        // Log the action
        logger.info("🕒 Donor scheduled a contribution. Waiting for association validation.");

        return dons;
    }

    public DonsDTO validateDonsByAssociation(Long idDons) {
        Dons dons = donsRepository.findById(idDons)
                .orElseThrow(() -> new RuntimeException("❌ Dons not found!"));

        if (dons.isAssociationValidated()) {
            throw new IllegalStateException("⚠️ This donation is already validated.");
        }

        Donation donation = dons.getDonation();

        // ✅ Update the donation's total now that it's confirmed
        donation.addDonation(dons.getQuantite());
        dons.setAssociationValidated(true);
        dons.setDonationDate(LocalDateTime.now()); // Optional: update the validation date

        // Save both
        donationRepository.save(donation);
        donsRepository.save(dons);

        logger.info("✅ Association validated {} units from {} {}", dons.getQuantite(), dons.getNomDoneur(), dons.getPrenomDoneur());

        return DonsDTO.fromDons(dons);
    }

    public List<DonsDTO> getDonsByDonationId(Long donationId) {
        List<Dons> donsList = donsRepository.findByDonation_IdDonation(donationId);
        return donsList.stream()
                .map(DonsDTO::fromDons)
                .collect(Collectors.toList());
    }



}
