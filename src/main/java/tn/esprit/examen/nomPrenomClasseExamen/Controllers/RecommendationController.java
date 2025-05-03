package tn.esprit.examen.nomPrenomClasseExamen.Controllers;

import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.examen.nomPrenomClasseExamen.services.RecommendationService;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    @PermitAll
    @GetMapping("/posts/{userId}")
    public Object getRecommendedPosts(@PathVariable Long userId) {
        System.out.println("Requête reçue pour userId : " + userId);
        return recommendationService.getRecommendedPosts(userId);
    }

    @PermitAll
    @GetMapping("/events/{userId}")
    public Object getRecommendedEvents(@PathVariable Long userId) {
        System.out.println("Requête reçue pour userId : " + userId);
        return recommendationService.getRecommendedEvents(userId);
    }
}