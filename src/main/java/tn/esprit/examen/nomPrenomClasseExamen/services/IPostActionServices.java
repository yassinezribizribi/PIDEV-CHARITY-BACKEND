package tn.esprit.examen.nomPrenomClasseExamen.services;

import tn.esprit.examen.nomPrenomClasseExamen.dto.PostActionDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.PostAction;
import tn.esprit.examen.nomPrenomClasseExamen.entities.TypeAction;
import tn.esprit.examen.nomPrenomClasseExamen.entities.User;

import java.util.List;
import java.util.Optional;

public interface IPostActionServices {
    // Méthode pour créer une action et l'associer à un post

    PostAction createPostActionAndAssignToPost(PostActionDTO postActionDTO);

    List<PostAction> getAllPostActions();

    Optional<PostAction> getPostActionById(Long id);

    PostAction updatePostAction(Long id, PostActionDTO postActionDTO);

    void deletePostAction(Long id);

    // Récupérer les PostActions par Post
 //   List<PostAction> getPostActionsByPost(Long postId);
}
