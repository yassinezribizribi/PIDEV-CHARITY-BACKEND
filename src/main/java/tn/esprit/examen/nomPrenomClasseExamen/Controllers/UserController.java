package tn.esprit.examen.nomPrenomClasseExamen.Controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.nomPrenomClasseExamen.dto.MessageResponseDto;
import tn.esprit.examen.nomPrenomClasseExamen.dto.UserDataDTO;
import tn.esprit.examen.nomPrenomClasseExamen.services.UserService;

@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 🔹 Demander une réinitialisation de mot de passe (Forget Password)
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody UserDataDTO userDataDTO) {
        log.info("📩 Tentative de réinitialisation de mot de passe pour : {}", userDataDTO.getEmail());

        if (userDataDTO.getEmail() == null || userDataDTO.getEmail().trim().isEmpty()) {
            log.warn("❌ Email non fourni !");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponseDto("Error: Email is required!"));
        }

        try {
            String response = userService.forgotPassword(userDataDTO.getEmail());
            log.info("✅ Email de réinitialisation envoyé avec succès !");
            return ResponseEntity.ok(new MessageResponseDto(response));
        } catch (Exception e) {
            log.error("❌ Erreur lors de la demande de réinitialisation : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponseDto("Error: " + e.getMessage()));
        }
    }

    // 🔹 Réinitialiser le mot de passe avec le token
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody UserDataDTO userDataDTO) {
        log.info("🔑 Tentative de réinitialisation avec le token : {}", userDataDTO.getResetToken());

        if (userDataDTO.getResetToken() == null || userDataDTO.getResetToken().trim().isEmpty()) {
            log.warn("❌ Token non fourni !");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponseDto("Error: Reset token is required!"));
        }

        if (userDataDTO.getPassword() == null || userDataDTO.getPassword().length() < 6) {
            log.warn("❌ Mot de passe invalide !");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponseDto("Error: Password must be at least 6 characters!"));
        }

        try {
            String response = userService.resetPassword(userDataDTO.getResetToken(), userDataDTO.getPassword());

            if (response.contains("invalid")) {
                log.warn("❌ Token invalide ou expiré !");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponseDto(response));
            }

            log.info("✅ Mot de passe réinitialisé avec succès !");
            return ResponseEntity.ok(new MessageResponseDto(response));

        } catch (Exception e) {
            log.error("❌ Erreur lors de la réinitialisation : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponseDto("Error: " + e.getMessage()));
        }
    }

}
