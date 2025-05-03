package tn.esprit.examen.nomPrenomClasseExamen.services;

import org.springframework.stereotype.Service;

import java.util.Map;



import org.springframework.stereotype.Service;
import tn.esprit.examen.nomPrenomClasseExamen.entities.TestimonialAnalysisResult;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class TestimonialAIAnalyzerServices {

    private final Map<String, String> emotionKeywords = Map.of(
            "triste", "tristesse",
            "mal", "détresse",
            "seul", "solitude",
            "peur", "peur",
            "déprimé", "dépression",
            "angoisse", "angoisse",
            "calme", "sérénité",
            "heureux", "joie"
    );

    public TestimonialAnalysisResult analyze(String content) {
        String normalized = content.toLowerCase();
        String detectedEmotion = "neutre";
        int distressScore = 0;

        for (Map.Entry<String, String> entry : emotionKeywords.entrySet()) {
            if (normalized.contains(entry.getKey())) {
                detectedEmotion = entry.getValue();
                distressScore += 20;
            }
        }

        String response = switch (detectedEmotion) {
            case "tristesse", "détresse" -> "Vous traversez une période difficile. N'hésitez pas à demander de l’aide.";
            case "solitude" -> "Vous n'êtes pas seul·e. Il existe des groupes de soutien.";
            case "peur" -> "Il est normal de ressentir de la peur. Parlez-en à un professionnel.";
            case "joie" -> "Merci pour ce message positif ! Continuez à inspirer les autres.";
            default -> "Merci pour votre témoignage.";
        };

        List<String> resources = switch (detectedEmotion) {
            case "tristesse", "détresse" -> List.of("https://soutien-psychologique.fr", "https://numéro-vert.fr");
            case "solitude" -> List.of("https://solidarité-écoute.org");
            case "peur" -> List.of("https://psycom.org");
            default -> Collections.emptyList();
        };

        return new TestimonialAnalysisResult(detectedEmotion, distressScore, response, resources);
    }
}
