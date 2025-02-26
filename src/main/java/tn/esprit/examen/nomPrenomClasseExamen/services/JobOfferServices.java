package tn.esprit.examen.nomPrenomClasseExamen.services;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.ForumRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.JobOfferRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.SubscriberRepository; // Add Subscriber repository to fetch the user
import tn.esprit.examen.nomPrenomClasseExamen.dto.JobOfferDto;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Forum;
import tn.esprit.examen.nomPrenomClasseExamen.entities.JobOffer;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Subscriber;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class JobOfferServices {

    private final JobOfferRepository jobOfferRepository;
    private final ForumRepository forumRepository;
    private final SubscriberRepository subscriberRepository; // To fetch the subscriber
    private static final Logger logger = LoggerFactory.getLogger(JobOfferServices.class);

    public List<JobOffer> getAllJobOffers() {
        logger.info("📢 Récupération de toutes les offres d'emploi...");
        return jobOfferRepository.findAll();
    }

    public JobOffer getJobOfferById(Long id) {
        logger.info("🔍 Recherche de l'offre d'emploi avec l'ID: {}", id);
        return jobOfferRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("JobOffer avec l'ID " + id + " introuvable"));
    }

    public JobOffer createJobOffer(JobOfferDto jobOfferDto) {
        try {
            logger.info("📝 Création d'une nouvelle offre d'emploi: {}", jobOfferDto);

            // Récupération du forum avec ID 1
            Forum forum = forumRepository.findById(1L)
                    .orElseThrow(() -> new RuntimeException("Forum avec ID 1 introuvable"));

            // Extract the current logged-in user (subscriber) from the token
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName(); // Get the username (which could be the user ID)
            Subscriber subscriber = subscriberRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Subscriber not found"));

            // Conversion DTO → Entité
            JobOffer jobOffer = jobOfferDto.toJobOffer();
            jobOffer.setForum(forum); // Association automatique
            jobOffer.setCreatedBy(subscriber); // Set the user who created the job offer
            jobOffer.setCreatedAt(LocalDateTime.now()); // Set the creation timestamp

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
        if (jobOfferDto.getForum() != null) {
            jobOffer.setForum(jobOfferDto.getForum());
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
}
