package tn.esprit.examen.nomPrenomClasseExamen.services;

import tn.esprit.examen.nomPrenomClasseExamen.dto.PostsDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Posts;

import java.util.List;
import java.util.Optional;

public interface IPostsServives {
    Posts createPostsAndAssignToAssociation(PostsDTO postsDTO);

    Posts updatePosts(Long id, Posts updatedPosts);

    void deletePosts(Long id);

    Optional<Posts> getPostsById(Long id);

    List<Posts> getAllPosts();

    List<Posts> getPostsBySubscription(Long subscriptionId);
}
