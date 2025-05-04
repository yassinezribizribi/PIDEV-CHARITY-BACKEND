package tn.esprit.examen.nomPrenomClasseExamen.services;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.AssociationRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.DonationRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.DonsRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.SubscriberRepository;
import tn.esprit.examen.nomPrenomClasseExamen.dto.DonsDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Association;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Donation;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Dons;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Subscriber;
import tn.esprit.examen.nomPrenomClasseExamen.jwt.JwtUtils;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class DonsServices {
    private final DonsRepository donsRepository;
    private final DonationRepository donationRepository;
    private final SubscriberRepository subscriberRepository;
    private final JwtUtils jwtUtils;
    private final AssociationRepository associationRepository;
    private final MailerSendService mailerSendService;
    private static final Logger logger = LoggerFactory.getLogger(DonsServices.class);


    //    @Scheduled(cron = "0 0 1 * * ?") // Every day at 1 AM
//    @Transactional
//    public void removeUnvalidatedExpiredDons() {
//        LocalDateTime now = LocalDateTime.now();
//        List<Dons> allDons = donsRepository.findAll();
//
//        for (Dons dons : allDons) {
//            if (!dons.isAssociationValidated() &&
//                    dons.getScheduledDateTime() != null &&
//                    dons.getScheduledDateTime().isBefore(now)) {
//
//                donsRepository.delete(dons);
//                logger.info("🗑️ Dons with ID {} deleted (expired & not validated)", dons.getIdDons());
//            }
//        }
//    }
    // Method to find donation by ID
    public Dons findDonationById(Long id) {
        return donsRepository.findById(id).orElse(null);
    }

    // Method to create the cancellation email body
//    public String createCancellationEmailBody(Dons donation) {
//        String firstName = donation.getNomDoneur() != null ? donation.getPrenomDoneur() : "Friend";
//        return String.format("""
//            <p>Hello %s,</p>
//            <p>We regret to inform you that your donation has been canceled because it was not validated within the scheduled time.</p>
//            <p>If you have any questions, please feel free to contact us.</p>
//            <p>Thank you for your understanding,</p>
//            <p>The Solidarity & Refugee Team</p>
//        """, firstName);
//    }
    @Scheduled(cron = "0 0 6 * * ?") // Runs every day at 6 AM
    @Transactional
    public void dailyBackgroundCleaner() {
        LocalDateTime now = LocalDateTime.now();

        // Fetch all donations that are not validated and have a scheduled date
        List<Dons> allDons = donsRepository.findAll();

        for (Dons donation : allDons) {
            LocalDateTime scheduledDate = donation.getScheduledDateTime();

            // If the donation is not validated and past the grace period, cancel it
            if (scheduledDate != null && !donation.isAssociationValidated()) {
                if (scheduledDate.plusDays(2).isBefore(now)) {
                    // Donation has passed grace period, cancel it
                    cancelDonation(donation);
                    logger.info("Donation ID {} canceled after grace period.", donation.getIdDons());

                    // Send an email to the donor informing them about the deletion
                    String emailBody = createCancellationEmailBody(donation);
                    sendEmailToDonor(donation.getDonorEmail(), emailBody);
                } else {
                    logger.info("Donation ID {} is still pending grace period.", donation.getIdDons());
                }
            }
        }
    }

    private void cancelDonation(Dons donation) {
        donation.setAssociationValidated(false); // Mark it as not validated
        donsRepository.save(donation);  // Persist the canceled donation

        // Delete the donation from the repository
        donsRepository.delete(donation);
    }

    private void sendEmailToDonor(String recipientEmail, String emailBody) {
        try {
            // Logic for sending the email to the donor
            // Assuming you have an EmailService to send emails
            mailerSendService.sendEmail(recipientEmail, "Donation Cancellation Notice", emailBody);
        } catch (IOException e) {
            // Handle the exception: log it or take necessary action
            logger.error("Failed to send email to {}: {}", recipientEmail, e.getMessage());
            // Optionally, rethrow or handle differently
        }
    }


    public String createCancellationEmailBody(Dons donation) {
        String firstName = donation.getNomDoneur() != null ? donation.getPrenomDoneur() : "Friend";
        String formattedDate = donation.getScheduledDateTime()
                .format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' HH:mm"));

        // Extracting relevant details from the donation
        Long donationId = donation.getIdDons();
        int quantity = donation.getQuantite();
        String donorEmail = donation.getDonorEmail();
        String deliveryMethod = donation.getDeliveryMethod().toString(); // Enum to String

        // Building the email body with these details
        return String.format("""
    <div style="font-family: 'Helvetica Neue', Arial, sans-serif; color: #333333; max-width: 600px; margin: 0 auto;">
        <div style="background-color: #FF6347; padding: 20px; text-align: center;">
            <h1 style="color: white; margin: 0;">Donation Cancellation Notice</h1>
        </div>
        <div style="padding: 20px;">
            <h2 style="color: #FF6347;">Hello %s,</h2>
            <p style="font-size: 16px;">
                We regret to inform you that your donation scheduled for <strong>%s</strong> has been automatically canceled as it was not validated within the grace period.
            </p>
            <p style="font-size: 16px;">
                Donation ID: <strong>%d</strong><br>
                Quantity: <strong>%d</strong><br>
                Delivery Method: <strong>%s</strong><br>
                Donor Email: <strong>%s</strong>
            </p>
            <p style="font-size: 16px;">We encourage you to reschedule your donation if you'd like to support again.</p>
            <p style="font-size: 16px;">Thank you for your understanding and continued support!</p>
            <p style="font-size: 16px;">The Solidarity & Refugee Team</p>
            <div style="margin-top: 40px; font-size: 12px; color: #999; text-align: center;">
                © %d Solidarity & Refugee | <a href="https://yourorganization.org" style="color: #FF6347;">Website</a>
            </div>
        </div>
    </div>
    """,
                firstName,
                formattedDate,
                donationId,         // Donation ID
                quantity,           // Donation quantity
                deliveryMethod,     // Delivery method (DROP_OFF or PICK_UP)
                donorEmail,         // Donor's email
                java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)  // Current year
        );
    }



    @Scheduled(cron = "0 0 6 * * ?") // Every day at 6 AM
    @Transactional
    public void sendDonationReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next24h = now.plusHours(24);

        List<Dons> allDons = donsRepository.findAll();

        for (Dons dons : allDons) {
            if (dons.getScheduledDateTime() != null &&
                    dons.getScheduledDateTime().isAfter(now) &&
                    dons.getScheduledDateTime().isBefore(next24h)) {

                sendReminder(dons);
                logger.info("📧 Reminder sent for Donation ID {}", dons.getIdDons());
            }
        }
    }

    public DonsDTO contributeToDonation(Long idDonation, DonsDTO donsDto, String jwtToken) {
        try {
            logger.info("📝 Request to contribute to donation ID {} with data: {}", idDonation, donsDto);

            // 1. Find the donation
            Donation donation = donationRepository.findById(idDonation)
                    .orElseThrow(() -> new RuntimeException("❌ Donation not found with ID: " + idDonation));

            // 2. Create the Dons entity from DTO
            Dons dons = new Dons();

            // If the user is authenticated, pre-fill details from JWT but allow override
            if (jwtToken != null && !jwtToken.trim().isEmpty() && jwtUtils.validateJwtToken(jwtToken)) {
                try {
                    Long userId = jwtUtils.getUserIdFromJwtToken(jwtToken);
                    String userEmail = jwtUtils.getUserEmailFromJwtToken(jwtToken);
                    String userFirstName = jwtUtils.getUserFirstNameFromJwtToken(jwtToken);
                    String userLastName = jwtUtils.getUserLastNameFromJwtToken(jwtToken);

                    // Pre-fill with user info from JWT, but allow the user to override if they provide their own data
                    dons.setNomDoneur(donsDto.getNomDoneur() != null ? donsDto.getNomDoneur() : userFirstName);
                    dons.setPrenomDoneur(donsDto.getPrenomDoneur() != null ? donsDto.getPrenomDoneur() : userLastName);
                    dons.setDonorEmail(donsDto.getDonorEmail() != null ? donsDto.getDonorEmail() : userEmail);

                    // Find and set Utilisateur (subscriber)
                    subscriberRepository.findById(userId).ifPresentOrElse(
                            dons::setSubscriberDons,
                            () -> dons.setSubscriberDons(null)
                    );
                    logger.info("🔍 User ID extracted and utilisateur set for donation: {}", userId);

                } catch (Exception ex) {
                    logger.warn("⚠️ Could not extract user from token: {}", ex.getMessage());
                    dons.setSubscriberDons(null);  // If something goes wrong, no subscriber
                }
            } else {
                // If unauthenticated, allow manual entry
                dons.setNomDoneur(donsDto.getNomDoneur());
                dons.setPrenomDoneur(donsDto.getPrenomDoneur());
                dons.setDonorEmail(donsDto.getDonorEmail()); // Allow manual entry for email
                dons.setSubscriberDons(null); // No subscriber if not authenticated
            }

            // 3. Set other details
            dons.setQuantite(donsDto.getQuantite());
            dons.setScheduledDateTime(donsDto.getScheduledDateTime());
            dons.setDonorNote(donsDto.getDonorNote());
            dons.setDeliveryMethod(donsDto.getDeliveryMethod());
            dons.setDonorAddress(donsDto.getDonorAddress());
            dons.setDonationDate(LocalDateTime.now());
            dons.setDonation(donation);

            // 4. Save the contribution
            Dons savedDons = donsRepository.save(dons);
            logger.info("✅ Dons contribution successfully saved with ID: {}", savedDons.getIdDons());

            // 5. Send confirmation email
            String emailBody = createEmailBody(savedDons, donsDto.getNomDoneur(), donsDto.getPrenomDoneur());
            mailerSendService.sendEmail(dons.getDonorEmail(), "Your Donation Has Been Created – Don’t Miss Your Scheduled Date!", emailBody);
            logger.info("📧 Confirmation email sent to: {}", dons.getDonorEmail());

            // 6. Return DTO
            return DonsDTO.fromDons(savedDons);

        } catch (Exception e) {
            logger.error("❌ Error while contributing to donation: {}", e.getMessage(), e);
            throw new RuntimeException("Error while contributing to donation: " + e.getMessage());
        }
    }



    // Create the email body
    private String createEmailBody(Dons savedDons, String firstName, String lastName) {
        String formattedDate = formatEmailDate(savedDons.getScheduledDateTime());
        String noteContent = savedDons.getDonorNote() != null ?
                savedDons.getDonorNote() :
                "<span style='color: #999;'>No note provided</span>";

        return String.format("""
    <div style="font-family: 'Helvetica Neue', Arial, sans-serif; color: #333333; max-width: 600px; margin: 0 auto;">
        <!-- Email Header -->
        <div style="background-color: #006666; padding: 20px; text-align: center;">
            <h1 style="color: white; margin: 0; font-size: 24px;">Thank You For Your Donation!</h1>
        </div>
        
        <!-- Greeting -->
        <div style="padding: 20px;">
            <h2 style="color: #006666; margin-top: 0;">Dear %s %s,</h2>
            <p style="font-size: 16px; line-height: 1.5;">
                We are truly grateful for your generous contribution to our cause. 
                Your support makes a meaningful difference in our mission to help those in need.
            </p>
            
            <!-- Donation Summary -->
            <div style="background-color: #f8f9fa; border-radius: 8px; padding: 20px; margin: 20px 0;">
                <h3 style="color: #006666; margin-top: 0; border-bottom: 2px solid #e9ecef; padding-bottom: 10px;">
                    <span style="background-color: #ff9933; color: white; padding: 3px 8px; border-radius: 4px; margin-right: 10px;">✓</span>
                    Your Donation Summary
                </h3>
                
                <table style="width: 100%%; border-collapse: collapse;">
                    <tr>
                        <td style="padding: 12px 8px; border-bottom: 1px solid #e9ecef; font-weight: bold; width: 40%%;">Donation ID</td>
                        <td style="padding: 12px 8px; border-bottom: 1px solid #e9ecef;">DN-%s</td>
                    </tr>
                    <tr>
                        <td style="padding: 12px 8px; border-bottom: 1px solid #e9ecef; font-weight: bold;">Quantity</td>
                        <td style="padding: 12px 8px; border-bottom: 1px solid #e9ecef;">%s items</td>
                    </tr>
                    <tr>
                        <td style="padding: 12px 8px; border-bottom: 1px solid #e9ecef; font-weight: bold;">Scheduled Date</td>
                        <td style="padding: 12px 8px; border-bottom: 1px solid #e9ecef;">%s</td>
                    </tr>
                    <tr>
                        <td style="padding: 12px 8px; border-bottom: 1px solid #e9ecef; font-weight: bold;">Delivery Method</td>
                        <td style="padding: 12px 8px; border-bottom: 1px solid #e9ecef;">%s</td>
                    </tr>
                    <tr>
                        <td style="padding: 12px 8px; font-weight: bold;">Your Note</td>
                        <td style="padding: 12px 8px;">%s</td>
                    </tr>
                </table>
            </div>
            
            <!-- Important Reminder -->
            <div style="background-color: #fff8e6; border-left: 4px solid #ff9933; padding: 15px; margin: 20px 0;">
                <h4 style="margin-top: 0; color: #d35400;">Important Reminder</h4>
                <p style="margin-bottom: 0;">
                    Please be available on your scheduled date. If you need to make any changes, 
                    please contact us at least 24 hours in advance at 
                    <a href="mailto:support@yourorganization.org" style="color: #006666;">support@yourorganization.org</a>.
                </p>
            </div>
            
            <!-- Receipt Download -->
            <div style="text-align: center; margin: 30px 0;">
                <p style="margin-bottom: 15px;">Your official donation receipt is ready:</p>
                <a href="http://localhost:8089/api/dons/receipt/%s" 
                   style="background-color: #006666; color: white; padding: 12px 25px; 
                          text-decoration: none; border-radius: 4px; font-weight: bold; 
                          display: inline-block;">
                    Download Receipt
                </a>
            </div>
            
            <!-- Closing -->
            <p style="font-size: 16px; line-height: 1.5;">
                With gratitude,<br>
                <strong style="color: #006666;">The Solidarity & Refugee Team</strong>
            </p>
            
            <!-- Footer -->
            <div style="margin-top: 40px; padding-top: 20px; border-top: 1px solid #e9ecef; 
                        font-size: 12px; color: #999; text-align: center;">
                <p>
                    © %d Your Organization Name<br>
                    123 Charity Street, City, Country<br>
                    <a href="http://yourorganization.org" style="color: #006666;">www.yourorganization.org</a>
                </p>
            </div>
        </div>
    </div>
    """,
                firstName,
                lastName,
                savedDons.getIdDons(),
                savedDons.getQuantite(),
                formattedDate,
                capitalize(savedDons.getDeliveryMethod().toString()),
                noteContent,
                savedDons.getIdDons(),
                Calendar.getInstance().get(Calendar.YEAR)
        );
    }

    private String formatEmailDate(Temporal dateTime) {
        if (dateTime instanceof LocalDateTime) {
            return DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a").format((LocalDateTime)dateTime);
        } else if (dateTime instanceof LocalDate) {
            return DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy").format((LocalDate)dateTime);
        }
        return dateTime.toString();
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
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

    public DonsDTO getDonsById(Long donsId) {
        Dons dons = donsRepository.findById(donsId)
                .orElseThrow(() -> new RuntimeException("❌ Dons not found with ID: " + donsId));
        return DonsDTO.fromDons(dons);
    }

    public Page<DonsDTO> getDonsByAssociationFromToken(String jwtToken, int page, int size) {
        try {
            if (jwtToken == null || jwtToken.trim().isEmpty()) {
                throw new RuntimeException("JWT token cannot be null or empty");
            }

            Long userId = jwtUtils.getUserIdFromJwtToken(jwtToken);
            Association association = associationRepository.findBySubscriberIdUser(userId)
                    .orElseThrow(() -> new RuntimeException("Association not found for user ID: " + userId));

            List<Donation> donations = donationRepository.findByAssociationDonationIdAssociation(association.getIdAssociation());

            // Fetch all Dons
            List<Dons> allDons = new ArrayList<>();
            for (Donation donation : donations) {
                List<Dons> donsForDonation = donsRepository.findByDonation_IdDonation(donation.getIdDonation());
                allDons.addAll(donsForDonation);
            }

            // Sort manually (if needed)
            allDons.sort(Comparator.comparing(Dons::getIdDons)); // or any other sorting logic

            // Convert to DTO
            List<DonsDTO> allDtos = allDons.stream()
                    .map(DonsDTO::fromDons)
                    .collect(Collectors.toList());

            // Pagination manually
            int start = Math.min(page * size, allDtos.size());
            int end = Math.min(start + size, allDtos.size());
            List<DonsDTO> paginated = allDtos.subList(start, end);
            return new PageImpl<>(paginated, PageRequest.of(page, size), allDtos.size());

        } catch (Exception e) {
            logger.error("❌ Error occurred while fetching paginated DonsDTOs: {}", e.getMessage(), e);
            throw new RuntimeException("Error while fetching DonsDTOs: " + e.getMessage());
        }
    }

    public Page<DonsDTO> getDonsByAuthenticatedUser(String jwtToken, int page, int size) {
        try {
            Long userId = jwtUtils.getUserIdFromJwtToken(jwtToken);
            Pageable pageable = PageRequest.of(page, size, Sort.by("idDons").ascending());
            Page<Dons> donsPage = donsRepository.findBySubscriberDons_IdUser(userId, pageable);
            return donsPage.map(DonsDTO::fromDons);
        } catch (Exception e) {
            logger.error("❌ Failed to fetch paginated dons for user: {}", e.getMessage());
            throw new RuntimeException("Could not retrieve dons for user.");
        }
    }

    public DonsDTO updateDons(Long donsId, DonsDTO donsDTO, String jwtToken) {
        try {
            Long userId = jwtUtils.getUserIdFromJwtToken(jwtToken);
            Dons dons = donsRepository.findById(donsId)
                    .orElseThrow(() -> new RuntimeException("Dons not found"));

            if (!dons.getSubscriberDons().getIdUser().equals(userId)) {
                throw new RuntimeException("Unauthorized to update this donation");
            }

            // Update fields
            dons.setNomDoneur(donsDTO.getNomDoneur());
            dons.setPrenomDoneur(donsDTO.getPrenomDoneur());
            dons.setDonorEmail(donsDTO.getDonorEmail());
            dons.setQuantite(donsDTO.getQuantite());
            dons.setScheduledDateTime(donsDTO.getScheduledDateTime());
            dons.setDonorNote(donsDTO.getDonorNote());
            dons.setDeliveryMethod(donsDTO.getDeliveryMethod());
            dons.setDonorAddress(donsDTO.getDonorAddress());

            Dons updatedDons = donsRepository.save(dons);
            return DonsDTO.fromDons(updatedDons);

        } catch (Exception e) {
            logger.error("❌ Error updating dons: {}", e.getMessage());
            throw new RuntimeException("Error while updating donation");
        }
    }
    public void deleteDons(Long donsId) {
        try {
            Dons dons = donsRepository.findById(donsId)
                    .orElseThrow(() -> new RuntimeException("Dons not found"));
            donsRepository.delete(dons);
            logger.info("✅ Dons with ID {} deleted successfully", donsId);
        } catch (Exception e) {
            logger.error("❌ Error deleting dons: {}", e.getMessage());
            throw new RuntimeException("Error while deleting donation");
        }
    }
    public DonsDTO getDonsForUserById(Long donsId, String jwtToken) {
        Long userId = jwtUtils.getUserIdFromJwtToken(jwtToken); // 👈 extract user ID from token

        Dons dons = donsRepository.findById(donsId)
                .orElseThrow(() -> new RuntimeException("Dons not found with ID: " + donsId));

        if (dons.getSubscriberDons() == null || !dons.getSubscriberDons().getIdUser().equals(userId)) {
            throw new RuntimeException("Access denied: This donation doesn't belong to the authenticated user");
        }

        return DonsDTO.fromDons(dons);
    }
    public String createReminderEmailBody(Dons donation, boolean isRegisteredUser, String specialLink) {
        String firstName = donation.getNomDoneur() != null ? donation.getPrenomDoneur() : "Friend";
        String formattedDate = donation.getScheduledDateTime()
                .format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' HH:mm"));

        return String.format("""
        <div style="font-family: 'Helvetica Neue', Arial, sans-serif; color: #333333; max-width: 600px; margin: 0 auto;">
            <div style="background-color: #006666; padding: 20px; text-align: center;">
                <h1 style="color: white; margin: 0;">Upcoming Donation Reminder</h1>
            </div>
            <div style="padding: 20px;">
                <h2 style="color: #006666;">Hello %s,</h2>
                <p style="font-size: 16px;">
                    Just a reminder: Your donation is scheduled for <strong>%s</strong>.
                </p>
                <div style="text-align: center; margin: 30px 0;">
                    <a href="%s" style="background-color: #006666; color: white; padding: 12px 25px; 
                                       text-decoration: none; border-radius: 5px; font-weight: bold;">
                        Manage My Donation
                    </a>
                </div>
                <p style="font-size: 16px;">Thank you for your kindness and support!</p>
                <p style="font-size: 16px;">The Solidarity & Refugee Team</p>
                <div style="margin-top: 40px; font-size: 12px; color: #999; text-align: center;">
                    © %d Solidarity & Refugee | <a href="https://yourorganization.org" style="color: #006666;">Website</a>
                </div>
            </div>
        </div>
        """,
                firstName,
                formattedDate,
                specialLink,
                java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        );
    }


     final TaskScheduler scheduler = createDefaultScheduler();

    public void scheduleReminder(Dons donation) {
        Duration untilScheduledDate = Duration.between(LocalDateTime.now(), donation.getScheduledDateTime());

        if (untilScheduledDate.toHours() >= 48) {
            LocalDateTime reminderTime = donation.getScheduledDateTime().minusHours(24);
            Date reminderDate = java.sql.Timestamp.valueOf(reminderTime);
            scheduler.schedule(() -> sendReminder(donation), reminderDate);
        } else {
            sendReminder(donation);
        }
    }

    private void sendReminder(Dons donation) {
        boolean isRegistered = donation.getSubscriberDons() != null;
        String specialLink = generateSpecialLink(donation, isRegistered);

        String emailBody = createReminderEmailBody(donation, isRegistered, specialLink);
        String subject = "Reminder: Your Donation is Coming Up!";

        String recipientEmail = isRegistered
                ? donation.getSubscriberDons().getEmail()
                : donation.getDonorEmail(); // <<< Use the donor email for non-registered users

        try {
            mailerSendService.sendEmail(recipientEmail, subject, emailBody);
        } catch (IOException e) {
            throw new RuntimeException("Failed to send reminder email: " + e.getMessage(), e);
        }
    }


    private String generateSpecialLink(Dons donation, boolean isRegistered) {
        if (isRegistered) {
            return "http://localhost:4200/login";

        } else {
            String token = createSecureToken(donation);
            return String.format("http://localhost:4200/update-dons-nonauth/%s?token=%s", donation.getIdDons(), token);
        }
    }

    private String createSecureToken(Dons donation) {
        // Set token expiration to 24 hours from now
        long expirationTime = System.currentTimeMillis() + Duration.ofHours(24).toMillis();

        String donorEmail = donation.getSubscriberDons() != null
                ? donation.getSubscriberDons().getEmail()
                : donation.getDonorEmail();

        String tokenData = donation.getIdDons() + "-" + donorEmail + "-" + expirationTime;

        // Create a secure token using SHA-256
        String token = org.apache.commons.codec.digest.DigestUtils.sha256Hex(tokenData);

        // Return the token with expiration time appended
        return token + "-" + expirationTime;
    }


    private static TaskScheduler createDefaultScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("ReminderScheduler-");
        scheduler.initialize();
        return scheduler;
    }

    public boolean validateToken(String token) {
        // Extract the expiration time from the token
        String[] tokenParts = token.split("-");
        if (tokenParts.length != 2) {
            return false; // Invalid token format
        }

        String hashedToken = tokenParts[0];
        long expirationTime = Long.parseLong(tokenParts[1]);

        // Check if the token is expired
        if (System.currentTimeMillis() > expirationTime) {
            return false; // Token expired
        }

        // Optionally, verify that the token's hash matches the expected hash based on your original token data
        // (This would require saving or having access to the original donation details to generate and compare hashes)
        return true; // Token is valid and not expired
    }
    public long extractDonationIdFromToken(String token) {
        String[] tokenParts = token.split("-");
        if (tokenParts.length != 2) {
            throw new IllegalArgumentException("Invalid token format.");
        }

        String hashedToken = tokenParts[0];
        long donationId = Long.parseLong(tokenParts[1]);
        // The donation ID is part of the token

        return donationId;
    }

}


