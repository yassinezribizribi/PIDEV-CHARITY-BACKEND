package tn.esprit.examen.nomPrenomClasseExamen.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder // ➡️ ADD THIS

public class Participation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long idParticipation;

    @ManyToOne
    private Subscriber subscriber;

    @ManyToOne
    private Mission mission;

    @ManyToOne
    private MissionRole missionRole; // ✅ NEW: Participation to a specific role

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime participationDate;

    private Boolean confirmed;
    private  String RejectionReason;
    private Boolean attended;

    private Date confirmationDate;

    @Column(length = 500)
    private String feedback;

    @Enumerated(EnumType.STRING)
    private ParticipationStatus participationStatus = ParticipationStatus.PENDING;
}



