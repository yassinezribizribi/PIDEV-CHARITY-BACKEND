package tn.esprit.examen.nomPrenomClasseExamen.Controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.nomPrenomClasseExamen.dto.TranslationRequest;
import tn.esprit.examen.nomPrenomClasseExamen.services.TranslationService;

import java.util.Map;

@RestController
@RequestMapping("/api/translate")
@AllArgsConstructor
public class TranslationController {

    private final TranslationService translationService;

    @PostMapping
    public ResponseEntity<Map<String, String>> translate(@RequestBody TranslationRequest request) {
        try {
            Map<String, String> translations = translationService.translate(
                    request.getText(),
                    request.getSourceLang(),
                    request.getTargetLangs()
            );
            return ResponseEntity.ok(translations);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
