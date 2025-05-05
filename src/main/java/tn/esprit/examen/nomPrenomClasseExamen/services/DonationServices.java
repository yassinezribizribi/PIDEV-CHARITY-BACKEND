package tn.esprit.examen.nomPrenomClasseExamen.services;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.CagnotteEnligneRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.DonationRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.AssociationRepository;
import tn.esprit.examen.nomPrenomClasseExamen.dto.DonationDto;
import tn.esprit.examen.nomPrenomClasseExamen.dto.DonationExtensionSuggestionDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.*;
import tn.esprit.examen.nomPrenomClasseExamen.jwt.JwtUtils;

import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.ZoneId;


@Service
@AllArgsConstructor
public class DonationServices {

    private final DonationRepository donationRepository;
    private final CagnotteEnligneRepository cagnotteRepository;
    private final AssociationRepository associationRepository;

    private final JwtUtils jwtUtils; // Inject JwtUtils to extract the User ID from JWT

    private static final Logger logger = LoggerFactory.getLogger(DonationServices.class);

    //  Automatically expire old donations
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void expireOldDonations() {
        List<Donation> activeDonations = donationRepository.findByStatus(DonationStatus.ACTIVE);
        LocalDate today = LocalDate.now();

        for (Donation donation : activeDonations) {
            Date endDate = donation.getEndDate();
            if (endDate != null) {
                LocalDate endLocalDate = endDate.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();

                if (endLocalDate.isBefore(today)) {
                    donation.setStatus(DonationStatus.EXPIRED);
                    donationRepository.save(donation);
                    logger.info("📅 Donation with ID {} marked as EXPIRED", donation.getIdDonation());
                }
            }
        }
    }
    public DonationExtensionSuggestionDTO suggestDonationExtension(Long donationId) {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new RuntimeException("Donation not found"));

        CagnotteEnligne cagnotte = donation.getCagnotteenligne();

        // 🔸 Calculate average based on actual donation entries
        Set<Dons> dons = donation.getDons();
        Map<LocalDate, Integer> donsPerDay = new HashMap<>();
        if (dons != null) {
            for (Dons don : dons) {
                LocalDate date = don.getDonationDate().toLocalDate(); // Make sure you have this field
                donsPerDay.put(date, donsPerDay.getOrDefault(date, 0) + don.getQuantite());
            }
        }
        int totalQuantiteDonnee = dons != null ? dons.stream().mapToInt(Dons::getQuantite).sum() : 0;
        int activeDonationDays = donsPerDay.size();
        double donationAvgPerDay = activeDonationDays > 0 ? (double) totalQuantiteDonnee / activeDonationDays : 0;
        int quantiteDemandee = donation.getQuantiteDemandee();
        int donationRemaining = quantiteDemandee - totalQuantiteDonnee;
        long donationEstimatedDays = donationAvgPerDay > 0
                ? (long) Math.ceil(donationRemaining / donationAvgPerDay)
                : -1;

        // 🔸 Calculate average based on actual payments
        float cagnotteCollected = 0;
        float cagnotteGoal = 0;
        float cagnotteRemaining = 0;
        double cagnotteAvgPerDay = 0;
        long cagnotteEstimatedDays = -1;

        if (cagnotte != null && cagnotte.getPaiements() != null) {
            Map<LocalDate, Float> paymentsPerDay = new HashMap<>();
            for (Paiement paiement : cagnotte.getPaiements()) {
                LocalDate date = paiement.getDatePaiement()
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();                paymentsPerDay.put(date, paymentsPerDay.getOrDefault(date, 0f) + paiement.getMontant());
            }
            cagnotteCollected = cagnotte.getCurrentAmount();
            cagnotteGoal = cagnotte.getGoalAmount();
            cagnotteRemaining = cagnotteGoal - cagnotteCollected;
            int activePaymentDays = paymentsPerDay.size();
            cagnotteAvgPerDay = activePaymentDays > 0 ? cagnotteCollected / activePaymentDays : 0;
            cagnotteEstimatedDays = cagnotteAvgPerDay > 0
                    ? (long) Math.ceil(cagnotteRemaining / cagnotteAvgPerDay)
                    : -1;
        }

        boolean donationIncomplete = totalQuantiteDonnee < quantiteDemandee;
        boolean cagnotteIncomplete = cagnotte != null && cagnotteCollected < cagnotteGoal;

        long daysToExtend = 0;
        if (donationIncomplete || cagnotteIncomplete) {
            daysToExtend = Math.max(
                    donationIncomplete ? donationEstimatedDays : 0,
                    cagnotteIncomplete ? cagnotteEstimatedDays : 0
            );
        }

