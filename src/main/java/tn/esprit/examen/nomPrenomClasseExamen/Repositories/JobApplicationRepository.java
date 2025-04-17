package tn.esprit.examen.nomPrenomClasseExamen.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tn.esprit.examen.nomPrenomClasseExamen.entities.JobApplication;
import tn.esprit.examen.nomPrenomClasseExamen.entities.JobApplicationStatus;
import tn.esprit.examen.nomPrenomClasseExamen.entities.JobOffer;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Subscriber;

import java.time.LocalDateTime;
import java.util.List;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    // Find applications by applicant
    List<JobApplication> findByApplicant(Subscriber applicant);

    // Find applications by applicant ID
    List<JobApplication> findByApplicant_IdUser(Long idUser);

    // Check if application exists for applicant and job offer
    boolean existsByApplicantAndJobOffer(Subscriber applicant, JobOffer jobOffer);

    // Find applications for job offers created by specific user
    List<JobApplication> findByJobOffer_CreatedBy_IdUser(Long userId);

    // Find applications by job offer
    List<JobApplication> findByJobOffer(JobOffer jobOffer);

    // Find applications by job offer ID
    List<JobApplication> findByJobOffer_IdJobOffer(Long jobOfferId);

    // Find applications by status
    List<JobApplication> findByJobApplicationStatus(JobApplicationStatus status);

    // Find applications by status and date range
    List<JobApplication> findByJobApplicationStatusAndApplicationDateBetween(
            JobApplicationStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    // Custom query to count applications by status for a specific job offer
    @Query("SELECT COUNT(ja) FROM JobApplication ja WHERE ja.jobOffer.idJobOffer = :jobOfferId AND ja.jobApplicationStatus = :status")
    long countByJobOfferAndStatus(Long jobOfferId, JobApplicationStatus status);

    // Find applications with pending status older than specific date
    List<JobApplication> findByJobApplicationStatusAndApplicationDateBefore(
            JobApplicationStatus status,
            LocalDateTime date
    );
}