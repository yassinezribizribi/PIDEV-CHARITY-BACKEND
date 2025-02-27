package tn.esprit.examen.nomPrenomClasseExamen.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Subscription;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
}