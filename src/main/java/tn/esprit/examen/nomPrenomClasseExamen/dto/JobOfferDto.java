package tn.esprit.examen.nomPrenomClasseExamen.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Forum;
import tn.esprit.examen.nomPrenomClasseExamen.entities.JobApplication;
import tn.esprit.examen.nomPrenomClasseExamen.entities.JobOffer;

import java.io.Serializable;
import java.util.Set;

/**
 * DTO for {@link tn.esprit.examen.nomPrenomClasseExamen.entities.JobOffer}
 */
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
                jobOffer.getJobApplications()
        );
    }
}
