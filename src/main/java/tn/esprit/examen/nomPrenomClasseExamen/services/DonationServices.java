package tn.esprit.examen.nomPrenomClasseExamen.services;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.CagnotteEnligneRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.DonationRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.AssociationRepository;
import tn.esprit.examen.nomPrenomClasseExamen.dto.DonationDto;
import tn.esprit.examen.nomPrenomClasseExamen.entities.CagnotteEnligne;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Donation;
import tn.esprit.examen.nomPrenomClasseExamen.entities.DonationType;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Association;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class DonationServices {

    private final DonationRepository donationRepository;
    private final CagnotteEnligneRepository cagnotteRepository;
    private final AssociationRepository associationRepository;
    private static final Logger logger = LoggerFactory.getLogger(DonationServices.class);

    // Get all donations
    public List<Donation> getAllDonations() {
        return donationRepository.findAll();
    }

    // Get donation by ID
    public Donation getDonationById(Long id) {
        logger.info("🔍 Searching for donation with ID: {}", id);
        return donationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Donation with ID " + id + " not found"));
    }

    // Create a new donation
    // Create a new donation
    public Donation createDonation(DonationDto donationDto) {
        try {
            logger.info("📝 Creating a new donation: {}", donationDto);

            // Convert DTO → Entity
            Donation donation = new Donation();

            // Set attributes using the setter methods from the DTO
            donation.setTitre(donationDto.getTitre());
            donation.setDescription(donationDto.getDescription());
            donation.setQuantiteDemandee(donationDto.getQuantiteDemandee());
            donation.setQuantiteDonnee(donationDto.getQuantiteDonnee());
            donation.setAvailability(donationDto.getAvailability());
            donation.setLastUpdated(donationDto.getLastUpdated());
            donation.setDonationType(donationDto.getDonationType());

            // Fetch and set CagnotteEnligne
            if (donationDto.getCagnotteId() != null) {
                CagnotteEnligne cagnotte = cagnotteRepository.findById(donationDto.getCagnotteId())
                        .orElseThrow(() -> new RuntimeException("Cagnotte not found with ID: " + donationDto.getCagnotteId()));
                donation.setCagnotteenligne(cagnotte);
            }

            // Fetch and set Association
            if (donationDto.getAssociationId() != null) {
                Association association = associationRepository.findById(donationDto.getAssociationId())
                        .orElseThrow(() -> new RuntimeException("Association not found with ID: " + donationDto.getAssociationId()));
                donation.setAssociationDonation(association);
            }

            // Save the donation
            Donation savedDonation = donationRepository.save(donation);

            logger.info("✅ Donation successfully created: {}", savedDonation);
            return savedDonation;
        } catch (Exception e) {
            logger.error("❌ Error while creating donation: {}", e.getMessage(), e);
            throw new RuntimeException("Error while creating donation: " + e.getMessage());
        }
    }

    // Update an existing donation
    public Donation updateDonation(Long id, DonationDto donationDto) {
        logger.info("🔄 Updating donation with ID: {}", id);
        Donation donation = getDonationById(id);

        // Update fields from DTO
        donation.setTitre(donationDto.getTitre());
        donation.setDescription(donationDto.getDescription());
        donation.setQuantiteDemandee(donationDto.getQuantiteDemandee());
        donation.setQuantiteDonnee(donationDto.getQuantiteDonnee());
        donation.setAvailability(donationDto.getAvailability());
        donation.setLastUpdated(donationDto.getLastUpdated());
        donation.setDonationType(donationDto.getDonationType());

        // Fetch and update CagnotteEnligne
        if (donationDto.getCagnotteId() != null) {
            CagnotteEnligne cagnotte = cagnotteRepository.findById(donationDto.getCagnotteId())
                    .orElseThrow(() -> new RuntimeException("Cagnotte not found with ID: " + donationDto.getCagnotteId()));
            donation.setCagnotteenligne(cagnotte);
        }

        // Fetch and update Association
        if (donationDto.getAssociationId() != null) {
            Association association = associationRepository.findById(donationDto.getAssociationId())
                    .orElseThrow(() -> new RuntimeException("Association not found with ID: " + donationDto.getAssociationId()));
            donation.setAssociationDonation(association);
        }

        // Save the updated donation
        Donation updatedDonation = donationRepository.save(donation);
        logger.info("✅ Donation successfully updated: {}", updatedDonation);
        return updatedDonation;
    }

    // Delete a donation
    public void deleteDonation(Long id) {
        logger.info("🗑 Deleting donation with ID: {}", id);
        donationRepository.deleteById(id);
        logger.info("✅ Donation successfully deleted!");
    }

    // Find donations by type
    public List<DonationDto> findByDonationType(DonationType donationType) {
        List<Donation> donations = donationRepository.findByDonationType(donationType);
        return donations.stream()
                .map(DonationDto::fromDonation)
                .collect(Collectors.toList());
    }
}