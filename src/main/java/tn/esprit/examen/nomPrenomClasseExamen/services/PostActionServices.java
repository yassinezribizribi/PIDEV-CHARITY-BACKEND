package tn.esprit.examen.nomPrenomClasseExamen.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.examen.nomPrenomClasseExamen.dto.PostActionDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.PostAction;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Posts;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Testimonial;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.PostActionRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.PostsRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.TestimonialRepository;

import java.util.*;

@Service
public class PostActionServices implements IPostActionServices {

    @Autowired
    private PostActionRepository postActionRepository;

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private TestimonialRepository testimonialRepository;

    @Override
    public PostAction createPostActionAndAssignToPost(PostActionDTO postActionDTO) {
        PostAction postAction = new PostAction();

        postAction.setTypeAction(postActionDTO.getTypeAction());
        postAction.setDateAction(new Date()); // Assigner la date actuelle

        // Assigner Post
        if (postActionDTO.getPosts() != null) {
            Optional<Posts> postsOptional = postsRepository.findById(postActionDTO.getPosts().getIdPosts());
            if (postsOptional.isPresent()) {
                postAction.setPosts(postsOptional.get());
            } else {
                throw new IllegalArgumentException("Post ID introuvable.");
            }
        } else {
            throw new IllegalArgumentException("Un Post est requis.");
        }

        // Enregistrement de PostAction
        return postActionRepository.save(postAction);
}


    @Override
    public List<PostAction> getAllPostActions() {
        return postActionRepository.findAll();
    }

    @Override
    public Optional<PostAction> getPostActionById(Long id) {
        return postActionRepository.findById(id);
    }

    @Override
    public PostAction updatePostAction(Long id, PostActionDTO postActionDTO) {
        return postActionRepository.findById(id)
                .map(existingPostAction -> {
                    existingPostAction.setTypeAction(postActionDTO.getTypeAction());
                    existingPostAction.setDateAction(new Date());

                    if (postActionDTO.getTestimonial() != null) {
                        Optional<Testimonial> testimonialOptional = testimonialRepository.findById(postActionDTO.getTestimonial().getIdTestimonial());
                        if (testimonialOptional.isPresent()) {
                            existingPostAction.setTestimonial(testimonialOptional.get());
                        } else {
                            throw new IllegalArgumentException("Testimonial ID introuvable.");
                        }
                    }

                    if (postActionDTO.getPosts() != null) {
                        Optional<Posts> postsOptional = postsRepository.findById(postActionDTO.getPosts().getIdPosts());
                        if (postsOptional.isPresent()) {
                            existingPostAction.setPosts(postsOptional.get());
                        } else {
                            throw new IllegalArgumentException("Post ID introuvable.");
                        }
                    }

                    return postActionRepository.save(existingPostAction);
                }).orElseThrow(() -> new RuntimeException("PostAction non trouvé avec l'ID : " + id));
    }

    @Override
    public void deletePostAction(Long id) {
        if (!postActionRepository.existsById(id)) {
            throw new RuntimeException("PostAction non trouvé avec l'ID : " + id);
        }
        postActionRepository.deleteById(id);
    }

    // Récupérer les PostActions par Post
/*    @Override
    public List<PostAction> getPostActionsByPost(Long postId) {
        Optional<Posts> postOptional = postsRepository.findById(postId);
        if (postOptional.isEmpty()) {
            throw new IllegalArgumentException("Post ID introuvable.");
        }
        Posts post = postOptional.get();
        return postActionRepository.findByPosts(post);
        }


 */


    // Méthode simplifiée avec boucle for classique
    @Override
    public Map<Long, Long> getLikesCountByPost() {
        List<Object[]> results = postActionRepository.countLikesByPost();
        Map<Long, Long> likesCountMap = new HashMap<>();

        for (Object[] result : results) {
            Long idPosts = (Long) result[0];
            Long likesCount = (Long) result[1];
            likesCountMap.put(idPosts, likesCount);
        }

        return likesCountMap;
    }

}
