package tn.esprit.examen.nomPrenomClasseExamen.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.examen.nomPrenomClasseExamen.entities.JobOffer;

import java.util.Optional;

public interface JobOfferRepository extends JpaRepository<JobOffer, Long> {
 //   @Query("SELECT j FROM JobOffer j WHERE j.idJobOffer = :id")
   // Optional<JobOffer> findById(@Param("id") Long id);

}