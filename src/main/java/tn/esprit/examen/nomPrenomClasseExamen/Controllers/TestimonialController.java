// ✅ CONTROLLER CORRIGÉ
package tn.esprit.examen.nomPrenomClasseExamen.Controllers;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.examen.nomPrenomClasseExamen.dto.TestimonialDTO;
import tn.esprit.examen.nomPrenomClasseExamen.jwt.JwtUtils;
import tn.esprit.examen.nomPrenomClasseExamen.services.ITestimonialServices;
import tn.esprit.examen.nomPrenomClasseExamen.services.TranscriptionServices;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/testimonials")
@AllArgsConstructor
public class TestimonialController {

    private final ITestimonialServices testimonialService;
    private final JwtUtils jwtUtils;
    private final TranscriptionServices transcriptionService;

    private static final Logger logger = LoggerFactory.getLogger(TestimonialController.class);
    private static final String UPLOAD_DIR = "C:/temp/uploads/";

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ASSOCIATION_MEMBER', 'ROLE_VOLUNTEER', 'ROLE_REFUGEE', 'ROLE_MENTOR')")
    public ResponseEntity<?> createTestimonial(@RequestBody TestimonialDTO testimonialDTO,
                                               @RequestHeader("Authorization") String tokenHeader) {
        try {
            String token = tokenHeaderToToken(tokenHeader);
            if (!jwtUtils.validateJwtToken(token))
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("⛔ Token JWT invalide !");

            if (testimonialDTO.getBeforePhotoBase64() != null && !testimonialDTO.getBeforePhotoBase64().isEmpty())
                testimonialDTO.setBeforePhotoPath(processImage(testimonialDTO.getBeforePhotoBase64(), "before"));

            if (testimonialDTO.getAfterPhotoBase64() != null && !testimonialDTO.getAfterPhotoBase64().isEmpty())
                testimonialDTO.setAfterPhotoPath(processImage(testimonialDTO.getAfterPhotoBase64(), "after"));

            TestimonialDTO savedDto = testimonialService.addTestimonial(testimonialDTO, token);
            return ResponseEntity.ok(savedDto);
        } catch (Exception e) {
            logger.error("❌ Erreur lors de l'ajout du témoignage : {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur : " + e.getMessage());
        }
    }

    @PostMapping("/media")
    @PreAuthorize("hasAnyRole('ROLE_ASSOCIATION_MEMBER', 'ROLE_VOLUNTEER', 'ROLE_REFUGEE', 'ROLE_MENTOR')")
    public ResponseEntity<?> uploadMediaTestimonial(@RequestParam("file") MultipartFile file,
                                                    @RequestParam("mediaType") String mediaType,
                                                    @RequestHeader("Authorization") String tokenHeader) {
        try {
            String token = tokenHeaderToToken(tokenHeader);
            if (!jwtUtils.validateJwtToken(token))
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

            String filename = saveMedia(file);
            logger.info("✅ Média sauvegardé avec succès : {}", filename);

            String transcription;
            try {
                transcription = transcriptionService.transcribeMedia(Paths.get(UPLOAD_DIR).resolve(filename));
            } catch (Exception ex) {
                logger.warn("⚠️ Transcription échouée pour {} : {}", filename, ex.getMessage());
                transcription = "Transcription non disponible pour ce fichier.";
            }

            TestimonialDTO dto = new TestimonialDTO();
            dto.setMediaPath("/api/testimonials/media/" + filename);
            dto.setMediaType(mediaType);
            dto.setTranscriptionText(transcription);
            dto.setContent("Témoignage média");
            dto.setDescription("Témoignage avec transcription automatique IA");

            TestimonialDTO savedDto = testimonialService.addTestimonial(dto, token);
            return ResponseEntity.ok(savedDto);
        } catch (Exception e) {
            logger.error("❌ Erreur lors de l'upload média : {}", file.getOriginalFilename(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur : " + e.getMessage());
        }
    }

    @PostMapping("/{id}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> toggleLike(@PathVariable Long id, @RequestHeader("Authorization") String tokenHeader) {
        try {
            String token = tokenHeaderToToken(tokenHeader);
            testimonialService.likeTestimonial(id, token);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors du like: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteTestimonial(@PathVariable Long id, @RequestHeader("Authorization") String tokenHeader) {
        try {
            String token = tokenHeaderToToken(tokenHeader);
            testimonialService.deleteTestimonial(id, token);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur suppression: " + e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<TestimonialDTO>> getAllTestimonials(@RequestHeader(value = "Authorization", required = false) String tokenHeader) {
        String token = null;
        if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
            token = tokenHeader.substring(7);
        }
        return ResponseEntity.ok(testimonialService.getAllTestimonials(token));
    }

    @GetMapping("/media/{filename:.+}")
    public ResponseEntity<Resource> getMedia(@PathVariable String filename) {
        return returnFile(filename);
    }

    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        return returnFile(filename);
    }

    private ResponseEntity<Resource> returnFile(String filename) {
        try {
            Path file = Paths.get(UPLOAD_DIR).resolve(filename).normalize();
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() && resource.isReadable()) {
                String contentType = Files.probeContentType(file);
                if (contentType == null) contentType = "application/octet-stream";

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                throw new RuntimeException("❌ Fichier non trouvé ou illisible : " + filename);
            }
        } catch (Exception e) {
            throw new RuntimeException("❌ Erreur dans returnFile() : " + e.getMessage());
        }
    }

    private String tokenHeaderToToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer "))
            return authHeader.substring(7).trim();
        throw new RuntimeException("⛔ Token JWT manquant ou invalide !");
    }

    private String processImage(String base64Data, String prefix) throws IOException {
        if (base64Data.contains(","))
            base64Data = base64Data.substring(base64Data.indexOf(",") + 1);
        base64Data = base64Data.replaceAll("\\s+", "");
        byte[] decodedBytes = Base64.getDecoder().decode(base64Data);
        return saveFile(decodedBytes, prefix);
    }

    private String saveFile(byte[] data, String prefix) throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
        String filename = prefix + "_" + System.currentTimeMillis() + ".png";
        Files.write(uploadPath.resolve(filename), data);
        return filename;
    }

    private String saveMedia(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
        String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath);
        return filename;
    }
}
