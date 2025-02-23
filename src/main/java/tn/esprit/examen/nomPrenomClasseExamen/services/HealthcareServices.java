package tn.esprit.examen.nomPrenomClasseExamen.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.examen.nomPrenomClasseExamen.dto.HealthcareDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Healthcare;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Subscriber;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.HealthcareRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.SubscriberRepository;

import java.util.List;
import java.util.Optional;

@Service  // ✅ Ajout correct du @Service
public class HealthcareServices implements IHealthcareServices {

    @Autowired
    private HealthcareRepository healthcareRepository;

    @Autowired
    private SubscriberRepository subscriberRepository;

    @Override
    public Healthcare addHealthcare(HealthcareDTO healthcareDTO) {
        Healthcare healthcare = new Healthcare();
        healthcare.setHistory(healthcareDTO.getHistory());
        healthcare.setTreatmentPlan(healthcareDTO.getTreatmentPlan());
        healthcare.setTerminalDisease(healthcareDTO.getTerminalDisease());
        healthcare.setBookingDate(healthcareDTO.getBookingDate());

        if (healthcareDTO.getSubscriberId() != null) {
            Optional<Subscriber> subscriber = subscriberRepository.findById(healthcareDTO.getSubscriberId());
            subscriber.ifPresent(healthcare::setSubscriber);
        }

        return healthcareRepository.save(healthcare);
    }

    @Override
    public Healthcare updateHealthcare(Long id, HealthcareDTO healthcareDTO) {
        Optional<Healthcare> existingHealthcare = healthcareRepository.findById(id);
        if (existingHealthcare.isPresent()) {
            Healthcare healthcare = existingHealthcare.get();
            healthcare.setHistory(healthcareDTO.getHistory());
            healthcare.setTreatmentPlan(healthcareDTO.getTreatmentPlan());
            healthcare.setTerminalDisease(healthcareDTO.getTerminalDisease());
            healthcare.setBookingDate(healthcareDTO.getBookingDate());

            if (healthcareDTO.getSubscriberId() != null) {
                Optional<Subscriber> subscriber = subscriberRepository.findById(healthcareDTO.getSubscriberId());
                subscriber.ifPresent(healthcare::setSubscriber);
            }

            return healthcareRepository.save(healthcare);
        } else {
            throw new RuntimeException("Healthcare not found with ID: " + id);
        }
    }

    @Override
    public void deleteHealthcare(Long id) {
        if (healthcareRepository.existsById(id)) {
            healthcareRepository.deleteById(id);
        } else {
            throw new RuntimeException("Healthcare not found with ID: " + id);
        }
    }

    @Override
    public Optional<Healthcare> getHealthcareById(Long id) {
        return healthcareRepository.findById(id);
    }

    @Override
    public List<Healthcare> getAllHealthcare() {
        return healthcareRepository.findAll();
    }
}
