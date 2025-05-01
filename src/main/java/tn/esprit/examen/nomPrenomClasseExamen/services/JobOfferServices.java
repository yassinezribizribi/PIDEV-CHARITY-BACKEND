package tn.esprit.examen.nomPrenomClasseExamen.services;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.ForumRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.JobApplicationRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.JobOfferRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.SubscriberRepository;
import tn.esprit.examen.nomPrenomClasseExamen.dto.JobApplicationDto;
import tn.esprit.examen.nomPrenomClasseExamen.dto.JobOfferDto;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Forum;
import tn.esprit.examen.nomPrenomClasseExamen.entities.JobOffer;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Subscriber;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
@AllArgsConstructor
public class JobOfferServices {

    private final JobOfferRepository jobOfferRepository;
    private final ForumRepository forumRepository;
    private final SubscriberRepository subscriberRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final JobApplicationServices jobApplicationServices; // Inject JobApplicationServices

    private static final Logger logger = LoggerFactory.getLogger(JobOfferServices.class);

    public List<JobOffer> getAllJobOffers() {
        List<JobOffer> offers = jobOfferRepository.findAll();
        offers.forEach(o -> logger.info("DB Active State - ID: {} => {}", o.getIdJobOffer(), o.isActive()));
        return offers;
    }

    /**
     * Fetch job applications for a specific job offer.
     *
     * @param jobOfferId ID of the job offer to retrieve applications for
     * @return List of job application DTOs
     */
    public List<JobApplicationDto> getApplicationsForJobOffer(Long jobOfferId) {
        logger.info("Fetching job applications for job offer with ID: {}", jobOfferId);

        try {
            // Fetch the job offer by ID to ensure it exists
            JobOffer jobOffer = jobOfferRepository.findById(jobOfferId)
                    .orElseThrow(() -> new RuntimeException("JobOffer avec l'ID " + jobOfferId + " introuvable"));

            // Retrieve the applications for the given job offer
            List<JobApplicationDto> applications = jobApplicationServices.getApplicationsForJobOffer(jobOfferId);

            if (applications.isEmpty()) {
                logger.info("No applications found for job offer with ID: {}", jobOfferId);
            } else {
                logger.info("Found {} application(s) for job offer with ID: {}", applications.size(), jobOfferId);
            }

            return applications;
        } catch (Exception e) {
            logger.error("Error fetching applications for job offer with ID: {}: {}", jobOfferId, e.getMessage());
            throw new RuntimeException("Error fetching applications for job offer: " + e.getMessage());
        }
    }

