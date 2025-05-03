package tn.esprit.examen.nomPrenomClasseExamen.services;

import org.springframework.web.multipart.MultipartFile;
import tn.esprit.examen.nomPrenomClasseExamen.dto.PostsDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Posts;
import tn.esprit.examen.nomPrenomClasseExamen.entities.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface IPostsServives {
    public Posts createPostsAndAssignToAssociation(PostsDTO postsDTO, MultipartFile imageFile);
    Posts updatePosts(Long id, Posts updatedPosts);
    public Set<User> getUserlikepost(Long postId);
    void deletePosts(Long id);

    Optional<Posts> getPostsById(Long id);

    List<Posts> getAllPosts();

    List<Posts> getPostsBySubscription(Long subscriptionId);

    Posts toggleLike(Long postId, Long userId);

    Long getLikesCountByPost(Long id);
}
