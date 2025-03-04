package tn.esprit.examen.nomPrenomClasseExamen.services;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.UserRepository;
import tn.esprit.examen.nomPrenomClasseExamen.dto.PostsDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Posts;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Subscription;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.PostsRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.SubscriptionRepository;
import tn.esprit.examen.nomPrenomClasseExamen.entities.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class PostsServices implements IPostsServives{

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private UserRepository userRepository;

   @Override
    public Posts createPostsAndAssignToAssociation(PostsDTO postsDTO) {
        Posts posts = new Posts();

        posts.setContent(postsDTO.getContent());
        posts.setCreationDate(postsDTO.getCreationDate());
        System.out.println("setContent"+ postsDTO.getContent());
        //posts.setPostActions(postsDTO.getPostActions());

        // Vérification et assignation au bon abonnement (Subscription)
//        if (postsDTO.getSubscription() != null) {
//            Optional<Subscription> existingSubscription = subscriptionRepository.findById(postsDTO.getSubscription().getIdSubscription());
//            if (existingSubscription.isPresent()) {
//                posts.setSubscription(existingSubscription.get());
//            } else {
//                throw new IllegalArgumentException("Subscription ID " + postsDTO.getSubscription().getIdSubscription() + " introuvable.");
//            }
//        }
//        } else {
//            throw new IllegalArgumentException("Une subscription est requise pour créer un post.");
//        }

        return postsRepository.save(posts);
    }

    @Override
    public Posts updatePosts(Long id, Posts updatedPosts) {
        return postsRepository.findById(id)
                .map(existingPosts -> {
                    existingPosts.setContent(updatedPosts.getContent());
                    existingPosts.setCreationDate(updatedPosts.getCreationDate());
                    //existingPosts.setPostActions(updatedPosts.getPostActions());
                    //existingPosts.setSubscription(updatedPosts.getSubscription());
                    return postsRepository.save(existingPosts);
                }).orElseThrow(() -> new RuntimeException("Post non trouvé avec l'ID : " + id));
    }

    @Transactional
    public Posts toggleLike(Long postId, Long userId) {
        Posts post = postsRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Fetch the latest likedByUsers list to ensure consistency
        Set<User> likedUsers = post.getLikedByUsers();

        if (likedUsers.stream().anyMatch(u -> u.getIdUser().equals(userId))) {
            // Unlike the post
            likedUsers.removeIf(u -> u.getIdUser().equals(userId));
        } else {
            // Like the post
            likedUsers.add(user);
        }

        post.setLikesCount((long) likedUsers.size()); // Ensure count is accurate

        return postsRepository.save(post);
    }

    @Override
    public void deletePosts(Long id) {
        if (!postsRepository.existsById(id)) {
            throw new RuntimeException("Post non trouvé avec l'ID : " + id);
        }
        postsRepository.deleteById(id);
    }

    @Override
    public Optional<Posts> getPostsById(Long id) {
        return postsRepository.findById(id);
    }

    @Override
    public List<Posts> getAllPosts() {
        return postsRepository.findAll();
    }

//    public List<Posts> getAllPosts(Long userId) {
//        List<Posts> posts = postsRepository.findAll();
//        for (Posts post : posts) {
//            boolean likedByUser = postsRepository.hasUserLikedPost(post.getIdPosts(), userId);
//            post.setLikedByUser(likedByUser); // Set likedByUser correctly
//        }
//        return posts;
//    }



    @Override
    public List<Posts> getPostsBySubscription(Long subscriptionId) {
        Optional<Subscription> subscription = subscriptionRepository.findById(subscriptionId);
        if (subscription.isEmpty()) {
            throw new IllegalArgumentException("Subscription ID " + subscriptionId + " introuvable.");
        }
        return postsRepository.findBySubscription(subscription.get());
    }

    @Override
    public Long getLikesCountByPost(Long postId) {
        return postsRepository.findById(postId)
                .map(post -> (long) post.getLikedByUsers().size())
                .orElseThrow(() -> new RuntimeException("Post not found"));
    }

}
