package tn.esprit.examen.nomPrenomClasseExamen.services;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.examen.nomPrenomClasseExamen.dto.HealthcareDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Healthcare;
import tn.esprit.examen.nomPrenomClasseExamen.entities.HealthcareStatus;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Subscriber;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.HealthcareRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.SubscriberRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class HealthcareServices implements IHealthcareServices {

    @Autowired
    private HealthcareRepository healthcareRepository;

    @Autowired
    private SubscriberRepository subscriberRepository;

    // ✅ Ajouter un soin
    @Transactional
    @Override
    public Healthcare addHealthcare(HealthcareDTO healthcareDTO) {
        // ✅ Vérifier si le patient existe
        Subscriber subscriber = subscriberRepository.findById(healthcareDTO.getSubscriberId())
                .orElseThrow(() -> new RuntimeException("❌ Patient (Subscriber) non trouvé !"));

        Healthcare healthcare = new Healthcare();
        healthcare.setSubscriber(subscriber); // ✅ Associe le patient
        healthcare.setStatus(HealthcareStatus.PENDING);
        healthcare.setHistory(healthcareDTO.getHistory());
        healthcare.setBookingDate(healthcareDTO.getBookingDate()); // ✅ BookingDate est déjà un Date

        return healthcareRepository.save(healthcare);
    }

    // ✅ Modifier un soin
    @Transactional
    @Override
    public Healthcare updateHealthcare(Long id, HealthcareDTO healthcareDTO) {
        Healthcare healthcare = healthcareRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("❌ Traitement non trouvé !"));

        healthcare.setHistory(healthcareDTO.getHistory());
        healthcare.setBookingDate(healthcareDTO.getBookingDate()); // ✅ BookingDate est déjà un Date

        return healthcareRepository.save(healthcare);

    }

    // ✅ Modifier uniquement le statut et la date
    @Transactional
    @Override
    public Healthcare updateHealthcareStatus(Long healthcareId, String status, Date bookingDate) {
        Healthcare healthcare = healthcareRepository.findById(healthcareId)
                .orElseThrow(() -> new RuntimeException("❌ Soin non trouvé !"));

        try {
            HealthcareStatus newStatus = HealthcareStatus.valueOf(status.toUpperCase());
            healthcare.setStatus(newStatus);
            healthcare.setBookingDate(bookingDate); // ✅ BookingDate est déjà un Date

            return healthcareRepository.save(healthcare);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("❌ Statut invalide ! Utilisez PENDING, IN_PROGRESS ou COMPLETED.");
        }
    }

    // ✅ Supprimer un soin
    @Transactional
    @Override
    public void deleteHealthcare(Long id) {
        if (!healthcareRepository.existsById(id)) {
            throw new RuntimeException("❌ Traitement non trouvé !");
        }
        healthcareRepository.deleteById(id);
    }

    // ✅ Récupérer un soin par ID
    @Override
    public Optional<Healthcare> getHealthcareById(Long id) {
        return healthcareRepository.findById(id);
    }

    // ✅ Récupérer tous les soins
    @Override
    public List<Healthcare> getAllHealthcare() {
        return healthcareRepository.findAll();
    }
}
