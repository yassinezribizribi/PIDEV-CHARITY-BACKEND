package tn.esprit.examen.nomPrenomClasseExamen.Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.PostsRepository;
import tn.esprit.examen.nomPrenomClasseExamen.dto.PostsDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Posts;
import tn.esprit.examen.nomPrenomClasseExamen.entities.User;
import tn.esprit.examen.nomPrenomClasseExamen.services.IPostsServives;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@RestController
@RequestMapping("/api/posts")
@Controller
public class PostsController {

    @Autowired
    private IPostsServives postsServices;
    @Autowired
    private PostsRepository postsRepository;
    // Ajouter un post et l'assigner à une Subscription
    @PostMapping("/createPost")
    public ResponseEntity<Posts> createPost(
            @RequestPart(value = "file",required = false) MultipartFile file,
            @RequestPart("postData") Posts postData
    ) {
        try {
            if (file != null && !file.isEmpty()) {
                String uploadDir = "upload/";
                File dir = new File(uploadDir);
                if (!dir.exists()) dir.mkdirs();

                String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                Path filePath = Paths.get(uploadDir, fileName);
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                postData.setImagePath(fileName);
            }
            postsRepository.save(postData);

            return ResponseEntity.ok(postData);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }



    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        try {
            Path imagePath = Paths.get("upload").resolve(filename).normalize();
            Resource resource = new UrlResource(imagePath.toUri());

            if (resource.exists()) {
                String contentType = Files.probeContentType(imagePath);
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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
    @GetMapping("/{id}/like-users")
    public ResponseEntity<Set<User>> getListUser(@PathVariable Long id) {
        Set<User> likesCount = postsServices.getUserlikepost(id);
        return ResponseEntity.ok(likesCount);
    }
}
