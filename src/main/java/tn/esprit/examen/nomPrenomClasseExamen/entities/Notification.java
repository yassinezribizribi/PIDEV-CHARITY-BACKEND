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

    @Temporal(TemporalType.TIMESTAMP)
    Date createdAt = new Date();

    @ManyToOne
    @JoinColumn(name = "association_id", nullable = true)
    Association association;

    @ManyToOne
    @JoinColumn(name = "subscriber_id", nullable = false)
    Subscriber subscriber;
}
