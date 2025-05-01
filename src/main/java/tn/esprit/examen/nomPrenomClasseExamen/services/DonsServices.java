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

@Service
@AllArgsConstructor
public class DonsServices {
    private final DonsRepository donsRepository;
    private final DonationRepository donationRepository;
    private final SubscriberRepository subscriberRepository;
    private static final Logger logger = LoggerFactory.getLogger(DonsServices.class);

    public DonsDTO contributeToDonation(Long idDonation, DonsDTO donsDto) {
        Donation donation = donationRepository.findById(idDonation)
                .orElseThrow(() -> new RuntimeException("❌ Donation not found!"));

        // 🔹 Create a new Dons entity
        Dons dons = new Dons();
        dons.setNomDoneur(donsDto.getNomDoneur());
        dons.setPrenomDoneur(donsDto.getPrenomDoneur());
        dons.setQuantite(donsDto.getQuantite());
        dons.setDonation(donation);
        dons.setDonationDate(LocalDateTime.now());

        // Link the donor (if provided)
        if (donsDto.getSubscriberId() != null) {
            Subscriber subscriber = subscriberRepository.findById(donsDto.getSubscriberId())
                    .orElseThrow(() -> new RuntimeException("❌ Subscriber not found!"));
            dons.setSubscriberDons(subscriber);
        }

        // 🔹 Update the donation totals
        donation.addDonation(donsDto.getQuantite());

        // Save both Dons and Donation updates
        donsRepository.save(dons);
        donationRepository.save(donation);

        logger.info("✅ Contribution added: {} units from {} {}",
                dons.getQuantite(), dons.getNomDoneur(), dons.getPrenomDoneur());

        return DonsDTO.fromDons(dons);
    }
}
