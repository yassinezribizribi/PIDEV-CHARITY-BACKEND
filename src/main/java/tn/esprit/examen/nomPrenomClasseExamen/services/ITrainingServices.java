package tn.esprit.examen.nomPrenomClasseExamen.services;

import jakarta.validation.Valid;
import tn.esprit.examen.nomPrenomClasseExamen.dto.TrainingDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Training;

import java.util.List;
import java.util.Optional;

public interface ITrainingServices {
    Training addTraining(@Valid TrainingDTO trainingDTO);
    Training updateTraining(@Valid Long id, Training updatedTraining);
    Optional<Training> getTrainingById(@Valid Long id);
    List<Training> getAllTrainings();
    List<Training> getTrainingsBySubscriber(@Valid Long subscriberId);
    public void deleteTraining(@Valid Long id);
    Training addSubscriberToTraining(@Valid Long trainingId, Long subscriberId);
}
