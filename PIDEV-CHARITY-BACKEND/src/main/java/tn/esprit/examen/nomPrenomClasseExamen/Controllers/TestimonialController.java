package tn.esprit.examen.nomPrenomClasseExamen.Controllers;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tn.esprit.examen.nomPrenomClasseExamen.dto.TestimonialDTO;
import tn.esprit.examen.nomPrenomClasseExamen.jwt.JwtUtils;
import tn.esprit.examen.nomPrenomClasseExamen.services.ITestimonialServices;
import tn.esprit.examen.nomPrenomClasseExamen.services.TranscriptionServices;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Base64;
import java.util.List;

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
    @PreAuthorize("hasAnyRole('ROLE_ASSOCIATION_MEMBER','ROLE_VOLUNTEER','ROLE_REFUGEE','ROLE_MENTOR')")
    public ResponseEntity<?> createTestimonial(
            @RequestBody TestimonialDTO dto,
            @RequestHeader("Authorization") String auth
    ) {
        try {
            String token = extractToken(auth);

            if (dto.getBeforePhotoBase64() != null) {
                dto.setBeforePhotoPath(processImage(dto.getBeforePhotoBase64(), "before"));
            }
            if (dto.getAfterPhotoBase64() != null) {
                dto.setAfterPhotoPath(processImage(dto.getAfterPhotoBase64(), "after"));
            }

            TestimonialDTO saved = testimonialService.addTestimonial(dto, token);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            logger.error("❌ createTestimonial:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/media")
    @PreAuthorize("hasAnyRole('ROLE_ASSOCIATION_MEMBER','ROLE_VOLUNTEER','ROLE_REFUGEE','ROLE_MENTOR')")
    public ResponseEntity<?> uploadMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam("mediaType") String mediaType,
            @RequestParam("content") String content,
            @RequestParam("description") String description,
            @RequestHeader("Authorization") String auth
    ) {
        try {
            String token = extractToken(auth);
            String cleanedName = file.getOriginalFilename().replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");
            String filename = System.currentTimeMillis() + "_" + cleanedName;
            Path dest = Paths.get(UPLOAD_DIR).resolve(filename);
            Files.createDirectories(dest.getParent());
            Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

            String transcription;
            try {
                transcription = transcriptionService.transcribeMedia(dest);
            } catch (Exception ex) {
                transcription = "Transcription non disponible";
            }

            String fileUrl = "http://localhost:8089/api/testimonials/media/" + URLEncoder.encode(filename, StandardCharsets.UTF_8);
            TestimonialDTO dto = new TestimonialDTO();
            dto.setMediaPath(fileUrl);
            dto.setMediaType(mediaType);
            dto.setContent(content);
            dto.setDescription(description);


            TestimonialDTO saved = testimonialService.addTestimonial(dto, token);
            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            logger.error("❌ uploadMedia:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> likeTestimonial(
            @PathVariable Long id,
            @RequestHeader("Authorization") String auth
    ) {
        try {
            String token = extractToken(auth);
            testimonialService.likeTestimonial(id, token);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("❌ Error while liking:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{id}/unlike")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> unlikeTestimonial(
            @PathVariable Long id,
            @RequestHeader("Authorization") String auth
    ) {
        try {
            String token = extractToken(auth);
            testimonialService.unlikeTestimonial(id, token);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("❌ Error while unliking:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteTestimonial(
            @PathVariable Long id,
            @RequestHeader("Authorization") String auth
    ) {
        try {
            String token = extractToken(auth);
            testimonialService.deleteTestimonial(id, token);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("❌ deleteTestimonial:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<TestimonialDTO>> getAll(
            @RequestHeader(value = "Authorization", required = false) String auth
    ) {
        String token = (auth != null && auth.startsWith("Bearer ")) ? auth.substring(7) : null;
        List<TestimonialDTO> list = testimonialService.getAllTestimonials(token);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/media/{filename:.+}")
    public ResponseEntity<Resource> getMedia(@PathVariable String filename, @RequestHeader(value = "Range", required = false) String rangeHeader) {
        try {
            Path filePath = Paths.get(UPLOAD_DIR).resolve(filename).normalize();
            if (!Files.exists(filePath)) return ResponseEntity.notFound().build();

            long fileSize = Files.size(filePath);
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) contentType = "application/octet-stream";
            InputStream inputStream = Files.newInputStream(filePath);

            if (rangeHeader == null) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, contentType)
                        .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileSize))
                        .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                        .body(new InputStreamResource(inputStream));
            }

            HttpRange range = HttpRange.parseRanges(rangeHeader).get(0);
            long start = range.getRangeStart(fileSize);
            long end = range.getRangeEnd(fileSize);
            long chunkSize = end - start + 1;

            inputStream.skip(start);
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileSize)
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(chunkSize))
                    .body(new InputStreamResource(inputStream));
        } catch (Exception e) {
            logger.error("❌ getMedia:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        try {
            Path file = Paths.get(UPLOAD_DIR).resolve(filename).normalize();
            if (!Files.exists(file)) return ResponseEntity.notFound().build();

            String contentType = Files.probeContentType(file);
            InputStreamResource resource = new InputStreamResource(Files.newInputStream(file));

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(Files.size(file)))
                    .body(resource);

        } catch (Exception e) {
            logger.error("❌ getImage:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // === Helpers ===

    private String extractToken(String header) {
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7).trim();
        }
        throw new RuntimeException("⛔ Token JWT manquant");
    }

    private String processImage(String base64, String prefix) throws Exception {
        String data = base64.contains(",") ? base64.split(",")[1] : base64;
        byte[] bytes = Base64.getDecoder().decode(data);
        String filename = prefix + "_" + System.currentTimeMillis() + ".png";
        Path dest = Paths.get(UPLOAD_DIR).resolve(filename);
        Files.createDirectories(dest.getParent());
        Files.write(dest, bytes);
        return filename;
    }
}
