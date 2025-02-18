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
public class Subscription implements Serializable {
    @Id
    private Long idSubscription;
    private Date subscriptionDate;

    @OneToMany(cascade = CascadeType.ALL, mappedBy="subscription")
    private Set<Posts> Postss;

    @ManyToMany(mappedBy="subscriptions", cascade = CascadeType.ALL)
    private Set<Association> associations;
}
