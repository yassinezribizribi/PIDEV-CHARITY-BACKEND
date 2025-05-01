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
    private Long idHealthcare;

    private String history;
    private String treatmentPlan;

    private String terminalDisease;
    @Column(name = "meeting_url")
    private String meetingUrl;


    @Temporal(TemporalType.TIMESTAMP)
    private Date bookingDate;

    // ✅ Ajout du statut pour suivre l’évolution du traitement
    @Enumerated(EnumType.STRING)
    private HealthcareStatus status;

    @JsonIgnore    // ✅ Patient qui a demandé le soin
    @ManyToOne
    @JoinColumn(name = "subscriber_id", nullable = false)
    private Subscriber subscriber;

    // ✅ Médecin (VOLUNTEER) qui traite la demande
    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = true)
    private Subscriber doctor;

    // ✅ Setter pour éviter l’erreur de compilation
    public void setStatus(HealthcareStatus status) {
        this.status = status;
    }
}
