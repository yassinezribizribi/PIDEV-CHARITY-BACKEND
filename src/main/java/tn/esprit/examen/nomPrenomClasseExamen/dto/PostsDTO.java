package tn.esprit.examen.nomPrenomClasseExamen.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Posts;
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
    private boolean likedByUser;

    public PostsDTO(Posts post, Long userId) {
        this.idPosts = post.getIdPosts();
        this.content = post.getContent();
        this.creationDate = post.getCreationDate();
        this.likedByUser = post.getLikedByUsers().stream().anyMatch(user -> user.getIdUser().equals(userId));
    }
    //private Set<PostAction> postActions;
   // private Subscription subscription;
}
