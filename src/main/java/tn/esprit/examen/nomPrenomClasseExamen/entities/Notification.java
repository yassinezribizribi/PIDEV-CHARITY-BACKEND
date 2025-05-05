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

    @Temporal(TemporalType.TIMESTAMP)
    Date createdAt = new Date();

    @ManyToOne
    @JoinColumn(name = "association_id", nullable = true)
    Association association;
    @Enumerated(EnumType.STRING)
    private NotificationType type;
    @ManyToOne
    private Healthcare healthcare;
    @ManyToOne
    @JoinColumn(name = "subscriber_id", nullable = false)
    Subscriber subscriber;
    public static final List<LocalDate> FRENCH_HOLIDAYS = List.of(
            LocalDate.of(2025, 1, 1),   // Jour de l'An
            LocalDate.of(2025, 4, 21),  // Lundi de Pâques
            LocalDate.of(2025, 5, 1),   // Fête du Travail
            LocalDate.of(2025, 5, 8),   // Victoire 1945
            LocalDate.of(2025, 5, 29),  // Ascension
            LocalDate.of(2025, 6, 9),   // Pentecôte
            LocalDate.of(2025, 7, 14),  // Fête nationale
            LocalDate.of(2025, 8, 15),  // Assomption
            LocalDate.of(2025, 11, 1),  // Toussaint
            LocalDate.of(2025, 11, 11), // Armistice
            LocalDate.of(2025, 12, 25)  // Noël
    );

    public static boolean isHoliday(LocalDate date) {
        return FRENCH_HOLIDAYS.contains(date);
    }
}
