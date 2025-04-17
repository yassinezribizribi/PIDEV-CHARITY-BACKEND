package tn.esprit.examen.nomPrenomClasseExamen.entities;

import java.util.List;

public class TestimonialAnalysisResult {
    public String emotion;
    public int distressScore;
    public String suggestedResponse;
    public List<String> recommendedResources;

    public TestimonialAnalysisResult(String emotion, int distressScore, String suggestedResponse, List<String> resources) {
        this.emotion = emotion;
        this.distressScore = distressScore;
        this.suggestedResponse = suggestedResponse;
        this.recommendedResources = resources;
    }
}
