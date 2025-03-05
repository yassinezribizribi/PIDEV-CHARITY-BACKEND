package tn.esprit.examen.nomPrenomClasseExamen.Controllers;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.examen.nomPrenomClasseExamen.dto.AssociationDto;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Association;
import tn.esprit.examen.nomPrenomClasseExamen.jwt.JwtUtils;
import tn.esprit.examen.nomPrenomClasseExamen.services.AssociationServices;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/associations")
@AllArgsConstructor
public class AssociationController {

    private final AssociationServices associationServices;
    private final JwtUtils jwtUtils;
    private static final String FILE_DIRECTORY = "C:\\Users\\hp\\Desktop\\uploads";
    private static final Logger logger = LoggerFactory.getLogger(AssociationController.class);

    // File serving endpoint
    @GetMapping("/protected/files/{filename}")
    public ResponseEntity<Resource> getProtectedFile(
            @PathVariable String filename,
            @RequestHeader("Authorization") String token) throws IOException {

        // Validate token format
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Extract and validate JWT
        String jwt = token.substring(7);
        if (!jwtUtils.validateJwtToken(jwt)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Decode filename and resolve path
        String decodedFilename = URLDecoder.decode(filename, StandardCharsets.UTF_8);
        Path filePath = Paths.get(FILE_DIRECTORY, decodedFilename);

        // Verify file existence
        if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Create resource and headers
        Resource resource = new UrlResource(filePath.toUri());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/list")
    public ResponseEntity<List<AssociationDto>> getAllAssociations() {
        List<AssociationDto> associations = associationServices.getAllAssociations();
        return ResponseEntity.ok(associations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssociationDto> getAssociationById(@PathVariable Long id) {
        return associationServices.getAssociationById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createAssociation(
            @RequestParam("associationName") String associationName,
            @RequestParam("associationAddress") String associationAddress,
            @RequestParam("description") String description,
            @RequestParam("associationLogo") MultipartFile associationLogo,
            @RequestParam("registrationDocument") MultipartFile registrationDocument,
            @RequestParam("legalDocument") MultipartFile legalDocument,
            @RequestParam("status") String status,
            @RequestHeader("Authorization") String token) {

        // Validate token format
        if (!token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String jwt = token.substring(7);
        if (!jwtUtils.validateJwtToken(jwt)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        AssociationDto associationDto = new AssociationDto();
        associationDto.setAssociationName(associationName);
        associationDto.setAssociationAddress(associationAddress);
        associationDto.setDescription(description);

        try {
            // Save files and store only filenames
            saveFile(associationLogo, associationDto::setAssociationLogoPath);
            saveFile(registrationDocument, associationDto::setRegistrationDocumentPath);
            saveFile(legalDocument, associationDto::setLegalDocumentPath);

            associationDto.setStatus(Association.AssociationStatus.valueOf(status));
            return ResponseEntity.ok(associationServices.createAssociation(associationDto, jwt));

        } catch (RuntimeException e) {
            if (e.getMessage().contains("already has an association")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
            }
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
            if (e instanceof IllegalArgumentException) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid status value: " + status);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File processing error: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAssociationBySubscriberIDUser(@RequestHeader("Authorization") String token) {
        try {
            if (!token.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String jwt = token.substring(7);
            if (!jwtUtils.validateJwtToken(jwt)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Optional<AssociationDto> association = associationServices.getAssociationByUserId(jwt);
            return association.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    private void saveFile(MultipartFile file, PathConsumer pathConsumer) throws IOException {
        if (file != null && !file.isEmpty()) {
            // Only store the filename
            String filename = file.getOriginalFilename();
            Path path = Paths.get(FILE_DIRECTORY, filename);
            Files.createDirectories(path.getParent());
            file.transferTo(path);

            // Use only filename in the DTO
            pathConsumer.accept(filename);  // Store only the filename
        }
    }

    @FunctionalInterface
    private interface PathConsumer {
        void accept(String path);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssociationDto> updateAssociation(
            @PathVariable Long id,
            @RequestBody AssociationDto associationDto,
            @RequestHeader("Authorization") String token) {

        logger.info("Received update request for association ID: {}", id);
        logger.info("Request body: {}", associationDto);

        if (!validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(associationServices.updateAssociation(id, associationDto, token));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/verify/{id}")
    public ResponseEntity<?> verifyAssociation(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        try {
            AssociationDto verifiedAssociation = associationServices.verifyAssociation(id, token);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Association verified successfully");
            response.put("association", verifiedAssociation);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssociation(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        if (!validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        associationServices.deleteAssociation(id, token);
        return ResponseEntity.noContent().build();
    }

    private boolean validateToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) return false;
        String jwt = token.substring(7);
        return jwtUtils.validateJwtToken(jwt);
    }
}
