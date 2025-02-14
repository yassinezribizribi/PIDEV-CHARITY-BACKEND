package tn.esprit.examen.nomPrenomClasseExamen.Repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Role;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Subscriber;

import java.util.List;

public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {
}
