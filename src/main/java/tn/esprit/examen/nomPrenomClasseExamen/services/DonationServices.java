package tn.esprit.examen.nomPrenomClasseExamen.services;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
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
import tn.esprit.examen.nomPrenomClasseExamen.jwt.JwtUtils;

import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class DonationServices {

    private final DonationRepository donationRepository;
    private final CagnotteEnligneRepository cagnotteRepository;
    private final AssociationRepository associationRepository;

    private final JwtUtils jwtUtils; // Inject JwtUtils to extract the User ID from JWT

    private static final Logger logger = LoggerFactory.getLogger(DonationServices.class);


    // Fetch donations by association ID
    public List<Donation> getDonationsByAssociationIdFromToken(String jwtToken) {
        try {
            if (jwtToken == null || jwtToken.trim().isEmpty()) {
                throw new RuntimeException("JWT token cannot be null or empty");
            }

            // Extract the user ID from the JWT token
            Long userId = jwtUtils.getUserIdFromJwtToken(jwtToken);
            logger.info("🔍 User ID extracted from JWT: {}", userId);

            // Find the association by the user ID
            Association association = associationRepository.findBySubscriberIdUser(userId)
                    .orElseThrow(() -> new RuntimeException("Association not found for user ID: " + userId));

            // Fetch donations by association ID
            return donationRepository.findByAssociationDonationIdAssociation(association.getIdAssociation());

        } catch (Exception e) {
            logger.error("❌ Error occurred while fetching donations: {}", e.getMessage(), e);
            throw new RuntimeException("Error while fetching donations: " + e.getMessage());
        }
    }
    // Get all donations
    public List<DonationDto> getAllDonations() {
        List<Donation> donations = donationRepository.findAllWithCagnottes();
        return donations.stream()
                .map(DonationDto::fromDonation)
                .collect(Collectors.toList());
    }

    public CagnotteEnligne getCagnotteEnLigneByDonationId(Long donationId) {
        logger.info("🔍 Searching for CagnotteEnligne linked to Donation ID: {}", donationId);

        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new RuntimeException("Donation with ID " + donationId + " not found"));

        CagnotteEnligne cagnotte = donation.getCagnotteenligne();

        if (cagnotte == null) {
            throw new RuntimeException("No CagnotteEnligne found for Donation ID " + donationId);
        }

        return cagnotte;
    }


    // Get donation by ID
    public Donation getDonationById(Long id) {
        logger.info("🔍 Searching for donation with ID: {}", id);
        return donationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Donation with ID " + id + " not found"));
    }

    // Create a new donation
    public Donation createDonation(DonationDto donationDto, String jwtToken) {
        try {
            if (jwtToken == null || jwtToken.trim().isEmpty()) {
                throw new RuntimeException("JWT token cannot be null or empty");
            }

            // Extract User ID from JWT token
            Long userId = jwtUtils.getUserIdFromJwtToken(jwtToken);
            logger.info("🔍 User ID extracted from JWT: {}", userId);

            // Now you can use the User ID to fetch the Association (if applicable)
            Association association = associationRepository.findBySubscriberIdUser(userId)
                    .orElseThrow(() -> new RuntimeException("Association not found for user ID: " + userId));

            // Convert DTO → Entity
            Donation donation = new Donation();
            donation.setTitre(donationDto.getTitre());
            donation.setDescription(donationDto.getDescription());
            donation.setQuantiteDemandee(donationDto.getQuantiteDemandee());
            donation.setQuantiteDonnee(donationDto.getQuantiteDonnee());
            donation.setLastUpdated(donationDto.getLastUpdated());
            donation.setDonationType(donationDto.getDonationType());
            donation.setQuantiteExcedentaire(donationDto.getQuantiteExcedentaire());
            donation.setAssociationDonation(association);  // Associate with the correct user’s association
            donation.setCagnotteenligne(donationDto.getCagnotteenligne());

            // Save the donation
            Donation savedDonation = donationRepository.save(donation);
            logger.info("✅ Donation successfully created with ID: {}", savedDonation.getIdDonation());
            return savedDonation;

        } catch (Exception e) {
            logger.error("❌ Error occurred while creating donation: {}", e.getMessage(), e);
            throw new RuntimeException("Error while creating donation: " + e.getMessage());
        }
    }

    // Update an existing donation
    // Update an existing donation
