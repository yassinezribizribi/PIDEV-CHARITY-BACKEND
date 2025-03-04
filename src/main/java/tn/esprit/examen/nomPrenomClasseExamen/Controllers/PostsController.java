package tn.esprit.examen.nomPrenomClasseExamen.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.nomPrenomClasseExamen.dto.PostsDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Posts;
import tn.esprit.examen.nomPrenomClasseExamen.services.IPostsServives;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/posts")
public class PostsController {

    @Autowired
    private IPostsServives postsServices;

    // Ajouter un post et l'assigner à une Subscription
    @PostMapping("/createPost")
    public ResponseEntity<Posts> createPost(@RequestBody PostsDTO postsDTO) {
        Posts createdPost = postsServices.createPostsAndAssignToAssociation(postsDTO);
        return ResponseEntity.ok(createdPost);
    }

    // Mettre à jour un post
    @PutMapping("/updatePost/{id}")
    public ResponseEntity<Posts> updatePost(@PathVariable Long id, @RequestBody Posts updatedPost) {
        Posts post = postsServices.updatePosts(id, updatedPost);
        return ResponseEntity.ok(post);
    }

    @PutMapping("/likePost/{postId}/user/{userId}")
    public ResponseEntity<Posts> toggleLike(@PathVariable Long postId, @PathVariable Long userId) {
        Posts updatedPost = postsServices.toggleLike(postId, userId);
        return ResponseEntity.ok(updatedPost);
    }

    // Supprimer un post
    @DeleteMapping("/deletePost/{id}")
    public ResponseEntity<String> deletePost(@PathVariable Long id) {
        postsServices.deletePosts(id);
        return ResponseEntity.ok("Post supprimé avec succès !");
    }

    // Récupérer un post par ID
    @GetMapping("/getPostById/{id}")
    public ResponseEntity<Posts> getPostById(@PathVariable Long id) {
        Optional<Posts> post = postsServices.getPostsById(id);
        return post.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Récupérer tous les posts
    @GetMapping("/getAllPosts")
    public ResponseEntity<List<Posts>> getAllPosts() {
        List<Posts> postsList = postsServices.getAllPosts();
        return ResponseEntity.ok(postsList);
    }

//    @GetMapping("/all/{userId}")
//    public ResponseEntity<List<Posts>> getAllPosts(@PathVariable Long userId) {
//        List<Posts> posts = postsServices.getAllPosts(userId);
//        return ResponseEntity.ok(posts);
//    }



    // Récupérer les posts par Subscription
    @GetMapping("/getPostsBySubscription/{subscriptionId}")
    public ResponseEntity<List<Posts>> getPostsBySubscription(@PathVariable Long subscriptionId) {
        List<Posts> postsList = postsServices.getPostsBySubscription(subscriptionId);
        return ResponseEntity.ok(postsList);
    }

    @GetMapping("/{id}/likes-count")
    public ResponseEntity<Long> getLikesCountByPost(@PathVariable Long id) {
        Long likesCount = postsServices.getLikesCountByPost(id);
        return ResponseEntity.ok(likesCount);
    }

}
