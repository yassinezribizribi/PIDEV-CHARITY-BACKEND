package tn.esprit.examen.nomPrenomClasseExamen.services;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.examen.nomPrenomClasseExamen.dto.PostsDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Posts;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Subscription;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.PostsRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.SubscriptionRepository;

import java.util.List;
import java.util.Optional;

@Service
public class PostsServices implements IPostsServives{

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Override
    public Posts createPostsAndAssignToAssociation(PostsDTO postsDTO) {
        Posts posts = new Posts();

        // Vérification que l'ID du post n'est pas défini si @GeneratedValue est utilisé
        if (postsDTO.getIdPosts() != null) {
            throw new IllegalArgumentException("L'ID du post ne doit pas être fourni.");
        }

        posts.setContent(postsDTO.getContent());
        posts.setCreationDate(postsDTO.getCreationDate());
        posts.setLikesCount(postsDTO.getLikesCount());
        posts.setShareCount(postsDTO.getShareCount());
        posts.setPostActions(postsDTO.getPostActions());

        // Vérification et assignation au bon abonnement (Subscription)
        if (postsDTO.getSubscription() != null) {
            Optional<Subscription> existingSubscription = subscriptionRepository.findById(postsDTO.getSubscription().getIdSubscription());
            if (existingSubscription.isPresent()) {
                posts.setSubscription(existingSubscription.get());
            } else {
                throw new IllegalArgumentException("Subscription ID " + postsDTO.getSubscription().getIdSubscription() + " introuvable.");
            }
        } else {
            throw new IllegalArgumentException("Une subscription est requise pour créer un post.");
        }

        return postsRepository.save(posts);
    }

    @Override
    public Posts updatePosts(Long id, Posts updatedPosts) {
        return postsRepository.findById(id)
                .map(existingPosts -> {
                    existingPosts.setContent(updatedPosts.getContent());
                    existingPosts.setCreationDate(updatedPosts.getCreationDate());
                    existingPosts.setLikesCount(updatedPosts.getLikesCount());
                    existingPosts.setShareCount(updatedPosts.getShareCount());
                    existingPosts.setPostActions(updatedPosts.getPostActions());
                    existingPosts.setSubscription(updatedPosts.getSubscription());
                    return postsRepository.save(existingPosts);
                }).orElseThrow(() -> new RuntimeException("Post non trouvé avec l'ID : " + id));
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

    @Override
    public List<Posts> getPostsBySubscription(Long subscriptionId) {
        Optional<Subscription> subscription = subscriptionRepository.findById(subscriptionId);
        if (subscription.isEmpty()) {
            throw new IllegalArgumentException("Subscription ID " + subscriptionId + " introuvable.");
        }
        return postsRepository.findBySubscription(subscription.get());
    }

}
