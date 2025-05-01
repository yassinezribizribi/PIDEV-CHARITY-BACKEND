package tn.esprit.examen.nomPrenomClasseExamen.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Association;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Subscriber;

import java.util.List;
import java.util.Optional;

public interface AssociationRepository extends JpaRepository<Association, Long>, JpaSpecificationExecutor<Association> {
    Association findByIdAssociation(Long id);
    Optional<Association> findBySubscriber(Subscriber subscriber);

    Optional<Association> findBySubscriberIdUser(Long idUser);

    Optional<Object> findByStatus(Association.AssociationStatus associationStatus);
    @Query("SELECT DISTINCT a FROM Association a " +
                  "LEFT JOIN FETCH a.partners " +
                  "WHERE a.idAssociation = :id")
    Optional<Association> findByIdWithPartners(@Param("id") Long id);
    // In AssociationRepository interface
    @Query("SELECT a FROM Association a " +
            "WHERE a.idAssociation <> :currentId " +
            "AND a NOT IN (SELECT p FROM Association a2 JOIN a2.partners p WHERE a2.idAssociation = :currentId)")
    List<Association> findNonPartnerAssociations(@Param("currentId") Long currentId);


    Optional<Association> findByAssociationAddress(String associationAddress);

}