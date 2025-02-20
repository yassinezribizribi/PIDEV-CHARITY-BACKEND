package tn.esprit.examen.nomPrenomClasseExamen.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.io.Serializable;

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

    private String applicationDate;

    @Enumerated(EnumType.STRING)
    private JobApplicationStatus jobApplicationStatus;

    @ManyToOne
    private JobOffer jobOffer;


    @ManyToOne
    private Subscriber subscriber;
}
