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
    private String title;
    private String description;
    private Date dateTime;
    private String location;
    private TypeEvent typeEvent;
    private Date reservationDate;

    @ManyToOne
    Association association;

    @OneToMany(cascade = CascadeType.ALL, mappedBy="event")
    private Set<Paiement> paiements;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<Notification> notifications;

}
