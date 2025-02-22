package tn.esprit.examen.nomPrenomClasseExamen.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Event;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
        public List<Event> findByAssociation_IdAssociation(Long associationId);
}