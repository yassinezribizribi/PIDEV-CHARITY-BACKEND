package tn.esprit.examen.nomPrenomClasseExamen.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.AssociationRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.CrisisRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.SubscriberRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.UserRepository;
import tn.esprit.examen.nomPrenomClasseExamen.dto.CrisisDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor

public class CrisisServices implements ICrisisServices {

    private final CrisisRepository crisisRepository;
    private final SubscriberRepository subscriberRepository;
    private final NominatimService nominatimService;
    private final ImageStorageService imageStorageService; // 🔥 Ajout de ce service pour gérer les images

    private final SmsService smsService;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final AssociationRepository associationRepository;

    @Override
    public Crisis addCrisis(CrisisDTO crisisDTO) {
        if (crisisDTO == null) {
            throw new IllegalArgumentException("Les données de la crise sont nulles.");
        }

        Crisis crisis = new Crisis();
        crisis.setCategorie(crisisDTO.getCategorie());
        crisis.setLocation(crisisDTO.getLocation());
        crisis.setUpdates(crisisDTO.getUpdates());
        crisis.setDescription(crisisDTO.getDescription());
        crisis.setCrisisDate(crisisDTO.getCrisisDate());
        crisis.setSeverity(crisisDTO.getSeverity());

        // Associer l'utilisateur
        Subscriber subscriber = subscriberRepository.findById(crisisDTO.getIdUser())
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'ID : " + crisisDTO.getIdUser()));
        crisis.setSubscriber(subscriber);

        // Géocodage via Nominatim
        Double[] coords = nominatimService.getCoordinatesFromAddress(crisisDTO.getLocation());
        if (coords != null) {
            crisis.setLatitude(coords[0]);
            crisis.setLongitude(coords[1]);
        } else {
            log.warn("Impossible de géocoder l'adresse : " + crisisDTO.getLocation());
        }

        // Gestion de l'image
        MultipartFile imageFile = crisisDTO.getImage();
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String base64Image = imageStorageService.encodeImageToBase64(imageFile);
                crisis.setImage(base64Image);
            } catch (Exception e) {
                log.error("Erreur lors de la conversion de l'image en Base64", e);
                crisis.setImage(null);
            }
        }

        assignCrisisToAssociation(crisis);
        Crisis savedCrisis = crisisRepository.save(crisis);

        analyzeAndNotifyUsersByCity();


        // ✅ Envoi de SMS si besoin
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

                    // Mise à jour lat/lng si adresse changée
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
    public List<Crisis> getAllCrises() {
        return crisisRepository.findAll();
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
        return addCrisis(crisisDTO); // ✅ Reuse your existing logic
    }




    @Scheduled(cron = "0 0 * * * *") // toutes les heures
    public void autoExpireOldPendingCrises() {
        Date twentyFourHoursAgo = Date.from(Instant.now().minusSeconds(24 * 3600));

        List<Crisis> oldPendingCrises = crisisRepository.findAll().stream()
                .filter(crisis -> crisis.getStatus() == CrisisStatus.PENDING
                        && crisis.getCrisisDate().before(twentyFourHoursAgo))
                .toList();

        for (Crisis crisis : oldPendingCrises) {
            crisis.setStatus(CrisisStatus.EXPIRED); // statut métier à définir dans l'enum si besoin
            crisisRepository.save(crisis);
        }

        log.info("Crises expirées automatiquement : " + oldPendingCrises.size());
    }

    public void handleNewCrisis(Crisis crisis) {
        if (crisis.getSeverity().equals(CrisisSeverity.HIGH)) {
            List<User> targetUsers = userRepository.findByLocation(crisis.getLocation());

            for (User user : targetUsers) {
                try {
                    smsService.sendSms(user.getTelephone(),
                            "🚨 Attention, une crise HIGH de categorie " + crisis.getCategorie()+" a été signalée à " + crisis.getLocation() + ". Restez vigilant !");
                } catch (Exception e) {
                    log.error("Erreur lors de l'envoi du SMS à l'utilisateur avec le téléphone : " + user.getTelephone(), e);
                    // Continue sending to others even if one fails
                }
            }
        }
    }

    @Override
    public void analyzeAndNotifyUsersByCity() {
            LocalDate today = LocalDate.now();

            // 1. Récupérer les crises d’aujourd’hui
            List<Crisis> todayCrises = crisisRepository.findAll().stream()
                    .filter(crisis -> {
                        if (crisis.getCrisisDate() == null) return false;
                        return crisis.getCrisisDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().isEqual(today);
                    })
                    .toList();

            // 2. Grouper les crises par localisation
            Map<String, List<Crisis>> crisesByLocation = todayCrises.stream()
                    .collect(Collectors.groupingBy(Crisis::getLocation));

            // 3. Vérifier chaque localisation
            for (Map.Entry<String, List<Crisis>> entry : crisesByLocation.entrySet()) {
                String location = entry.getKey();
                List<Crisis> locationCrises = entry.getValue();

                // Si 3 crises sont signalées dans la même localisation aujourd'hui
                if (locationCrises.size() >= 3) {
                    // 4. Récupérer les utilisateurs dans cette localisation
                    List<User> users = userRepository.findByLocation(location);

                    for (User user : users) {
                        String subject = "🚨 Alerte Crise : " + location + " - Plusieurs événements signalés";
                        String body = "<html>"
                                + "<body style=\"font-family: Arial, sans-serif; color: #333;\">"
                                + "<h2 style=\"color: #D32F2F;\">Alerte de Crises à " + location + "</h2>"
                                + "<p>Bonjour <strong>" + user.getLastName() + "</strong>,</p>"
                                + "<p>Nous souhaitons vous informer qu'un nombre important de crises a été signalé aujourd'hui à <strong>" + location + "</strong>.</p>"
                                + "<p>En effet, <strong>" + locationCrises.size() + " crise(s)</strong> ont été signalées, indiquant une situation d'urgence à cette localisation.</p>"
                                + "<p style=\"font-size: 14px; color: #555;\">Nous vous encourageons à rester vigilant(e) et à suivre les recommandations locales pour assurer votre sécurité.</p>"
                                + "<p style=\"color: #555;\"><em>Restez informé(e), restez en sécurité.</em></p>"
                                + "<p>Bien à vous,</p>"
                                + "<p><strong>L'équipe de gestion des crises</strong></p>"
                                + "</body>"
                                + "</html>";

                        try {
                            // Envoi de l'email
                            emailService.sendEmail(user.getEmail(), subject, body);
                            log.info("Email envoyé à : " + user.getEmail() + " pour la localisation : " + location);
                        } catch (Exception e) {
                            log.error("Erreur lors de l'envoi de l'email à " + user.getEmail(), e);
                        }
                    }
                }
            }
        }



    public Crisis assignCrisisToAssociation(Crisis crisis) {
        String crisisLocation = crisis.getLocation().toLowerCase();

        // Rechercher une association dont l'adresse contient la même ville
        Optional<Association> optionalAssociation = associationRepository
                .findAll()
                .stream()
                .filter(a -> crisisLocation.contains(a.getAssociationAddress().toLowerCase()))
                .findFirst();

        optionalAssociation.ifPresent(crisis::setAssignedAssociation);
        return crisis;
    }



}


