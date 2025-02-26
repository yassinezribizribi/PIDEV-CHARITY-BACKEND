package tn.esprit.examen.nomPrenomClasseExamen.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Association;

import java.util.List;
import java.util.Optional;

public interface AssociationRepository extends JpaRepository<Association, Long> {
    Association findByIdAssociation(Long id);
    Optional<Object> findByStatus(Association.AssociationStatus associationStatus);
}