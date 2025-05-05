package tn.esprit.examen.nomPrenomClasseExamen.Utlis;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValidationUtils {
    public static ResponseEntity<?> handleValidationErrors(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<Map<String, String>> errorList = bindingResult.getFieldErrors()
                    .stream()
                    .map(err -> {
                        Map<String, String> fieldError = new HashMap<>();
                        fieldError.put("field", err.getField());
                        fieldError.put("message", err.getDefaultMessage());
                        return fieldError;
                    })
                    .toList();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("timestamp", Instant.now().toString());
            errorResponse.put("errors", errorList);

            return ResponseEntity.badRequest().body(errorResponse);
        }
        return null;
    }
}
