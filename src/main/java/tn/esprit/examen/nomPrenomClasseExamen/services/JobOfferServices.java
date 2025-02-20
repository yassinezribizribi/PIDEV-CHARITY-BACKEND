package tn.esprit.examen.nomPrenomClasseExamen.services;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.ForumRepository;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Forum;
import tn.esprit.examen.nomPrenomClasseExamen.entities.JobOffer;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.JobOfferRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobOfferServices {

    private final JobOfferRepository jobOfferRepository;
    private final ForumRepository forumRepository;
    private static final Logger logger = LoggerFactory.getLogger(JobOfferServices.class);

    public List<JobOffer> getAllJobOffers() {
        logger.info("📢 Récupération de toutes les offres d'emploi...");
        return jobOfferRepository.findAll();
    }

    public JobOffer getJobOfferById(Long id) {
        logger.info("Recherche de l'offre d'emploi avec l'ID: {}", id);
        return jobOfferRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("JobOffer avec l'ID " + id + " introuvable"));
    }


    public JobOffer createJobOffer(JobOffer jobOffer) {
        try {
            logger.info("📝 Création d'une nouvelle offre d'emploi: {}", jobOffer);

            if (forumRepository == null) {
                logger.error("❌ forumRepository is null!");
                throw new RuntimeException("forumRepository is null!");
            }

            if (jobOffer.getForum() != null && jobOffer.getForum().getIdForum() == null) {
                logger.info("💾 Sauvegarde du Forum associé...");
                Forum savedForum = forumRepository.save(jobOffer.getForum());
                jobOffer.setForum(savedForum);
            }

            JobOffer savedJobOffer = jobOfferRepository.save(jobOffer);
            logger.info("✅ Offre créée avec succès: {}", savedJobOffer);
            return savedJobOffer;
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la création de l'offre: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la création de l'offre: " + e.getMessage());
        }
    }

    public JobOffer updateJobOffer(Long id, JobOffer updatedJobOffer) {
        logger.info("🔄 Mise à jour de l'offre d'emploi avec ID: {}", id);
        JobOffer jobOffer = getJobOfferById(id);

        jobOffer.setTitle(updatedJobOffer.getTitle());
        jobOffer.setDescription(updatedJobOffer.getDescription());
        jobOffer.setRequirements(updatedJobOffer.getRequirements());
        jobOffer.setActive(updatedJobOffer.isActive());

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
