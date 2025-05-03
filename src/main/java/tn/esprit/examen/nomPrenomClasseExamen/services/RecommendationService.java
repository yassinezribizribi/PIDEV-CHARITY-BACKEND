package tn.esprit.examen.nomPrenomClasseExamen.services;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Service
public class RecommendationService {

    private final String recommendationApiUrl = "http://localhost:8001";
    private final RestTemplate restTemplate;

    public RecommendationService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // 5 seconds
        factory.setReadTimeout(5000); // 5 seconds
        this.restTemplate = new RestTemplate(factory);
    }

    public Object getRecommendedPosts(Long userId) {
        String url = recommendationApiUrl + "/feed/" + userId; // Changed from /recommend/posts/ to /feed/
        try {
            ResponseEntity<Object> response = restTemplate.getForEntity(url, Object.class);
            return response.getBody();
        } catch (RestClientException e) {
            System.err.println("Error fetching recommended posts: " + e.getMessage());
            return null;
        }
    }

    public Object getRecommendedEvents(Long userId) {
        String url = recommendationApiUrl + "/recommend/events/" + userId;
        try {
            ResponseEntity<Object> response = restTemplate.getForEntity(url, Object.class);
            return response.getBody();
        } catch (RestClientException e) {
            System.err.println("Error fetching recommended events: " + e.getMessage());
            return null;
        }
    }
}