package tn.esprit.examen.nomPrenomClasseExamen.services;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TranslationService {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String TRANSLATE_URL = "http://localhost:5000/translate";

    public Map<String, String> translate(String text, String sourceLang, List<String> targetLangs) {
        Map<String, String> results = new HashMap<>();

        for (String targetLang : targetLangs) {
            try {
                Map<String, Object> body = Map.of(
                        "q", text,
                        "source", "auto", // ✅ remplace sourceLang par "auto"
                        "target", targetLang,
                        "format", "text"
                );

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

                ResponseEntity<Map> response = restTemplate.postForEntity(TRANSLATE_URL, request, Map.class);

                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    Object translatedText = response.getBody().get("translatedText");
                    results.put(targetLang, translatedText != null ? translatedText.toString() : "❌ Texte intraduisible");
                } else {
                    results.put(targetLang, "❌ Erreur: Réponse invalide");
                }

            } catch (Exception e) {
                results.put(targetLang, "❌ Erreur: " + e.getMessage());
            }
        }

        return results;
    }
}
