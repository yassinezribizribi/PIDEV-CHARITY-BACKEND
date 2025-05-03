package tn.esprit.examen.nomPrenomClasseExamen.services;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.CommentRepo;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.PostsRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.UserRepository;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Comment;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Posts;
import tn.esprit.examen.nomPrenomClasseExamen.entities.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Service
@Slf4j
@AllArgsConstructor
public class Service implements IService{
@Autowired
PostsRepository postRepo;
    @Autowired
    CommentRepo commentRepo;
    @Autowired
    UserRepository userRepo;


    @Override
    public Comment addCommentandAffectToPost(Long id, Long iduser, Comment comment) {
        User user = userRepo.findById(iduser).get();
        Posts post = postRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with ID: " + id));

        if (containsBadWords(comment.getDescriptionComment())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Votre commentaire contient des mots inappropriés.");
        }

        comment.setPost(post);
        comment.setUser(user);
        return commentRepo.save(comment);
    }



    @Override
    public List<Comment> findAll() {
        return commentRepo.findAll();
    }

    @Override
    public void delete(Integer id) {
        commentRepo.deleteById(id);
    }

    @Override
    public Comment updateComment(Integer id, Comment commentDetails) {
        Comment comment = commentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found with ID: " + id));
        comment.setDescriptionComment(commentDetails.getDescriptionComment());
        comment.setRating(commentDetails.getRating());
        return commentRepo.save(comment);
    }




    private final RestTemplate restTemplate = new RestTemplate();
    private static final String FILTER_API_URL = "https://www.purgomalum.com/service/json?text=";
    private static final String ADD_BAD_WORD = "&add=voiture";

    private String filterBadWordsUsingAPI(String content) {
        if (content == null || content.isEmpty()) return "";

        ResponseEntity<Map> response = restTemplate.exchange(
                FILTER_API_URL + content + ADD_BAD_WORD,
                HttpMethod.GET,
                new HttpEntity<>(new HttpHeaders()),
                Map.class
        );

        return response.getBody().get("result").toString();
    }

    private boolean containsBadWords(String content) {
        if (content == null || content.isEmpty()) return false;

        ResponseEntity<Map> response = restTemplate.exchange(
                FILTER_API_URL + content + ADD_BAD_WORD,
                HttpMethod.GET,
                new HttpEntity<>(new HttpHeaders()),
                Map.class
        );

        String filtered = response.getBody().get("result").toString();
        return !filtered.equals(content); // S'il y a une différence => bad word détecté
    }





}
