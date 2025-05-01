package tn.esprit.examen.nomPrenomClasseExamen.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class JobApplication implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idApplication;

    private LocalDateTime applicationDate;

    @Enumerated(EnumType.STRING)
    private JobApplicationStatus jobApplicationStatus;

    @ManyToOne
    @JsonBackReference
    private JobOffer jobOffer;


    @ManyToOne
    private Subscriber applicant;
}
