package tn.esprit.examen.nomPrenomClasseExamen.services;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.SubscriberRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.TrainingRepository;
import tn.esprit.examen.nomPrenomClasseExamen.dto.TrainingDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Role;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Subscriber;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Training;

import java.util.*;

@Slf4j
@Service
public class TrainigServices implements ITrainingServices {
    @Autowired
    private TrainingRepository trainingRepository;

    @Autowired
    private SubscriberRepository subscriberRepository;

    @Override
    public Training addTraining(@Valid TrainingDTO trainingDTO) {
        if (trainingDTO == null) {
            throw new IllegalArgumentException("Les données de la formation sont nulles.");
        }

        log.info("Ajout d'une nouvelle formation : {}", trainingDTO.getTrainingName());

        // Ajouter des validations si nécessaire pour l'association ou d'autres entités comme Subscriber

        Training training = new Training();
        training.setTrainingName(trainingDTO.getTrainingName());
        training.setDescription(trainingDTO.getDescription());
        training.setDuration(trainingDTO.getDuration());
        training.setCapacity(trainingDTO.getCapacity());
        training.setLevel(trainingDTO.getLevel());
        training.setType(trainingDTO.getType());
        training.setSessionDate(trainingDTO.getSessionDate());

        return trainingRepository.save(training);
    }

    @Override
    public Training updateTraining(@Valid Long id, Training updatedTraining) {
        return trainingRepository.findById(id)
                .map(existingTraining -> {
                    existingTraining.setTrainingName(updatedTraining.getTrainingName());
                    existingTraining.setDescription(updatedTraining.getDescription());
                    existingTraining.setDuration(updatedTraining.getDuration());
                    existingTraining.setCapacity(updatedTraining.getCapacity());
                    existingTraining.setLevel(updatedTraining.getLevel());
                    existingTraining.setType(updatedTraining.getType());
                    existingTraining.setSessionDate(updatedTraining.getSessionDate());

                    log.info("Mise à jour de la formation avec ID {}", id);
                    return trainingRepository.save(existingTraining);
                }).orElseThrow(() -> new RuntimeException("Formation introuvable avec l'ID: " + id));
    }

    @Override
    public void deleteTraining(@Valid Long id) {
        if (!trainingRepository.existsById(id)) {
            throw new RuntimeException("Formation introuvable avec l'ID: " + id);
        }
        trainingRepository.deleteById(id);
        log.info("Suppression de la formation avec ID {}", id);
    }

    @Override
    public Optional<Training> getTrainingById(@Valid Long id) {
        return trainingRepository.findById(id);
    }

    @Override
    public List<Training> getAllTrainings() {
        return trainingRepository.findAll();
    }

    @Override
    public List<Training> getTrainingsBySubscriber(@Valid Long subscriberId) {
        return trainingRepository.findBySubscribers_IdUser(subscriberId);
    }

    // Ajouter un abonné à une formation
    public Training addSubscriberToTraining(Long trainingId, Long subscriberId) {
        if (subscriberId == null || subscriberId == null) {
            throw new IllegalArgumentException("Les IDs du mentor et de l'abonné ne peuvent pas être nuls.");
        }

        // Récupérer la formation
        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new NoSuchElementException("Formation non trouvée avec l'ID: " + trainingId));

        // Vérifier que le mentor existe et qu'il a bien le rôle de MENTOR
        Subscriber mentor = subscriberRepository.findById(subscriberId)
                .orElseThrow(() -> new NoSuchElementException("Mentor non trouvé avec l'ID: " + subscriberId));



        // Récupérer l'abonné
        Subscriber subscriber = subscriberRepository.findById(subscriberId)
                .orElseThrow(() -> new NoSuchElementException("Utilisateur non trouvé avec l'ID: " + subscriberId));

        // Ajouter l'abonné à la formation sans écraser les anciens
        Set<Subscriber> subscriberSet = training.getSubscribers();
        if (subscriberSet == null) {
            subscriberSet = new HashSet<>();
        }

        if (!subscriberSet.contains(subscriber)) {
            subscriberSet.add(subscriber);
            training.setSubscribers(subscriberSet);
            return trainingRepository.save(training);
        } else {
            throw new IllegalArgumentException("L'abonné est déjà inscrit à cette formation.");
        }
    }

}
