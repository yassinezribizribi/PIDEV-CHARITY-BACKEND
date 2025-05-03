package tn.esprit.examen.nomPrenomClasseExamen.services;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.CagnotteEnligneRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.DonationRepository;
import tn.esprit.examen.nomPrenomClasseExamen.entities.CagnotteEnligne;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Donation; // Make sure to import Donation if used

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class CagnotteServices {


    private static final Logger logger = LoggerFactory.getLogger(CagnotteServices.class);
    DonationRepository donationRepository;
    CagnotteEnligneRepository cagnotteEnligneRepository;
    // If you have a Donation repository, inject it as well

    public List<CagnotteEnligne> getAllCagnottes() {
        log.info("📢 Récupération de toutes les cagnottes en ligne...");
        return cagnotteEnligneRepository.findAll();
    }

    public CagnotteEnligne getCagnotteById(Long id) {
        log.info("Recherche de la cagnotte avec l'ID: {}", id);
        return cagnotteEnligneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cagnotte avec l'ID " + id + " introuvable"));
    }
    public CagnotteEnligne createCagnotteEtAffecterADonation(Long idDonation, CagnotteEnligne cagnotte) {
        // Find the existing donation
        Donation existingDonation = donationRepository.findById(idDonation)
                .orElseThrow(() -> new RuntimeException("Donation not found with ID: " + idDonation));

        // Save the CagnotteEnligne
        CagnotteEnligne savedCagnotte = cagnotteEnligneRepository.save(cagnotte);

        // Associate the saved cagnotte with the existing donation
        existingDonation.setCagnotteenligne(savedCagnotte);

        // Save the updated donation
        donationRepository.save(existingDonation);

        return savedCagnotte; // Return the saved cagnotte directly
    }




    public CagnotteEnligne updateCagnotte(Long id, CagnotteEnligne updatedCagnotte) {
        log.info("🔄 Mise à jour de la cagnotte avec ID: {}", id);
        CagnotteEnligne cagnotteEnligne = getCagnotteById(id);

        cagnotteEnligne.setTitle(updatedCagnotte.getTitle());
        cagnotteEnligne.setDescription(updatedCagnotte.getDescription());
        cagnotteEnligne.setGoalAmount(updatedCagnotte.getGoalAmount());
        cagnotteEnligne.setCurrentAmount(updatedCagnotte.getCurrentAmount());

        CagnotteEnligne updated = cagnotteEnligneRepository.save(cagnotteEnligne);
        log.info("✅ Cagnotte mise à jour avec succès: {}", updated);
        return updated;
    }

    public void deleteCagnotte(Long id) {
        log.info("🗑 Suppression de la cagnotte avec ID: {}", id);
        cagnotteEnligneRepository.deleteById(id);
        log.info("✅ Cagnotte supprimée avec succès !");
    }
}
