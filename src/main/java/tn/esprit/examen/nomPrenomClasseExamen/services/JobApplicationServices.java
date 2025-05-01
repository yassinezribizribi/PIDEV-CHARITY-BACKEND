package tn.esprit.examen.nomPrenomClasseExamen.services;
import org.springframework.beans.factory.annotation.Value;

import lombok.RequiredArgsConstructor;

import org.hibernate.cfg.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.JobOfferRepository;
import tn.esprit.examen.nomPrenomClasseExamen.dto.ApplicantDto;
import tn.esprit.examen.nomPrenomClasseExamen.dto.JobApplicationDto;
import tn.esprit.examen.nomPrenomClasseExamen.entities.JobApplication;
import tn.esprit.examen.nomPrenomClasseExamen.entities.JobApplicationStatus;
import tn.esprit.examen.nomPrenomClasseExamen.entities.JobOffer;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Subscriber;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.JobApplicationRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.SubscriberRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    public List<JobApplicationDto> getAllJobApplications() {
        return jobApplicationRepository.findAll()
                .stream()
                .map(JobApplicationDto::fromEntity)
                .collect(Collectors.toList());
    }

    public JobApplicationDto getJobApplicationById(Long id) {
        JobApplication application = jobApplicationRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Job Application not found"));

        // Create a proper Applicant object in the DTO
        return new JobApplicationDto(
                application.getIdApplication(),
                application.getApplicationDate(),
                application.getJobApplicationStatus(),
                application.getJobOffer().getIdJobOffer(),
                application.getJobOffer().getTitle(),
                new ApplicantDto(
                        application.getApplicant().getIdUser(),
                        application.getApplicant().getFirstName(),
                        application.getApplicant().getLastName(),
                        application.getApplicant().getEmail(),
                        application.getApplicant().getTelephone()
                )
        );
    }


    public List<JobApplicationDto> getApplicationsBySubscriber(Long idUser) {
        List<JobApplication> applications = jobApplicationRepository.findByApplicantWithDetails(idUser);
        return applications.stream()
                .map(JobApplicationDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<JobApplicationDto> getApplicationsForEmployerOffers(Long userId) {
        List<JobApplication> applications = jobApplicationRepository.findApplicationsForEmployerOffers(userId);
        logger.info("Found {} applications for employer ID: {}", applications.size(), userId);
        return applications.stream()
                .map(JobApplicationDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<JobApplicationDto> getApplicationsForUserJobOffers(Long userId) {
        List<JobApplication> applications = jobApplicationRepository.findByJobOffer_CreatedBy_IdUser(userId);
        return applications.stream()
                .map(JobApplicationDto::fromEntity)
                .collect(Collectors.toList());
    }

    public JobApplicationDto applyForJob(Long jobOfferId, Long applicantId) {
        JobOffer jobOffer = jobOfferRepository.findById(jobOfferId)
                .orElseThrow(() -> new NoSuchElementException("JobOffer not found"));

        Subscriber applicant = subscriberRepository.findById(applicantId)
                .orElseThrow(() -> new NoSuchElementException("Subscriber not found"));

        if (jobOffer.getCreatedBy().getIdUser().equals(applicant.getIdUser())) {
            throw new IllegalStateException("You cannot apply to your own job offer");
        }

        if (jobApplicationRepository.existsByApplicantAndJobOffer(applicant, jobOffer)) {
            throw new IllegalStateException("You have already applied to this position");
        }

        // Just use regular setters
        JobApplication jobApplication = new JobApplication();
        jobApplication.setApplicationDate(LocalDateTime.now());
        jobApplication.setJobApplicationStatus(JobApplicationStatus.PENDING);
        jobApplication.setJobOffer(jobOffer);
        jobApplication.setApplicant(applicant);

        jobApplication = jobApplicationRepository.save(jobApplication);
        return JobApplicationDto.fromEntity(jobApplication);
    }


    public void deleteJobApplication(Long id) {
        jobApplicationRepository.deleteById(id);
    }

    public List<JobApplicationDto> getApplicationsForJobOffer(Long jobOfferId) {
        JobOffer jobOffer = jobOfferRepository.findById(jobOfferId)
                .orElseThrow(() -> new RuntimeException("JobOffer with ID " + jobOfferId + " not found"));

        return jobApplicationRepository.findByJobOffer(jobOffer)
                .stream()
                .map(JobApplicationDto::fromEntity)
                .collect(Collectors.toList());
    }

    public JobApplicationDto acceptApplication(Long applicationId) {
        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new NoSuchElementException("Job Application not found"));

        application.setJobApplicationStatus(JobApplicationStatus.ACCEPTED);
        application = jobApplicationRepository.save(application);
        sendApprovalEmail(application);

        return JobApplicationDto.fromEntity(application);
    }

    private void sendApprovalEmail(JobApplication application) {
        try {
            String imagePath = "src/main/resources/static/assets/images/logo-dark.png"; // Chemin relatif dans les ressources
            String contentId = "logoImage";  // Utilisé pour référencer l'image dans le HTML
            String emailBody = buildApprovalEmailBody(application); // Ton corps d'email

            emailService.sendEmailWithInlineImage(
                    application.getApplicant().getEmail(),
                    "Your Job Application Has Been Approved",
                    emailBody,
                    imagePath,  // Passe le chemin de l'image
                    contentId   // Utilise le Content-ID pour lier l'image
            );
        } catch (Exception e) {
            logger.error("Failed to send approval email for application ID: {}", application.getIdApplication(), e);
        }
    }
    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.assets-path}")
    private String assetsPath;
    private String buildApprovalEmailBody(JobApplication application) {
        // Pre-format the date to avoid formatting issues
        String formattedDate = application.getApplicationDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));

        // Using String concatenation instead of format() to avoid CSS interpretation issues
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "  <style>" +
                "    body {" +
                "      font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;" +
                "      line-height: 1.6;" +
                "      color: #333333;" +
                "      background-color: #f5f7fa;" +
                "      margin: 0;" +
                "      padding: 0;" +
                "    }" +
                "    .container {" +
                "      max-width: 600px;" +
                "      margin: 0 auto;" +
                "      background: white;" +
                "      border-radius: 8px;" +
                "      overflow: hidden;" +
                "      box-shadow: 0 4px 12px rgba(0,0,0,0.1);" +
                "    }" +
                "    .header {" +
                "      text-align: center;" +
                "      padding: 30px 20px 20px;" +
                "      background: linear-gradient(135deg, #2f55d4 0%, #3b82f6 100%);" +
                "      color: white;" +
                "    }" +
                "    .logo-container {" +
                "      margin-bottom: 15px;" +
                "    }" +
                "    .logo {" +
                "      max-width: 180px;" +
                "      height: auto;" +
                "    }" +
                "    .platform-name {" +
                "      font-size: 24px;" +
                "      font-weight: 700;" +
                "      margin: 10px 0 0;" +
                "      color: white;" +
                "      letter-spacing: 0.5px;" +
                "    }" +
                "    .content {" +
                "      padding: 30px;" +
                "    }" +
                "    h1 {" +
                "      color: #2f55d4;" +
                "      margin-bottom: 25px;" +
                "      font-size: 28px;" +
                "    }" +
                "    h2 {" +
                "      color: #3b82f6;" +
                "      font-size: 20px;" +
                "      margin-top: 30px;" +
                "      margin-bottom: 15px;" +
                "      border-bottom: 2px solid #eef2ff;" +
                "      padding-bottom: 8px;" +
                "    }" +
                "    .button {" +
                "      display: inline-block;" +
                "      padding: 14px 28px;" +
                "      background: linear-gradient(135deg, #2f55d4 0%, #3b82f6 100%);" +
                "      color: white;" +
                "      text-decoration: none;" +
                "      border-radius: 6px;" +
                "      margin: 25px 0;" +
                "      font-weight: 600;" +
                "      box-shadow: 0 4px 8px rgba(47, 85, 212, 0.2);" +
                "      transition: all 0.3s ease;" +
                "    }" +
                "    .button:hover {" +
                "      transform: translateY(-2px);" +
                "      box-shadow: 0 6px 12px rgba(47, 85, 212, 0.3);" +
                "    }" +
                "    ul {" +
                "      padding-left: 20px;" +
                "    }" +
                "    li {" +
                "      margin-bottom: 10px;" +
                "      color: #4b5563;" +
                "    }" +
                "    .footer {" +
                "      text-align: center;" +
                "      margin-top: 40px;" +
                "      font-size: 12px;" +
                "      color: #9ca3af;" +
                "      padding: 20px;" +
                "      background-color: #f9fafb;" +
                "      border-top: 1px solid #e5e7eb;" +
                "    }" +
                "    .highlight-box {" +
                "      background-color: #f0f5ff;" +
                "      border-left: 4px solid #2f55d4;" +
                "      padding: 15px;" +
                "      margin: 20px 0;" +
                "      border-radius: 0 4px 4px 0;" +
                "    }" +
                "  </style>" +
                "</head>" +
                "<body>" +
                "  <div class=\"container\">" +
                "    <div class=\"header\">" +
                "      <div class=\"logo-container\">" +
                "        <img src=\"cid:logoImage\" alt=\"Solidarity & Refugee Platform Logo\" class=\"logo\">" +
                "      </div>" +
                "      <h1 class=\"platform-name\">Solidarity & Refugee Platform</h1>" +
                "    </div>" +
                "    <div class=\"content\">" +
                "      <h1>Congratulations!</h1>" +
                "      <p>Dear " + application.getApplicant().getFirstName() + " " + application.getApplicant().getLastName() + ",</p>" +
                "      <div class=\"highlight-box\">" +
                "        <p>We are delighted to inform you that your application for the position of <strong style=\"color: #2f55d4;\">" +
                application.getJobOffer().getTitle() + "</strong> has been accepted!</p>" +
                "      </div>" +
                "      <h2>Next Steps</h2>" +
                "      <ul>" +
                "        <li>Our team will contact you shortly with additional details</li>" +
                "        <li>Please ensure your contact information is up to date</li>" +
                "        <li>Review any attached documents or requirements</li>" +
                "        <li>Prepare for your onboarding process</li>" +
                "      </ul>" +
                "      <h2>Position Details</h2>" +
                "      <p><strong style=\"color: #2f55d4;\">Job Title:</strong> " + application.getJobOffer().getTitle() + "</p>" +
                "      <p><strong style=\"color: #2f55d4;\">Application Date:</strong> " + formattedDate + "</p>" +
                "      <p><strong style=\"color: #2f55d4;\">Status:</strong> <span style=\"color: #10b981; font-weight: 600;\">Accepted</span></p>" +
                "      <div style=\"text-align: center;\">" +
                "        <a href=\"" + baseUrl + "/dashboard/applications\" class=\"button\">View Your Application</a>" +
                "      </div>" +
                "      <p style=\"margin-top: 30px;\">If you have any questions, please contact our support team at " +
                "      <a href=\"mailto:support@solidarity-platform.org\" style=\"color: #2f55d4;\">support@solidarity-platform.org</a></p>" +
                "      <p>Best regards,<br>" +
                "      <strong>The Solidarity & Refugee Team</strong></p>" +
                "    </div>" +
                "    <div class=\"footer\">" +
                "      <p>© 2023 Solidarity & Refugee Platform. All rights reserved.</p>" +
                "      <p>This email was sent automatically. Please do not reply.</p>" +
                "    </div>" +
                "  </div>" +
                "</body>" +
                "</html>";
    }
    public JobApplicationDto rejectApplication(Long applicationId, String rejectionReason) {
        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new NoSuchElementException("Job Application not found"));

        application.setJobApplicationStatus(JobApplicationStatus.REJECTED);
        application = jobApplicationRepository.save(application);

        return JobApplicationDto.fromEntity(application);
    }

    public List<JobApplicationDto> getUserApplications(Long userId) {
        return jobApplicationRepository.findByApplicant_IdUser(userId)
                .stream()
                .map(JobApplicationDto::fromEntity)
                .collect(Collectors.toList());
    }
}


