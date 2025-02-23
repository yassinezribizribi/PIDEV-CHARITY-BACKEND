package tn.esprit.examen.nomPrenomClasseExamen.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Posts;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Association;

import java.util.Date;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubscriptionDTO {

    private Long idSubscription;
    private Date subscriptionDate;
    private Set<Posts> posts;
    private Set<Association> associations;
}
