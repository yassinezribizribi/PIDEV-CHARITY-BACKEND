package tn.esprit.examen.nomPrenomClasseExamen.entities;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level= AccessLevel.PRIVATE)
@Entity
public class JobApplication implements Serializable {
    @Id
    private Long idJob;
    private Date applicationDate;
    private JobApplicationStatus JobApplication;

    @ManyToMany(mappedBy="jobapplications", cascade = CascadeType.ALL)
    private Set<Forum> forums;

    @OneToOne
    private JobOffer joboffer;

}
