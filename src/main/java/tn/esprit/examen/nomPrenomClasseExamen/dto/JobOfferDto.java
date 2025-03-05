package tn.esprit.examen.nomPrenomClasseExamen.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@Getter
@Setter
public class JobOfferDto implements Serializable {
    private Long idJobOffer;
    private String title;
    private String description;
    private String requirements;
    private boolean isActive;
    private Long forumId;
    private Long createdById;  // Only include the creator's ID
    private LocalDateTime createdAt;

    // Remove jobApplications and other complex fields

    public JobOffer toJobOffer() {
        JobOffer jobOffer = new JobOffer();
        jobOffer.setTitle(this.title);
        jobOffer.setDescription(this.description);
        jobOffer.setRequirements(this.requirements);
        jobOffer.setActive(this.isActive);

        // Handle forumId in the service layer
        return jobOffer;
    }

    public static JobOfferDto fromJobOffer(JobOffer jobOffer) {
        return new JobOfferDto(
                jobOffer.getIdJobOffer(),
                jobOffer.getTitle(),
                jobOffer.getDescription(),
                jobOffer.getRequirements(),
                jobOffer.isActive(),
                jobOffer.getForum() != null ? jobOffer.getForum().getIdForum() : null,
                jobOffer.getCreatedBy() != null ? jobOffer.getCreatedBy().getIdUser() : null,
                jobOffer.getCreatedAt()
        );
    }
}