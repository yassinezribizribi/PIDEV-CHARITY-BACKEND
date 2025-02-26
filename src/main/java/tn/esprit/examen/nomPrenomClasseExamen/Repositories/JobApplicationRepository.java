package tn.esprit.examen.nomPrenomClasseExamen.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.examen.nomPrenomClasseExamen.entities.JobApplication;
import tn.esprit.examen.nomPrenomClasseExamen.entities.JobOffer;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Subscriber;

import java.util.List;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByApplicant(Subscriber applicant);
    List<JobApplication> findByApplicant_IdUser(Long idUser);

    boolean existsByApplicantAndJobOffer(Subscriber applicant, JobOffer jobOffer);


}
