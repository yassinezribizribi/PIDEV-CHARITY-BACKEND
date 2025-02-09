package tn.esprit.examen.nomPrenomClasseExamen.entities;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level= AccessLevel.PRIVATE)
@Entity
public class Event implements Serializable {
    @Id
    private Long idEvent;
    private String Title;
    private String Description;
    private Date DateTime;
    private String Location;
    private TypeEvent typeEvent;
    private Date ReservationDate;

    @ManyToOne
    Association association;

    @OneToMany(cascade = CascadeType.ALL, mappedBy="event")
    private Set<Paiement> Paielents;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<Notification> Notifications;

}