        DonationExtensionSuggestionDTO dto = new DonationExtensionSuggestionDTO();
        dto.setSuggestedDays(daysToExtend);
        dto.setMessage(daysToExtend > 0 ? "Extension recommended." : "No extension needed.");
        dto.setDonationProgress((totalQuantiteDonnee * 100.0) / quantiteDemandee);
        dto.setDonationDailyAvg(donationAvgPerDay);
        dto.setDonationRemaining(donationRemaining);
        dto.setDonationEstimatedDays(donationEstimatedDays);

        if (cagnotte != null) {
            dto.setCagnotteProgress((cagnotteCollected * 100.0) / cagnotteGoal);
            dto.setCagnotteDailyAvg(cagnotteAvgPerDay);
            dto.setCagnotteRemaining(cagnotteRemaining);
            dto.setCagnotteEstimatedDays(cagnotteEstimatedDays);
        }

        return dto;
    }


    public String applyDonationExtension(Long donationId, long daysToExtend) {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new RuntimeException("Donation not found"));

        if (donation.getEndDate() == null) {
            return "⚠️ Donation has no end date to extend.";
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(donation.getEndDate());
        calendar.add(Calendar.DAY_OF_MONTH, (int) daysToExtend);
        donation.setEndDate(calendar.getTime());

        donationRepository.save(donation);
        return "📅 End date extended by " + daysToExtend + " day(s).";
    }

    // Fetch donations by association ID
    public List<Donation> getDonationsByAssociationIdFromToken(String jwtToken) {
        try {
            if (jwtToken == null || jwtToken.trim().isEmpty()) {
                throw new RuntimeException("JWT token cannot be null or empty");
            }

            // 1. Extract User ID from JWT
            Long userId = jwtUtils.getUserIdFromJwtToken(jwtToken);
            logger.info("🔍 User ID extracted from JWT: {}", userId);

            // 2. Find the Association for the User
            Association association = associationRepository.findBySubscriberIdUser(userId)
                    .orElseThrow(() -> new RuntimeException("Association not found for user ID: " + userId));

            // 3. Fetch all donations of the association
            List<Donation> allDonations = donationRepository.findByAssociationDonationIdAssociation(association.getIdAssociation())
                    .stream()
                    .filter(donation -> donation.getStatus() == DonationStatus.ACTIVE || donation.getStatus() == DonationStatus.EXPIRED)
                    .filter(donation -> {
                        boolean donationIncomplete = donation.getQuantiteDonnee() < donation.getQuantiteDemandee();
                        boolean cagnotteIncomplete = donation.getCagnotteenligne() != null &&
                                donation.getCagnotteenligne().getCurrentAmount() < donation.getCagnotteenligne().getGoalAmount();
                        return donationIncomplete || cagnotteIncomplete;
                    })
                    .sorted(Comparator.comparing(Donation::getEndDate)) // Sort by soonest endDate
                    .collect(Collectors.toList());

            return allDonations;

        } catch (Exception e) {
            logger.error("❌ Error occurred while fetching donations: {}", e.getMessage(), e);
            throw new RuntimeException("Error while fetching donations: " + e.getMessage());
        }
    }


    private boolean isDonationIncomplete(Donation donation) {
        // Check if the donation progress is incomplete (physical or cagnotte)
        boolean isPhysicallyIncomplete = donation.getQuantiteDemandee() > 0 &&
                donation.getQuantiteDonnee() < donation.getQuantiteDemandee();

        boolean isCagnotteIncomplete = donation.getCagnotteenligne() != null &&
                donation.getCagnotteenligne().getCurrentAmount() < donation.getCagnotteenligne().getGoalAmount();

        return isPhysicallyIncomplete || isCagnotteIncomplete;
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

            Long userId = jwtUtils.getUserIdFromJwtToken(jwtToken);
            logger.info("🔍 User ID extracted from JWT: {}", userId);

            Association association = associationRepository.findBySubscriberIdUser(userId)
                    .orElseThrow(() -> new RuntimeException("Association not found for user ID: " + userId));

            Donation donation = new Donation();
            donation.setTitre(donationDto.getTitre());
            donation.setDescription(donationDto.getDescription());
            donation.setQuantiteDemandee(donationDto.getQuantiteDemandee());
            donation.setQuantiteDonnee(donationDto.getQuantiteDonnee());
            donation.setDonationType(donationDto.getDonationType());
            donation.setQuantiteExcedentaire(donationDto.getQuantiteExcedentaire());
            donation.setAssociationDonation(association);
            donation.setCagnotteenligne(donationDto.getCagnotteenligne());
            donation.setStartDate(donationDto.getStartDate());
            donation.setEndDate(donationDto.getEndDate());
            donation.setStatus(DonationStatus.ACTIVE); // Active by default
            donation.setPriority(donationDto.getPriority()); // ✅ Set priority

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

            Donation donation = donationRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Donation not found with ID: " + id));

            donation.setTitre(donationDto.getTitre());
            donation.setDescription(donationDto.getDescription());
            donation.setQuantiteDemandee(donationDto.getQuantiteDemandee());
            donation.setQuantiteDonnee(donationDto.getQuantiteDonnee());
            donation.setDonationType(donationDto.getDonationType());
            donation.setQuantiteExcedentaire(donationDto.getQuantiteExcedentaire());
            donation.setStartDate(donationDto.getStartDate());
            donation.setEndDate(donationDto.getEndDate());
            donation.setStatus(donationDto.getStatus());
            donation.setPriority(donationDto.getPriority()); // ✅ Update priority

            if (donationDto.getCagnotteenligne() != null) {
                CagnotteEnligne cagnotteDto = donationDto.getCagnotteenligne();

                if (donation.getCagnotteenligne() != null) {
                    CagnotteEnligne existingCagnotte = donation.getCagnotteenligne();
                    existingCagnotte.setTitle(cagnotteDto.getTitle());
                    existingCagnotte.setDescription(cagnotteDto.getDescription());
                    existingCagnotte.setGoalAmount(cagnotteDto.getGoalAmount());
                    existingCagnotte.setCurrentAmount(cagnotteDto.getCurrentAmount());
                } else {
                    CagnotteEnligne newCagnotte = new CagnotteEnligne();
                    newCagnotte.setTitle(cagnotteDto.getTitle());
                    newCagnotte.setDescription(cagnotteDto.getDescription());
                    newCagnotte.setGoalAmount(cagnotteDto.getGoalAmount());
                    newCagnotte.setCurrentAmount(cagnotteDto.getCurrentAmount());

                    donation.setCagnotteenligne(newCagnotte);
                    newCagnotte.setDonation(donation);
                }
            } else {
                if (donation.getCagnotteenligne() != null) {
                    CagnotteEnligne cagnotteToRemove = donation.getCagnotteenligne();
                    donation.setCagnotteenligne(null);
                    cagnotteRepository.delete(cagnotteToRemove);
                }
            }

            if (jwtToken == null || jwtToken.trim().isEmpty()) {
                throw new RuntimeException("JWT token cannot be null or empty");
            }
            Long userId = jwtUtils.getUserIdFromJwtToken(jwtToken);
            logger.info("🔍 User ID extracted from JWT: {}", userId);

            Association association = associationRepository.findBySubscriberIdUser(userId)
                    .orElseThrow(() -> new RuntimeException("Association not found for User ID: " + userId));
            donation.setAssociationDonation(association);

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

    public List<DonationDto> getSortedActiveDonations() {
        // Step 1: Get all donations with cagnottes
        List<Donation> allDonationsWithCagnottes = donationRepository.findAllWithCagnottes();

        // Step 2: Filter by ACTIVE status and sort
        List<Donation> sortedActiveDonations = allDonationsWithCagnottes.stream()
                .filter(donation -> donation.getStatus() == DonationStatus.ACTIVE)
                .sorted(Comparator.comparing(Donation::getPriority)
                        .thenComparing(donation -> donation.getStatus().name())
                        .thenComparing(Donation::getEndDate))
                .collect(Collectors.toList());

        // Step 3: Map to DTOs to expose Cagnotte
        return sortedActiveDonations.stream()
                .map(DonationDto::fromDonation)
                .collect(Collectors.toList());
    }



//    public List<DonationDto> getSortedActiveDonations() {
//        // Step 1: Get all donations with cagnottes
//        List<Donation> allDonationsWithCagnottes = donationRepository.findAllWithCagnottes();
//
//        // Step 2: Filter by ACTIVE status and sort (in reversed order)
//        List<Donation> sortedActiveDonations = allDonationsWithCagnottes.stream()
//                .filter(donation -> donation.getStatus() == DonationStatus.ACTIVE)
//                .sorted(
//                        Comparator.comparing(Donation::getPriority).reversed()
//                                .thenComparing((Donation d) -> d.getStatus().name()).reversed()
//                                .thenComparing(Donation::getEndDate, Comparator.reverseOrder())
//                )
//                .collect(Collectors.toList());
//
//        // Step 3: Map to DTOs to expose Cagnotte
//        return sortedActiveDonations.stream()
//                .map(DonationDto::fromDonation)
//                .collect(Collectors.toList());
//    }

}