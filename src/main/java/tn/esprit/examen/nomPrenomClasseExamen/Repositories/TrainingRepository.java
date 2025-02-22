package tn.esprit.examen.nomPrenomClasseExamen.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Training;

import java.util.List;

public interface TrainingRepository extends JpaRepository<Training, Long> {
    List<Training> findBySubscribers_IdUser(Long id);

}