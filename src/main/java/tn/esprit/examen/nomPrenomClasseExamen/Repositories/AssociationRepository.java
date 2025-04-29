package tn.esprit.examen.nomPrenomClasseExamen.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Association;

import java.util.Optional;

public interface AssociationRepository extends JpaRepository<Association, Long> {
    Optional<Association> findByAssociationAddressContainingIgnoreCase(String location);

}