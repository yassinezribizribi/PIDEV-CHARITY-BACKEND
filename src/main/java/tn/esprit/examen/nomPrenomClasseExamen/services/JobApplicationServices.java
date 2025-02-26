package tn.esprit.examen.nomPrenomClasseExamen.services;

import lombok.RequiredArgsConstructor;
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

    private final JobApplicationRepository jobApplicationRepository;
    private final SubscriberRepository subscriberRepository;
    private final JobOfferRepository jobOfferRepository;

    // Convert entity to DTO
    private JobApplicationDto convertToDto(JobApplication jobApplication) {
        return new JobApplicationDto(
                jobApplication.getIdApplication(),
                jobApplication.getApplicationDate(),
                jobApplication.getJobApplicationStatus(),
                jobApplication.getJobOffer().getIdJobOffer(),
                jobApplication.getApplicant().getIdUser() // Ensure getId() exists in Subscriber
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


    // Create job application
    public JobApplicationDto applyForJob(Long jobOfferId, Long applicantId) {
        JobOffer jobOffer = jobOfferRepository.findById(jobOfferId)
                .orElseThrow(() -> new NoSuchElementException("JobOffer not found"));

        Subscriber applicant = subscriberRepository.findById(applicantId)
                .orElseThrow(() -> new NoSuchElementException("Subscriber not found"));

        // Check if the user has already applied
        if (jobApplicationRepository.existsByApplicantAndJobOffer(applicant, jobOffer)) {
            throw new IllegalStateException("You have already applied for this job offer.");
        }

        // Create and save JobApplication
        JobApplication jobApplication = new JobApplication();
        jobApplication.setApplicationDate(LocalDateTime.now());
        jobApplication.setJobApplicationStatus(JobApplicationStatus.PENDING);
        jobApplication.setJobOffer(jobOffer);
        jobApplication.setApplicant(applicant);

        jobApplication = jobApplicationRepository.save(jobApplication);

        return convertToDto(jobApplication); // Ensure correct DTO conversion
    }


    // Delete job application
    public void deleteJobApplication(Long id) {
        jobApplicationRepository.deleteById(id);
    }
}
