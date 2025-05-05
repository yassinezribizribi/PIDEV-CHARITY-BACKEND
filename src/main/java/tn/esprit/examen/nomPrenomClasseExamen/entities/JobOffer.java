package tn.esprit.examen.nomPrenomClasseExamen.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Set;
import java.time.LocalDateTime;

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
    @Column(name = "is_active", columnDefinition = "BIT(1) DEFAULT 1")
    private boolean isActive;

    // ADD PROPER BOOLEAN GETTER
    public boolean isActive() {  // 👈 Crucial for Hibernate mapping
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
    @ManyToOne
    @JsonIgnoreProperties
    private Forum forum;

    @OneToMany(mappedBy = "jobOffer", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"jobApplications"})
    private Set<JobApplication> jobApplications;

    @ManyToOne
    @JsonIgnoreProperties({"jobOffers"}) // Avoid recursion if necessary
    private Subscriber createdBy;  // New field for the creator of the JobOffer

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now(); // Time when the job offer is created
}
