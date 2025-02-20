package tn.esprit.examen.nomPrenomClasseExamen.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.Set;

@Getter
@Setter
@ToString(exclude = "jobApplications")
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class JobOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idJobOffer;
    private String title;
    private String description;
    private String requirements;
    private boolean isActive;

    @ManyToOne
    private Forum forum;



    @JsonIgnoreProperties({"jobApplications"})
    @OneToMany(mappedBy = "jobOffer", cascade = CascadeType.ALL)
    private Set<JobApplication> jobApplications;
}
