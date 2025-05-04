package tn.esprit.examen.nomPrenomClasseExamen.dto;

import lombok.Getter;
import tn.esprit.examen.nomPrenomClasseExamen.entities.JobApplication;
import tn.esprit.examen.nomPrenomClasseExamen.entities.JobApplicationStatus;
import java.time.LocalDateTime;

@Getter
public class JobApplicationDto {
    private final Long idApplication;
    private final LocalDateTime applicationDate;
    private final JobApplicationStatus status;
    private final Long jobOfferId;
    private final String jobTitle;
    private final ApplicantDto applicant;

    public JobApplicationDto(Long idApplication,
                             LocalDateTime applicationDate,
                             JobApplicationStatus status,
                             Long jobOfferId,
                             String jobTitle,
                             ApplicantDto applicant) {
        this.idApplication = idApplication;
        this.applicationDate = applicationDate;
        this.status = status;
        this.jobOfferId = jobOfferId;
        this.jobTitle = jobTitle;
        this.applicant = applicant;
    }

    public static JobApplicationDto fromEntity(JobApplication application) {
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
}