package tn.esprit.examen.nomPrenomClasseExamen.services;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.JobOfferRepository;
import tn.esprit.examen.nomPrenomClasseExamen.entities.JobApplication;
import tn.esprit.examen.nomPrenomClasseExamen.entities.JobOffer;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Subscriber;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.JobApplicationRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.SubscriberRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class JobApplicationServices {

    JobApplicationRepository jobApplicationRepository;
    SubscriberRepository subscriberRepository;
    JobOfferRepository jobOfferRepository;
    private static final Logger logger = LoggerFactory.getLogger(JobApplicationServices.class);

    public List<JobApplication> getAllJobApplications() {
        logger.info("📢 Fetching all job applications...");
        List<JobApplication> jobApplications = jobApplicationRepository.findAll();
        logger.info("✅ Found {} job applications.", jobApplications.size());
        return jobApplications;
    }

    public JobApplication getJobApplicationById(Long id) {
        logger.info("🔍 Fetching job application with ID: {}", id);
        JobApplication jobApplication = jobApplicationRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("❌ JobApplication with ID {} not found.", id);
                    return new RuntimeException("JobApplication not found");
                });
        logger.info("✅ Found job application: {}", jobApplication);
        return jobApplication;
    }

    public List<JobApplication> getApplicationsBySubscriber(Long subscriberId) {
        logger.info("🔍 Fetching applications for subscriber with ID: {}", subscriberId);
        Subscriber subscriber = subscriberRepository.findById(subscriberId)
                .orElseThrow(() -> {
                    logger.error("❌ Subscriber with ID {} not found.", subscriberId);
                    return new RuntimeException("Subscriber not found");
                });
        List<JobApplication> applications = jobApplicationRepository.findBySubscriber(subscriber);
        logger.info("✅ Found {} applications for subscriber: {}", applications.size(), subscriber);
        return applications;
    }

    public JobApplication createJobApplication(JobApplication jobApplication) {
        logger.info("📝 Creating a new job application: {}", jobApplication);


        if (jobApplication == null) {
            logger.error("❌ JobApplication object is null!");
            throw new RuntimeException("JobApplication object is null");
        }

        if (jobApplication.getJobOffer() == null || jobApplication.getJobOffer().getIdJobOffer() == null) {
            logger.error("❌ JobOffer is null or has no ID!");
            throw new RuntimeException("JobOffer is null or has no ID");
        }

        if (jobApplication.getSubscriber() == null || jobApplication.getSubscriber().getIdUser() == null) {
            logger.error("❌ Subscriber is null or has no ID!");
            throw new RuntimeException("Subscriber is null or has no ID");
        }

        Long jobOfferId = jobApplication.getJobOffer().getIdJobOffer();
        JobOffer managedJobOffer = jobOfferRepository.findById(jobOfferId)
                .orElseThrow(() -> {
                    logger.error("❌ JobOffer with ID {} does not exist!", jobOfferId);
                    return new RuntimeException("JobOffer with ID " + jobOfferId + " does not exist");
                });

        Long subscriberId = jobApplication.getSubscriber().getIdUser();
        Subscriber managedSubscriber = subscriberRepository.findById(subscriberId)
                .orElseThrow(() -> {
                    logger.error("❌ Subscriber with ID {} does not exist!", subscriberId);
                    return new RuntimeException("Subscriber with ID " + subscriberId + " does not exist");
                });

        // Set the managed entities into the JobApplication
        jobApplication.setJobOffer(managedJobOffer);
        jobApplication.setSubscriber(managedSubscriber);

        try {
            JobApplication savedJobApplication = jobApplicationRepository.save(jobApplication);
            logger.info("✅ Job application created successfully: {}", savedJobApplication);
            return savedJobApplication;
        } catch (Exception e) {
            logger.error("❌ Error while creating job application: {}", e.getMessage(), e);
            throw new RuntimeException("Error while creating job application: " + e.getMessage());
        }
    }
    public void deleteJobApplication(Long id) {
        logger.info("🗑 Deleting job application with ID: {}", id);
        try {
            jobApplicationRepository.deleteById(id);
            logger.info("✅ Job application deleted successfully!");
        } catch (Exception e) {
            logger.error("❌ Error while deleting job application: {}", e.getMessage(), e);
            throw new RuntimeException("Error while deleting job application: " + e.getMessage());
        }
    }
}