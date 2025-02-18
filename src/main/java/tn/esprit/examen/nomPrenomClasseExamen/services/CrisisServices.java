package tn.esprit.examen.nomPrenomClasseExamen.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.CrisisRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.SubscriberRepository;
import tn.esprit.examen.nomPrenomClasseExamen.dto.CrisisDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Crisis;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Subscriber;

import java.util.List;
import java.util.Optional;
@Service
@Component
@Slf4j
public class CrisisServices implements ICrisisServices {

    @Autowired
    private CrisisRepository crisisRepository;

    @Autowired
    private SubscriberRepository subscriberRepository;

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

        // Récupérer l'utilisateur (Subscriber) par son ID
        Subscriber subscriber = subscriberRepository.findById(crisisDTO.getSubscriberId())
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'ID : " + crisisDTO.getSubscriberId()));
        crisis.setSubscriber(subscriber);

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


    public Optional<Crisis> getCrisisById(Long id) {
        return crisisRepository.findById(id);
    }

    @Override
    public List<Crisis> getAllCrises() {
        return crisisRepository.findAll();
    }

    @Override
    public List<Crisis> getCrisesBySubscriber(Long subscriberId) {
        return null;
    }


}








