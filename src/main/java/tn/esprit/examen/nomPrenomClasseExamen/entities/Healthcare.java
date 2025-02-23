package tn.esprit.examen.nomPrenomClasseExamen.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level= AccessLevel.PRIVATE)
@Entity
public class Healthcare implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idHealthcare;

    private String history;
    private String treatmentPlan;
    private String terminalDisease;

    @Temporal(TemporalType.TIMESTAMP)
    private Date bookingDate;

    @ManyToOne
    @JoinColumn(name = "subscriber_id", nullable = true)
    private Subscriber subscriber;
}
