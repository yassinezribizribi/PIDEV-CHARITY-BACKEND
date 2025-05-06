package tn.esprit.examen.nomPrenomClasseExamen.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.*;
import tn.esprit.examen.nomPrenomClasseExamen.dto.CrisisAssignmentResponse;
import tn.esprit.examen.nomPrenomClasseExamen.dto.CrisisDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.data.domain.Pageable;

@Service
@Slf4j
@AllArgsConstructor
public class CrisisServices implements ICrisisServices {

    private final CrisisRepository crisisRepository;
    private final SubscriberRepository subscriberRepository;
    private final NominatimService nominatimService;
    private final ImageStorageService imageStorageService;
    private final SmsService smsService;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final AssociationRepository associationRepository;

    @Override
    public Crisis addCrisis(CrisisDTO crisisDTO) {
        if (crisisDTO == null) {
            throw new IllegalArgumentException("Crisis data cannot be null");
        }

        Crisis crisis = new Crisis();
        crisis.setCategorie(crisisDTO.getCategorie());
        crisis.setLocation(crisisDTO.getLocation());
        crisis.setUpdates(crisisDTO.getUpdates());
        crisis.setDescription(crisisDTO.getDescription());
        crisis.setCrisisDate(crisisDTO.getCrisisDate());
        crisis.setSeverity(crisisDTO.getSeverity());

        Subscriber subscriber = subscriberRepository.findById(crisisDTO.getIdUser())
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + crisisDTO.getIdUser()));
        crisis.setSubscriber(subscriber);

        // Geocoding
        Double[] coords = nominatimService.getCoordinatesFromAddress(crisisDTO.getLocation());
        if (coords != null) {
            crisis.setLatitude(coords[0]);
            crisis.setLongitude(coords[1]);
        } else {
            log.warn("Failed to geocode address: " + crisisDTO.getLocation());
        }

        // Image handling
        MultipartFile imageFile = crisisDTO.getImage();
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String base64Image = imageStorageService.encodeImageToBase64(imageFile);
                crisis.setImage(base64Image);
            } catch (Exception e) {
                log.error("Error converting image to Base64", e);
                crisis.setImage(null);
            }
        }

        Crisis savedCrisis = crisisRepository.save(crisis);
        assignCrisisToAssociation(crisis.getIdCrisis());
        analyzeAndNotifyUsersByCity();
        handleNewCrisis(savedCrisis);

        return savedCrisis;
    }

    @Override
    public Crisis updateCrisis(Long id, Crisis updatedCrisis) {
        return crisisRepository.findById(id)
                .map(existingCrisis -> {
                    existingCrisis.setDescription(updatedCrisis.getDescription());
                    existingCrisis.setLocation(updatedCrisis.getLocation());
                    existingCrisis.setCategorie(updatedCrisis.getCategorie());
                    existingCrisis.setUpdates(updatedCrisis.getUpdates());
                    existingCrisis.setCrisisDate(updatedCrisis.getCrisisDate());
                    existingCrisis.setSeverity(updatedCrisis.getSeverity());

                    Double[] coords = nominatimService.getCoordinatesFromAddress(updatedCrisis.getLocation());
                    if (coords != null) {
                        existingCrisis.setLatitude(coords[0]);
                        existingCrisis.setLongitude(coords[1]);
                    }

                    return crisisRepository.save(existingCrisis);
                }).orElseThrow(() -> new RuntimeException("Crisis not found with ID: " + id));
    }

    @Override
    public void deleteCrisis(Long id) {
        if (!crisisRepository.existsById(id)) {
            throw new RuntimeException("Crisis not found with ID: " + id);
        }
        crisisRepository.deleteById(id);
    }

    @Override
    public Optional<Crisis> getCrisisById(Long id) {
        return crisisRepository.findById(id);
    }

    @Override
    public Page<Crisis> getAllCrises(Pageable pageable) {
        return crisisRepository.findAll(pageable);
    }

    @Override
    public List<Crisis> getCrisesBySubscriber(Long idUser) {
        return crisisRepository.findBySubscriber_IdUser(idUser);
    }

    @Override
    public List<Crisis> getCrisesByStatus(CrisisStatus status) {
        return crisisRepository.findByStatus(status);
    }

    @Override
    public List<Crisis> getCrisesBySeverity(CrisisSeverity severity) {
        return crisisRepository.findBySeverity(severity);
    }

    @Override
    public Crisis updateCrisisStatus(Long id, CrisisStatus status) {
        Crisis crisis = crisisRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Crisis not found with id: " + id));
        crisis.setStatus(status);
        return crisisRepository.save(crisis);
    }

    @Override
    public Crisis addCrisisWithImage(CrisisDTO crisisDTO, MultipartFile file) {
        crisisDTO.setImage(file);
        return addCrisis(crisisDTO);
    }

    @Scheduled(cron = "0 0 * * * *") // Every hour
    public void autoExpireOldPendingCrises() {
        Date twentyFourHoursAgo = Date.from(Instant.now().minusSeconds(24 * 3600));

        List<Crisis> oldPendingCrises = crisisRepository.findAll().stream()
                .filter(crisis -> crisis.getStatus() == CrisisStatus.PENDING
                        && crisis.getCrisisDate().before(twentyFourHoursAgo))
                .toList();

        for (Crisis crisis : oldPendingCrises) {
            crisis.setStatus(CrisisStatus.EXPIRED);
            crisisRepository.save(crisis);
        }

        log.info("Auto-expired crises: " + oldPendingCrises.size());
    }

    public void handleNewCrisis(Crisis crisis) {
        if (crisis.getSeverity().equals(CrisisSeverity.HIGH)) {
            List<User> targetUsers = userRepository.findByLocation(crisis.getLocation());

            for (User user : targetUsers) {
                try {
                    smsService.sendSms(user.getTelephone(),
                            "🚨 ALERT: " + crisis.getCategorie() + " crisis at " + crisis.getLocation() + ". Stay vigilant!");
                } catch (Exception e) {
                    log.error("Error sending SMS to user: " + user.getTelephone(), e);
                }
            }
        }
    }

    @Override
    public void analyzeAndNotifyUsersByCity() {
        LocalDate today = LocalDate.now();

        List<Crisis> todayCrises = crisisRepository.findAll().stream()
                .filter(crisis -> crisis.getCrisisDate() != null &&
                        crisis.getCrisisDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().isEqual(today))
                .toList();

        Map<String, List<Crisis>> crisesByLocation = todayCrises.stream()
                .collect(Collectors.groupingBy(Crisis::getLocation));

        for (Map.Entry<String, List<Crisis>> entry : crisesByLocation.entrySet()) {
            if (entry.getValue().size() >= 3) {
                List<User> users = userRepository.findByLocation(entry.getKey());

                for (User user : users) {
                    String subject = "🚨 Crisis Alert: " + entry.getKey();
                    String body = buildEmailBody(user, entry.getKey(), entry.getValue().size());

                    try {
                        emailService.sendEmail(user.getEmail(), subject, body);
                        log.info("Alert email sent to: " + user.getEmail());
                    } catch (Exception e) {
                        log.error("Error sending email to " + user.getEmail(), e);
                    }
                }
            }
        }
    }

    @Override
    public CrisisAssignmentResponse assignCrisisToAssociation(Long crisisId) {
        Crisis crisis = crisisRepository.findById(crisisId)
                .orElseThrow(() -> new RuntimeException("Crisis not found"));

        // Get crisis coordinates
        Double[] crisisCoords = nominatimService.getCoordinatesFromAddress(crisis.getLocation());
        if (crisisCoords == null) {
            throw new RuntimeException("Could not determine coordinates for crisis location");
        }

        // Find all approved associations
        List<Association> approvedAssociations = associationRepository.findAll().stream()
                .filter(a -> a.getStatus() == Association.AssociationStatus.APPROVED)
                .collect(Collectors.toList());

        // Find the nearest association
        Association nearestAssociation = null;
        double minDistance = Double.MAX_VALUE;

        for (Association association : approvedAssociations) {
            Double[] assocCoords = nominatimService.getCoordinatesFromAddress(association.getAssociationAddress());
            if (assocCoords != null) {
                double distance = haversine(crisisCoords[0], crisisCoords[1], assocCoords[0], assocCoords[1]);
                if (distance < minDistance) {
                    minDistance = distance;
                    nearestAssociation = association;
                }
            }
        }

        if (nearestAssociation == null) {
            throw new RuntimeException("No available association found");
        }

        crisis.setAssociation(nearestAssociation);
        crisisRepository.save(crisis);

        // Email to the assigned association
        Subscriber subscriber = nearestAssociation.getSubscriber();
        if (subscriber != null && subscriber.getEmail() != null) {
            String subject = "[Action Requise] Nouvelle crise assignée à votre organisation";
            String body = "<html>"
                    + "<body style=\"font-family: 'Segoe UI', Arial, sans-serif; color: #333333; line-height: 1.5;\">"
                    + "<div style=\"max-width: 600px; margin: 0 auto; padding: 20px;\">"
                    + "<h2 style=\"color: #C00000; font-size: 20px; border-bottom: 1px solid #eaeaea; padding-bottom: 10px;\">Nouvelle crise assignée</h2>"
                    + "<p>Bonjour <strong>" + subscriber.getFirstName() + "</strong>,</p>"
                    + "<p>Une nouvelle crise nécessitant votre intervention vient d'être assignée à votre organisation.</p>"
                    + "<div style=\"background-color: #F8F9FA; padding: 15px; border-left: 4px solid #C00000; margin: 15px 0;\">"
                    + "<p style=\"margin: 0 0 10px 0;\"><strong>Détails de la crise :</strong></p>"
                    + "<table style=\"width: 100%; border-collapse: collapse;\">"
                    + "<tr><td style=\"padding: 5px 0; width: 120px; font-weight: 600;\">Localisation :</td><td style=\"padding: 5px 0;\">" + crisis.getLocation() + "</td></tr>"
                    + "<tr><td style=\"padding: 5px 0; vertical-align: top; font-weight: 600;\">Description :</td><td style=\"padding: 5px 0;\">" + crisis.getDescription() + "</td></tr>"
                    + "</table>"
                    + "</div>"
                    + "<p style=\"font-weight: 600;\">Actions requises :</p>"
                    + "<ul style=\"margin-top: 5px; padding-left: 20px;\">"
                    + "<li>Veuillez confirmer la réception de cette alerte</li>"
                    + "<li>Évaluez l'urgence de la situation</li>"
                    + "<li>Mettez en œuvre votre protocole d'intervention</li>"
                    + "</ul>"
                    + "<p style=\"font-size: 13px; color: #707070; font-style: italic; margin-top: 20px;\">"
                    + "Ce message a été généré automatiquement. Pour toute question, veuillez utiliser le système de gestion des crises."
                    + "</p>"
                    + "<p>Cordialement,</p>"
                    + "<p style=\"font-weight: 600;\">Le Centre de Commandement des Crises</p>"
                    + "</div>"
                    + "</body>"
                    + "</html>";
            emailService.sendEmail(subscriber.getEmail(), subject, body);
            log.info("Tentative d'envoi d'un email de crise à : " + subscriber.getEmail());
        }

        // Email to the user who reported the crisis
        Subscriber declarant = crisis.getSubscriber();
        if (declarant != null && declarant.getEmail() != null) {
            String subject = "Votre signalement - Une association a pris en charge votre crise";
            String body = "<html>"
                    + "<body style=\"font-family: 'Segoe UI', Arial, sans-serif; color: #333333; line-height: 1.5;\">"
                    + "<div style=\"max-width: 600px; margin: 0 auto; padding: 20px;\">"
                    + "<h2 style=\"color: #2E7D32; font-size: 20px; border-bottom: 1px solid #eaeaea; padding-bottom: 10px;\">Prise en charge de votre signalement</h2>"
                    + "<p>Bonjour <strong>" + declarant.getFirstName() + "</strong>,</p>"
                    + "<p>Nous vous remercions d'avoir signalé cette crise sur notre plateforme. Votre vigilance contribue à une meilleure gestion des situations d'urgence.</p>"

                    + "<div style=\"background-color: #E8F5E9; padding: 15px; border-left: 4px solid #2E7D32; margin: 15px 0;\">"
                    + "<p style=\"margin: 0 0 10px 0; font-weight: 600;\">Détails de la prise en charge :</p>"
                    + "<table style=\"width: 100%; border-collapse: collapse;\">"
                    + "<tr><td style=\"padding: 5px 0; width: 120px; font-weight: 600;\">Association :</td><td style=\"padding: 5px 0;\">" + nearestAssociation.getAssociationName() + "</td></tr>"
                    + "<tr><td style=\"padding: 5px 0; font-weight: 600;\">Localisation :</td><td style=\"padding: 5px 0;\">" + crisis.getLocation() + "</td></tr>"
                    + "<tr><td style=\"padding: 5px 0; vertical-align: top; font-weight: 600;\">Description :</td><td style=\"padding: 5px 0;\">" + crisis.getDescription() + "</td></tr>"
                    + "</table>"
                    + "</div>"

                    + "<p>L'association assignée prendra contact avec vous si nécessaire. Vous pouvez suivre l'évolution de la situation depuis votre espace personnel.</p>"

                    + "<div style=\"margin-top: 20px; padding: 15px; background-color: #F5F5F5; border-radius: 4px;\">"
                    + "<p style=\"margin: 0; font-size: 14px;\">Vous souhaitez modifier ou compléter votre signalement ? <a href=\"[LIEN_VERS_PLATEFORME]\" style=\"color: #1565C0;\">Accédez à votre espace</a></p>"
                    + "</div>"

                    + "<p style=\"font-size: 13px; color: #707070; font-style: italic; margin-top: 20px;\">"
                    + "Ce message est envoyé automatiquement, merci de ne pas y répondre directement."
                    + "</p>"

                    + "<p>Cordialement,</p>"
                    + "<p style=\"font-weight: 600;\">L'équipe SoliRef</p>"
                    + "<p style=\"font-size: 12px; color: #707070;\">Ensemble pour une réponse solidaire aux crises</p>"
                    + "</div>"
                    + "</body>"
                    + "</html>";
            emailService.sendEmail(declarant.getEmail(), subject, body);
        }

        return new CrisisAssignmentResponse(
                crisisId,
                nearestAssociation.getIdAssociation(),
                "Assigned to nearest association (" + String.format("%.2f", minDistance) + " km away)"
        );
    }

    private String buildEmailBody(User user, String location, int crisisCount) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta charset=\"UTF-8\">" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "</head>" +
                "<body style=\"font-family: 'Segoe UI', Arial, sans-serif; margin: 0; padding: 0; color: #333333; line-height: 1.6;\">" +
                "    <div style=\"max-width: 600px; margin: 0 auto; padding: 20px;\">" +
                "        <!-- Header -->" +
                "        <div style=\"background-color: #D32F2F; padding: 20px; text-align: center; border-radius: 4px 4px 0 0;\">" +
                "            <h1 style=\"color: #ffffff; margin: 0; font-size: 24px;\">CRISIS ALERT NOTIFICATION</h1>" +
                "        </div>" +
                "        " +
                "        <!-- Content -->" +
                "        <div style=\"background-color: #ffffff; padding: 30px; border: 1px solid #e0e0e0; border-top: none;\">" +
                "            <p style=\"margin-top: 0;\">Dear " + user.getLastName() + ",</p>" +
                "            " +
                "            <div style=\"background-color: #FFF5F5; border-left: 4px solid #D32F2F; padding: 15px; margin: 20px 0;\">" +
                "                <p style=\"margin: 0; font-weight: 600; color: #D32F2F;\">" +
                "                    <strong>" + crisisCount + " emergency situations</strong> have been reported today in <strong>" + location + "</strong>." +
                "                </p>" +
                "            </div>" +
                "            " +
                "            <p>Our monitoring systems have detected increased activity in your area. Please:</p>" +
                "            " +
                "            <ul style=\"margin: 20px 0; padding-left: 20px;\">" +
                "                <li style=\"margin-bottom: 10px;\">Remain vigilant and monitor official channels</li>" +
                "                <li style=\"margin-bottom: 10px;\">Follow local safety instructions</li>" +
                "                <li style=\"margin-bottom: 10px;\">Avoid unnecessary travel to affected areas</li>" +
                "            </ul>" +
                "            " +
                "            <p>We will continue to monitor the situation and provide updates as needed.</p>" +
                "        </div>" +
                "        " +
                "        <!-- Footer -->" +
                "        <div style=\"background-color: #f5f5f5; padding: 20px; text-align: center; border-radius: 0 0 4px 4px; font-size: 13px; color: #666666;\">" +
                "            <p style=\"margin: 0 0 10px 0;\">For assistance, contact our 24/7 support team:</p>" +
                "            <p style=\"margin: 0;\">" +
                "                <a href=\"mailto:support@crisismanagement.org\" style=\"color: #D32F2F; text-decoration: none;\">support@crisismanagement.org</a> | " +
                "                <a href=\"tel:+1234567890\" style=\"color: #D32F2F; text-decoration: none;\">+1 (234) 567-890</a>" +
                "            </p>" +
                "            <p style=\"margin: 15px 0 0 0; border-top: 1px solid #e0e0e0; padding-top: 15px;\">" +
                "                © " + LocalDate.now().getYear() + " Crisis Management System. All rights reserved." +
                "            </p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    public List<String> getSaturationZones(int seuil) {
        LocalDate today = LocalDate.now();

        List<Crisis> todayCrises = crisisRepository.findAll().stream()
                .filter(crisis -> crisis.getCrisisDate() != null &&
                        crisis.getCrisisDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().isEqual(today))
                .toList();

        Map<String, Long> locationCounts = todayCrises.stream()
                .collect(Collectors.groupingBy(Crisis::getLocation, Collectors.counting()));

        return locationCounts.entrySet().stream()
                .filter(entry -> entry.getValue() >= seuil)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
