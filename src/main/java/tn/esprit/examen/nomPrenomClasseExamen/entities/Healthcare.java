package tn.esprit.examen.nomPrenomClasseExamen.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Healthcare implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long idHealthcare;

    String history;
    String treatmentPlan;
    String terminalDisease;

    @Column(name = "meeting_url")
    String meetingUrl;

    @Temporal(TemporalType.TIMESTAMP)
    Date bookingDate;

    @Enumerated(EnumType.STRING)
    HealthcareStatus status;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "subscriber_id", nullable = false)
    Subscriber subscriber;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = true)
    Subscriber doctor;
}
