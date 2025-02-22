package tn.esprit.examen.nomPrenomClasseExamen.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Crisis;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Subscriber;

import java.util.List;

public interface CrisisRepository extends JpaRepository<Crisis, Long> {
    List<Crisis> findBySubscriber(Subscriber subscriber);


}