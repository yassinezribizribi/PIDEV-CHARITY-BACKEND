package tn.esprit.examen.nomPrenomClasseExamen.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.Date;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String message;
    String actionUrl;
    @Column(name = "`read`") // Escape with backticks for MariaDB
    private boolean read = false;
    @Enumerated(EnumType.STRING)
    NotificationType type;

    @Temporal(TemporalType.TIMESTAMP)
    Date createdAt = new Date();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "association_id")
    Association association;

    @ManyToOne
    @JoinColumn(name = "healthcare_id")
    Healthcare healthcare;

    @ManyToOne
    @JoinColumn(name = "subscriber_id", nullable = false)
    Subscriber subscriber;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    Subscriber sender;

    public enum NotificationType {
        HEALTHCARE_UPDATE,
        VIDEO_CALL,
        APPOINTMENT_REMINDER,
        TIER_CHANGE,
        NEW_PARTNERSHIP,
        PARTNERSHIP_REQUEST,
        PARTNERSHIP_REMOVED,
        IMPACT_REPORT,
        SYSTEM_ALERT,
        NEW_MESSAGE
    }
}