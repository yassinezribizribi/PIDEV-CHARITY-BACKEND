package tn.esprit.examen.nomPrenomClasseExamen.Controllers;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.nomPrenomClasseExamen.dto.TestimonialDTO;
import tn.esprit.examen.nomPrenomClasseExamen.jwt.JwtUtils;
import tn.esprit.examen.nomPrenomClasseExamen.services.ITestimonialServices;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/testimonials")
@AllArgsConstructor
public class TestimonialController {

    private final ITestimonialServices testimonialService;
    private final JwtUtils jwtUtils;
    private static final Logger logger = LoggerFactory.getLogger(TestimonialController.class);
    private static final String UPLOAD_DIR = "C:/temp/uploads/";

    /** ✅ Ajouter un témoignage */
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ASSOCIATION_MEMBER', 'ROLE_VOLUNTEER', 'ROLE_REFUGEE', 'ROLE_MENTOR')")
    public ResponseEntity<?> createTestimonial(@RequestBody TestimonialDTO testimonialDTO,
                                               @RequestHeader("Authorization") String token) {
        logger.info("📩 Ajout de témoignage reçu.");

        // 🚀 Vérification du token
        if (token == null || token.length() < 10) {
            logger.error("⛔ Token JWT manquant ou invalide !");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("⛔ Token JWT manquant ou invalide !");
        }

        logger.info("🔑 Token reçu: " + token.substring(0, 20) + "...");

        try {
            // 🔹 Traitement des images Base64
            if (testimonialDTO.getBeforePhotoBase64() != null) {
                testimonialDTO.setBeforePhotoPath(processImage(testimonialDTO.getBeforePhotoBase64(), "before"));
            }

            if (testimonialDTO.getAfterPhotoBase64() != null) {
                testimonialDTO.setAfterPhotoPath(processImage(testimonialDTO.getAfterPhotoBase64(), "after"));
            }

            return ResponseEntity.ok(testimonialService.addTestimonial(testimonialDTO, token));

        } catch (RuntimeException e) {
            logger.error("❌ Erreur lors du stockage des fichiers : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur stockage fichiers: " + e.getMessage());
        }
    }

    /** ✅ Récupérer tous les témoignages */
    @GetMapping("/all")
    public ResponseEntity<List<TestimonialDTO>> getAllTestimonials() {
        return ResponseEntity.ok(testimonialService.getAllTestimonials());
    }

    /** ✅ Récupérer un témoignage par ID */
    @GetMapping("/{id}")
    public ResponseEntity<TestimonialDTO> getTestimonialById(@PathVariable Long id) {
        Optional<TestimonialDTO> testimonial = testimonialService.getTestimonialById(id);
        return testimonial.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** ✅ Mettre à jour un témoignage */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ASSOCIATION_MEMBER', 'ROLE_VOLUNTEER', 'ROLE_REFUGEE', 'ROLE_MENTOR')")
    public ResponseEntity<TestimonialDTO> updateTestimonial(@PathVariable Long id,
                                                            @RequestBody TestimonialDTO testimonialDTO,
                                                            @RequestHeader("Authorization") String token) {
        if (!isValidToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(testimonialService.updateTestimonial(id, testimonialDTO, token));
    }

    /** ✅ Supprimer un témoignage */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ASSOCIATION_MEMBER', 'ROLE_VOLUNTEER', 'ROLE_REFUGEE', 'ROLE_MENTOR')")
    public ResponseEntity<Void> deleteTestimonial(@PathVariable Long id,
                                                  @RequestHeader("Authorization") String token) {
        if (!isValidToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        testimonialService.deleteTestimonial(id, token);
        return ResponseEntity.noContent().build();
    }

    /** ✅ Rechercher un témoignage par mot-clé */
    @GetMapping("/search/{keyword}")
    public ResponseEntity<List<TestimonialDTO>> searchTestimonials(@PathVariable String keyword) {
        List<TestimonialDTO> results = testimonialService.searchTestimonials(keyword);
        return ResponseEntity.ok(results);
    }

    /** ✅ Vérification du token JWT */
    private boolean isValidToken(String token) {
        return token != null && token.startsWith("Bearer ") && jwtUtils.validateJwtToken(token.substring(7));
    }

    /** ✅ Traitement des images Base64 */
    private String processImage(String base64Data, String prefix) {
        if (base64Data == null || base64Data.isEmpty()) return null;

        try {
            logger.info("📥 Base64 brut reçu (taille: {} caractères)", base64Data.length());

            // 🔹 Nettoyage avancé
            base64Data = cleanBase64Data(base64Data);

            // 🔹 Décodage Base64 sécurisé
            byte[] decodedBytes;
            try {
                decodedBytes = Base64.getDecoder().decode(base64Data);
            } catch (IllegalArgumentException e) {
                logger.error("❌ Erreur de décodage Base64: {}", e.getMessage());
                throw new RuntimeException("Format d'image invalide !");
            }

            // 🔹 Vérification et sauvegarde
            return saveFile(decodedBytes, prefix);
        } catch (RuntimeException e) {
            logger.error("❌ Erreur : {}", e.getMessage());
            throw new RuntimeException("Erreur lors du stockage de l'image !");
        }
    }


    /** ✅ Nettoyage avancé de la Base64 */
    private String cleanBase64Data(String base64Data) {
        if (base64Data.contains(",")) {
            base64Data = base64Data.substring(base64Data.indexOf(",") + 1);
        }

        base64Data = base64Data.replaceAll("[^A-Za-z0-9+/=]", "");
        base64Data = base64Data.replace("-", "+").replace("_", "/");

        while (base64Data.length() % 4 != 0) {
            base64Data += "=";
        }

        logger.info("✅ Base64 nettoyé (100 premiers caractères) : {}", base64Data.substring(0, Math.min(base64Data.length(), 100)));
        return base64Data;
    }

    /** ✅ Sauvegarde de l'image */
    private String saveFile(byte[] data, String prefix) {
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String filename = String.format("%s_%d.png", prefix, System.currentTimeMillis());
            Path filePath = uploadPath.resolve(filename);
            Files.write(filePath, data);

            return filename;
        } catch (IOException e) {
            logger.error("❌ Erreur lors de l'enregistrement de l'image: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de l'enregistrement de l'image !");
        }
    }
}
