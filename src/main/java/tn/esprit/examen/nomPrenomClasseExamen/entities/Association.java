package tn.esprit.examen.nomPrenomClasseExamen.entities;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.Set;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level= AccessLevel.PRIVATE)
@Entity
public class Association implements Serializable {
    @Id
    private Long idAssociation;
    private String AssociationName;
    private String Description;
    private Boolean Validated;

    @ManyToMany(cascade = CascadeType.ALL)
    private Set<Subscription> subscriptions;

    @ManyToMany(cascade = CascadeType.ALL)
    private Set<Mission> missions;

    @OneToMany(cascade = CascadeType.ALL, mappedBy="association")
    private Set<Event> Events;

    @OneToMany(cascade = CascadeType.ALL, mappedBy="association")
    private Set<Notification> Notifications;







}
