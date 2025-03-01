package tn.esprit.examen.nomPrenomClasseExamen.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Subscription;
import tn.esprit.examen.nomPrenomClasseExamen.entities.PostAction;

import java.util.Date;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostsDTO {

    private Long idPosts;
    private String content;
    private Date creationDate;
    private Set<PostAction> postActions;
    private Subscription subscription;
}
