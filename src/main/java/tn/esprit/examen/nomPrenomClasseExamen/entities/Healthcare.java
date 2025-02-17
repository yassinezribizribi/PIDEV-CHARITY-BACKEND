package tn.esprit.examen.nomPrenomClasseExamen.entities;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level= AccessLevel.PRIVATE)
@Entity
public class Healthcare implements Serializable {
    @Id
    private Long idHealthcare;
    private String history;
    private String treatmentPlan;
    private String terminalDisease;
    private Date  bookingDate;



    @OneToMany(cascade = CascadeType.ALL, mappedBy="healthcare")
    private Set<Subscriber> subscribers;


}