    public JobOffer getJobOfferById(Long id) {
        logger.info("🔍 Recherche de l'offre d'emploi avec l'ID: {}", id);
        return jobOfferRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("JobOffer avec l'ID " + id + " introuvable"));
    }

    // JobOfferServices.java
    public List<JobOfferDto> getJobOffersByUser(Long userId) {
        logger.info("🔍 Recherche des offres d'emploi pour l'utilisateur avec ID: {}", userId);

        // Fetch job offers created by the specified user
        List<JobOffer> jobOffers = jobOfferRepository.findByCreatedBy_IdUser(userId);

        if (jobOffers.isEmpty()) {
            logger.info("Aucune offre d'emploi trouvée pour l'utilisateur avec ID: {}", userId);
        } else {
            logger.info("✅ Offre(s) d'emploi trouvée(s) pour l'utilisateur {} : {}", userId, jobOffers);
        }

        // Convert JobOffer entities to JobOfferDto objects using the static method
        return jobOffers.stream()
                .map(JobOfferDto::fromJobOffer) // Use the static method from JobOfferDto
                .collect(Collectors.toList());
    }


    public JobOffer createJobOffer(JobOfferDto jobOfferDto) {
        try {
            logger.info("📝 Création d'une nouvelle offre d'emploi: {}", jobOfferDto);

            // Récupération du forum à partir de forumId dans le DTO
            Forum forum = forumRepository.findById(jobOfferDto.getForumId())
                    .orElseThrow(() -> new RuntimeException("Forum avec ID " + jobOfferDto.getForumId() + " introuvable"));

            // Extract the current logged-in user (subscriber) from the token
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName(); // Get the username (which could be the user ID)
            Subscriber subscriber = subscriberRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Subscriber not found"));

            // Conversion DTO → Entité
            JobOffer jobOffer = jobOfferDto.toJobOffer();
            jobOffer.setForum(forum); // Association automatique avec le forum récupéré
            jobOffer.setCreatedBy(subscriber); // Set the user who created the job offer
            jobOffer.setCreatedAt(LocalDateTime.now()); // Set the creation timestamp
            jobOffer.setActive(true); // Set the job offer as active by default

            JobOffer savedJobOffer = jobOfferRepository.save(jobOffer);

            logger.info("✅ Offre créée avec succès: {}", savedJobOffer);
            return savedJobOffer;
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la création de l'offre: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la création de l'offre: " + e.getMessage());
        }
    }

    public JobOffer updateJobOffer(Long id, JobOfferDto jobOfferDto) {
        logger.info("🔄 Mise à jour de l'offre d'emploi avec ID: {}", id);
        JobOffer jobOffer = getJobOfferById(id);

        // Mise à jour des champs à partir du DTO
        jobOffer.setTitle(jobOfferDto.getTitle());
        jobOffer.setDescription(jobOfferDto.getDescription());
        jobOffer.setRequirements(jobOfferDto.getRequirements());
        jobOffer.setActive(jobOfferDto.isActive());

        // Vérification et mise à jour du forum si nécessaire
        if (jobOfferDto.getForumId() != null) {
            Forum forum = forumRepository.findById(jobOfferDto.getForumId())
                    .orElseThrow(() -> new RuntimeException("Forum avec ID " + jobOfferDto.getForumId() + " introuvable"));
            jobOffer.setForum(forum);
        }


        JobOffer updated = jobOfferRepository.save(jobOffer);
        logger.info("✅ Offre mise à jour avec succès: {}", updated);
        return updated;
    }

    public void deleteJobOffer(Long id) {
        logger.info("🗑 Suppression de l'offre avec ID: {}", id);
        jobOfferRepository.deleteById(id);
        logger.info("✅ Offre supprimée avec succès !");
    }

    public void reportJobOffer(Long jobOfferId, Long reporterId) {
        JobOffer jobOffer = jobOfferRepository.findById(jobOfferId)
                .orElseThrow(() -> new RuntimeException("Job offer not found"));

        Subscriber jobOwner = jobOffer.getCreatedBy();
        Subscriber reporter = subscriberRepository.findByIdUser(reporterId)
                .orElseThrow(() -> new RuntimeException("Reporter not found"));

        // Check if reporter is not the job owner
        if (reporter.getIdUser().equals(jobOwner.getIdUser())) {
            throw new RuntimeException("You cannot report your own job offer");
        }

        // Initialize reportCount if null
        if (jobOffer.getReportCount() == null) {
            jobOffer.setReportCount(0);
        }

        // Increment report count
        jobOffer.setReportCount(jobOffer.getReportCount() + 1);
        jobOfferRepository.save(jobOffer);

        // If report count reaches 3, mark the job owner for review
        if (jobOffer.getReportCount() >= 3) {
            jobOwner.setBanreason("Multiple job offer reports");
            subscriberRepository.save(jobOwner);
        }
    }

    public Integer getJobOfferReportCount(Long jobOfferId) {
        JobOffer jobOffer = jobOfferRepository.findById(jobOfferId)
                .orElseThrow(() -> new RuntimeException("Job offer not found"));
        return jobOffer.getReportCount() != null ? jobOffer.getReportCount() : 0;
    }

    @Transactional
    public void banUser(Long userId, boolean isBanned, String banReason) {
        Subscriber subscriber = subscriberRepository.findById(userId)
                .orElseThrow(() -> new AssociationServices.ResourceNotFoundException("User not found with id: " + userId));

        subscriber.setIsBanned(isBanned);
        subscriber.setBanreason(isBanned ? banReason : null);

        subscriberRepository.save(subscriber);
    }
    // In JobApplicationServices.java


}