// Update an existing donation
    @Transactional
    public Donation updateDonation(Long id, DonationDto donationDto, String jwtToken) {
        try {
            logger.info("🔄 Updating donation with ID: {}", id);

            // 1. Fetch the existing donation with its cagnotte
            Donation donation = donationRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Donation not found with ID: " + id));

            // 2. Update basic donation fields
            donation.setTitre(donationDto.getTitre());
            donation.setDescription(donationDto.getDescription());
            donation.setQuantiteDemandee(donationDto.getQuantiteDemandee());
            donation.setQuantiteDonnee(donationDto.getQuantiteDonnee());
            donation.setLastUpdated(LocalDateTime.now()); // Always use current time for update
            donation.setDonationType(donationDto.getDonationType());
            donation.setQuantiteExcedentaire(donationDto.getQuantiteExcedentaire());

            // 3. Handle cagnotte update
            if (donationDto.getCagnotteenligne() != null) {
                CagnotteEnligne cagnotteDto = donationDto.getCagnotteenligne();

                if (donation.getCagnotteenligne() != null) {
                    // Update existing cagnotte
                    CagnotteEnligne existingCagnotte = donation.getCagnotteenligne();
                    existingCagnotte.setTitle(cagnotteDto.getTitle());
                    existingCagnotte.setDescription(cagnotteDto.getDescription());
                    existingCagnotte.setGoalAmount(cagnotteDto.getGoalAmount());
                    existingCagnotte.setCurrentAmount(cagnotteDto.getCurrentAmount());
                    // Update other fields as needed
                } else {
                    // Create new cagnotte if none exists
                    CagnotteEnligne newCagnotte = new CagnotteEnligne();
                    newCagnotte.setTitle(cagnotteDto.getTitle());
                    newCagnotte.setDescription(cagnotteDto.getDescription());
                    newCagnotte.setGoalAmount(cagnotteDto.getGoalAmount());
                    newCagnotte.setCurrentAmount(cagnotteDto.getCurrentAmount());
                    // Set other fields as needed

                    // Set the relationship
                    donation.setCagnotteenligne(newCagnotte);
                    newCagnotte.setDonation(donation); // If you have bidirectional relationship
                }
            } else {
                // If cagnotte is null in DTO, remove existing one
                if (donation.getCagnotteenligne() != null) {
                    CagnotteEnligne cagnotteToRemove = donation.getCagnotteenligne();
                    donation.setCagnotteenligne(null);
                    cagnotteRepository.delete(cagnotteToRemove); // Only if you want to delete it
                }
            }

            // 4. Verify and set association
            if (jwtToken == null || jwtToken.trim().isEmpty()) {
                throw new RuntimeException("JWT token cannot be null or empty");
            }
            Long userId = jwtUtils.getUserIdFromJwtToken(jwtToken);
            logger.info("🔍 User ID extracted from JWT: {}", userId);

            Association association = associationRepository.findBySubscriberIdUser(userId)
                    .orElseThrow(() -> new RuntimeException("Association not found for User ID: " + userId));
            donation.setAssociationDonation(association);

            // 5. Save everything
            Donation updatedDonation = donationRepository.save(donation);
            logger.info("✅ Donation successfully updated with ID: {}", updatedDonation.getIdDonation());

            return updatedDonation;

        } catch (Exception e) {
            logger.error("❌ Error occurred while updating donation: {}", e.getMessage(), e);
            throw new RuntimeException("Error while updating donation: " + e.getMessage());
        }
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
