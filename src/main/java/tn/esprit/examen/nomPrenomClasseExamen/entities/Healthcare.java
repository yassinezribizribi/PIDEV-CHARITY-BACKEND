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
    private String History;
    private String TreatmentPlan;
    private String TerminalDisease;
    private Date  BookingDate;



    @OneToMany(cascade = CascadeType.ALL, mappedBy="healthcare")
    private Set<Subscriber> Subscribers;


}
