package tn.esprit.examen.nomPrenomClasseExamen.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Forum;
import tn.esprit.examen.nomPrenomClasseExamen.entities.JobApplication;
import tn.esprit.examen.nomPrenomClasseExamen.entities.JobOffer;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Subscriber;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobOfferDto implements Serializable {
    private Long idJobOffer;
    private String title;
    private String description;
    private String requirements;
    private boolean isActive;
    private Forum forum;
    private Set<JobApplication> jobApplications;

    // New fields for createdBy and createdAt
    private Long createdById;  // We will pass the user ID from the token
    private LocalDateTime createdAt;

    // Convert DTO to Entity
    public JobOffer toJobOffer() {
        JobOffer jobOffer = new JobOffer();
        jobOffer.setIdJobOffer(this.idJobOffer);
        jobOffer.setTitle(this.title);
        jobOffer.setDescription(this.description);
        jobOffer.setRequirements(this.requirements);
        jobOffer.setActive(this.isActive);
        jobOffer.setForum(this.forum);
        jobOffer.setJobApplications(this.jobApplications);
        jobOffer.setCreatedAt(this.createdAt);
        return jobOffer;
    }

    // Convert Entity to DTO
    public static JobOfferDto fromJobOffer(JobOffer jobOffer) {
        return new JobOfferDto(
                jobOffer.getIdJobOffer(),
                jobOffer.getTitle(),
                jobOffer.getDescription(),
                jobOffer.getRequirements(),
                jobOffer.isActive(),
                jobOffer.getForum(),
                jobOffer.getJobApplications(),
                jobOffer.getCreatedBy() != null ? jobOffer.getCreatedBy().getIdUser() : null, // Get ID of the creator
                jobOffer.getCreatedAt()
        );
    }
}
