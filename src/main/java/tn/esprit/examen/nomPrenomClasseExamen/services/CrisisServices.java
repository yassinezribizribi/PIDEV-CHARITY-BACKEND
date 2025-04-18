package tn.esprit.examen.nomPrenomClasseExamen.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.CrisisRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.SubscriberRepository;
import tn.esprit.examen.nomPrenomClasseExamen.dto.CrisisDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Crisis;
import tn.esprit.examen.nomPrenomClasseExamen.entities.CrisisSeverity;
import tn.esprit.examen.nomPrenomClasseExamen.entities.CrisisStatus;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Subscriber;

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

        // Associer l'utilisateur (subscriber)
        Subscriber subscriber = subscriberRepository.findById(crisisDTO.getIdUser())
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'ID : " + crisisDTO.getIdUser()));
        crisis.setSubscriber(subscriber);

        // Géocodage de l'adresse via OpenStreetMap (Nominatim)
        Double[] coords = nominatimService.getCoordinatesFromAddress(crisisDTO.getLocation());
        if (coords != null) {
            crisis.setLatitude(coords[0]);
            crisis.setLongitude(coords[1]);
        } else {
            log.warn("Impossible de géocoder l'adresse : " + crisisDTO.getLocation());
        }

        // 📸 Gestion de l'image
        if (crisisDTO.getImage() != null && !crisisDTO.getImage().isEmpty()) {
            try {
                String imagePath = imageStorageService.storeImage(crisisDTO.getImage());
                crisis.setImage(imagePath);
            } catch (Exception e) {
                log.error("Erreur lors du stockage de l'image", e);
                crisis.setImage(null); // ou définir une image par défaut
            }
        }

        return crisisRepository.save(crisis);
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
    public Crisis addCrisisWithImage(CrisisDTO crisisDTO, MultipartFile file) {
        return null;
    }
}
