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

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

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
    public Training addSubscriberToTraining(@Valid Long trainingId, Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("L'ID de l'utilisateur ne peut pas être nul.");
        }

        // Trouver la formation
        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new NoSuchElementException("Formation non trouvée avec l'ID: " + trainingId));

        // Trouver l'utilisateur (bénévole ou mentor)
        Subscriber mentor = subscriberRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Utilisateur non trouvé avec l'ID: " + userId));

        // Vérifier si l'utilisateur est un mentor
        if (mentor.getRole() != Role.MENTOR) {
            throw new IllegalArgumentException("Seuls les mentors peuvent inscrire des réfugiés à une formation.");
        }

        // Trouver un réfugié à inscrire
        Optional<Subscriber> optionalRefugee = subscriberRepository.findByRole(Role.REFUGEE).stream().findFirst();

        if (optionalRefugee.isEmpty()) {
            throw new IllegalArgumentException("Aucun réfugié disponible pour inscription.");
        }

        Subscriber refugee = optionalRefugee.get();

        // Vérifier si le réfugié est déjà inscrit
        if (training.getSubscribers().contains(refugee)) {
            throw new IllegalArgumentException("Le réfugié est déjà inscrit à cette formation.");
        }

        // Ajouter le réfugié à la formation
        training.getSubscribers().add(refugee);
        log.info("✅ Le mentor {} a inscrit le réfugié {} à la formation {}",
                mentor.getFirstName() + " " + mentor.getLastName(),
                refugee.getFirstName() + " " + refugee.getLastName(),
                training.getTrainingName());

        return trainingRepository.save(training);
    }

}
