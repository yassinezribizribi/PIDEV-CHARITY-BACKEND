package tn.esprit.examen.nomPrenomClasseExamen.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Association;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Subscriber;

import java.util.List;
import java.util.Optional;

public interface AssociationRepository extends JpaRepository<Association, Long> {
    Association findByIdAssociation(Long id);
    Optional<Association> findBySubscriber(Subscriber subscriber);

    Optional<Association> findBySubscriberIdUser(Long idUser);

    Optional<Object> findByStatus(Association.AssociationStatus associationStatus);
    @Query("SELECT DISTINCT a FROM Association a " +
                  "LEFT JOIN FETCH a.partners " +
                  "WHERE a.idAssociation = :id")
    Optional<Association> findByIdWithPartners(@Param("id") Long id);
    @Query("SELECT a FROM Association a " +
            "WHERE a.idAssociation <> :currentId " +
            "AND a.status = 'APPROVED' " +
            "AND NOT EXISTS (SELECT p FROM Association a2 JOIN a2.partners p " +
            "WHERE a2.idAssociation = :currentId AND p.idAssociation = a.idAssociation)")
    List<Association> findPotentialPartners(@Param("currentId") Long currentId);
}