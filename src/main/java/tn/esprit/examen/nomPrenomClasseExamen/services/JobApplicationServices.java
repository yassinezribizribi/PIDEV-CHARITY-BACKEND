package tn.esprit.examen.nomPrenomClasseExamen.services;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.JobOfferRepository;
import tn.esprit.examen.nomPrenomClasseExamen.dto.JobApplicationDto;
import tn.esprit.examen.nomPrenomClasseExamen.entities.JobApplication;
import tn.esprit.examen.nomPrenomClasseExamen.entities.JobApplicationStatus;
import tn.esprit.examen.nomPrenomClasseExamen.entities.JobOffer;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Subscriber;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.JobApplicationRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.SubscriberRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobApplicationServices {
    private static final Logger logger = LoggerFactory.getLogger(JobApplicationServices.class);

    private final JobApplicationRepository jobApplicationRepository;
    private final SubscriberRepository subscriberRepository;
    private final JobOfferRepository jobOfferRepository;
    private final EmailService emailService;

    // Convert entity to DTO
    private JobApplicationDto convertToDto(JobApplication jobApplication) {
        return new JobApplicationDto(
                jobApplication.getIdApplication(),
                jobApplication.getApplicationDate(),
                jobApplication.getJobApplicationStatus(),
                jobApplication.getJobOffer().getIdJobOffer(),
                jobApplication.getApplicant() // Ensure getId() exists in Subscriber
        );
    }

    // Get all applications
    public List<JobApplicationDto> getAllJobApplications() {
        return jobApplicationRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Get application by ID
    public JobApplicationDto getJobApplicationById(Long id) {
        return jobApplicationRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(() -> new RuntimeException("Job Application not found"));
    }

    // Get applications by subscriber
    public List<JobApplicationDto> getApplicationsBySubscriber(Long idUser) {
        List<JobApplication> applications = jobApplicationRepository.findByApplicant_IdUser(idUser);
        return applications.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<JobApplicationDto> getApplicationsForUserJobOffers(Long userId) {
        List<JobApplication> applications = jobApplicationRepository.findByJobOffer_CreatedBy_IdUser(userId);

        return applications.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Create job application
    public JobApplicationDto applyForJob(Long jobOfferId, Long applicantId) {
        JobOffer jobOffer = jobOfferRepository.findById(jobOfferId)
                .orElseThrow(() -> new NoSuchElementException("JobOffer not found"));

        Subscriber applicant = subscriberRepository.findById(applicantId)
                .orElseThrow(() -> new NoSuchElementException("Subscriber not found"));

        // Vérifier si l'utilisateur est le créateur de l'offre d'emploi
        if (jobOffer.getCreatedBy().getIdUser().equals(applicant.getIdUser())) {
            throw new IllegalStateException("Vous ne pouvez pas postuler à votre propre offre d'emploi.");
        }

        // Vérifier si l'utilisateur a déjà postulé
        if (jobApplicationRepository.existsByApplicantAndJobOffer(applicant, jobOffer)) {
            throw new IllegalStateException("Vous avez déjà postulé à cette offre.");
        }

        JobApplication jobApplication = new JobApplication();
        jobApplication.setApplicationDate(LocalDateTime.now());
        jobApplication.setJobApplicationStatus(JobApplicationStatus.PENDING);
        jobApplication.setJobOffer(jobOffer);
        jobApplication.setApplicant(applicant);

        jobApplication = jobApplicationRepository.save(jobApplication);

        return convertToDto(jobApplication);
    }

    // Delete job application
    public void deleteJobApplication(Long id) {
        jobApplicationRepository.deleteById(id);
    }

    // Get applications for a specific job offer
    public List<JobApplicationDto> getApplicationsForJobOffer(Long jobOfferId) {
        // Fetch job offers to ensure the job offer exists
        JobOffer jobOffer = jobOfferRepository.findById(jobOfferId)
                .orElseThrow(() -> new RuntimeException("JobOffer with ID " + jobOfferId + " not found"));

        // Fetch applications for the specific job offer
        List<JobApplication> jobApplications = jobApplicationRepository.findByJobOffer(jobOffer);
        return jobApplications.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public JobApplicationDto acceptApplication(Long applicationId) {
        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new NoSuchElementException("Job Application not found"));

        application.setJobApplicationStatus(JobApplicationStatus.ACCEPTED);
        application = jobApplicationRepository.save(application);
        sendApprovalEmail(application);


        return convertToDto(application);
    }
    private void sendApprovalEmail(JobApplication application) {
        try {
            String applicantEmail = application.getApplicant().getEmail();
            String subject = "Your Job Application Has Been Approved";
            String body = buildApprovalEmailBody(application);

            emailService.sendEmail(applicantEmail, subject, body);
        } catch (Exception e) {
            // Log the error but don't fail the operation
            logger.error("Failed to send approval email", e);
        }
    }

    private String buildApprovalEmailBody(JobApplication application) {
        return """
            <html>
                <body>
                    <h2>Congratulations!</h2>
                    <p>Dear %s %s,</p>
                    <p>We are pleased to inform you that your application for the position <strong>%s</strong> has been approved.</p>
                    <p>Our team will contact you shortly with further details.</p>
                    <p>Best regards,</p>
                    <p>The Hiring Team</p>
                </body>
            </html>
            """.formatted(
                application.getApplicant().getFirstName(),
                application.getApplicant().getLastName(),
                application.getJobOffer().getTitle()
        );
    }

    public JobApplicationDto rejectApplication(Long applicationId, String rejectionReason) {
        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new NoSuchElementException("Job Application not found"));

        application.setJobApplicationStatus(JobApplicationStatus.REJECTED);

        application = jobApplicationRepository.save(application);

        return convertToDto(application);
    }
}
