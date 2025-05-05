package tn.esprit.examen.nomPrenomClasseExamen.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

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

    @Column(name = "`read`") // Échappement pour MariaDB ou MySQL
    boolean read = false;

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

    // ✅ Enum complète
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

    // ✅ Jours fériés français 2025
    public static final List<LocalDate> FRENCH_HOLIDAYS = List.of(
            LocalDate.of(2026, 1, 1),   // Jour de l'An
            LocalDate.of(2026, 4, 21),  // Lundi de Pâques
            LocalDate.of(2026, 5, 1),   // Fête du Travail
            LocalDate.of(2026, 5, 8),   // Victoire 1945
            LocalDate.of(2026, 5, 29),  // Ascension
            LocalDate.of(2025, 6, 9),   // Lundi de Pentecôte
            LocalDate.of(2025, 7, 14),  // Fête nationale
            LocalDate.of(2025, 8, 15),  // Assomption
            LocalDate.of(2025, 11, 1),  // Toussaint
            LocalDate.of(2025, 11, 11), // Armistice 1918
            LocalDate.of(2025, 12, 25)  // Noël
    );

    public static boolean isHoliday(LocalDate date) {
        return FRENCH_HOLIDAYS.contains(date);
    }
}
