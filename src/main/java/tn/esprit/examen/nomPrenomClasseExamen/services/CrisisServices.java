package tn.esprit.examen.nomPrenomClasseExamen.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.CrisisRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.SubscriberRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.UserRepository;
import tn.esprit.examen.nomPrenomClasseExamen.dto.CrisisDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.*;

import java.time.Instant;
import java.util.Date;

import java.util.List;
import java.util.Optional;

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

        // ✅ Sauvegarde de la crise
        Crisis savedCrisis = crisisRepository.save(crisis);

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
            // Filter users by location
            List<User> targetUsers = userRepository.findByLocation(crisis.getLocation());

            // Send SMS to users in that location
            for (User user : targetUsers) {
                smsService.sendSms(user.getTelephone(),
                        "🚨 Attention, une crise HIGH a été signalée à " + crisis.getLocation() + ". Restez vigilant !");
            }
        }
    }
}
