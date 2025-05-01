package tn.esprit.examen.nomPrenomClasseExamen.Controllers;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.examen.nomPrenomClasseExamen.dto.*;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Association;
import tn.esprit.examen.nomPrenomClasseExamen.jwt.JwtUtils;
import tn.esprit.examen.nomPrenomClasseExamen.services.AssociationServices;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/api/associations")
@AllArgsConstructor
public class AssociationController {

    private final AssociationServices associationServices;
    private final JwtUtils jwtUtils;
    private static final String FILE_DIRECTORY = "C:\\Users\\hp\\Desktop\\uploads";
    private static final Logger logger = LoggerFactory.getLogger(AssociationController.class);

    // ============== FILE HANDLING ==============
    @GetMapping("/protected/files/{filename}")
    public ResponseEntity<Resource> getProtectedFile(
            @PathVariable String filename,
            @RequestHeader("Authorization") String token) throws IOException {

        if (!validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String decodedFilename = URLDecoder.decode(filename, StandardCharsets.UTF_8);
        Path filePath = Paths.get(FILE_DIRECTORY, decodedFilename);

        if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Resource resource = new UrlResource(filePath.toUri());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    // ============== CORE ASSOCIATION ENDPOINTS ==============
    @GetMapping("/list")
    public ResponseEntity<List<AssociationDto>> getAllAssociations() {
        return ResponseEntity.ok(associationServices.getAllAssociations());
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
            @RequestParam(value = "associationLogo", required = false) MultipartFile associationLogo,
            @RequestParam(value = "registrationDocument", required = false) MultipartFile registrationDocument,
            @RequestParam(value = "legalDocument", required = false) MultipartFile legalDocument,
            @RequestParam("status") String status,
            @RequestHeader("Authorization") String token) {

        if (!validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String jwt = token.substring(7);
        AssociationDto associationDto = new AssociationDto();
        associationDto.setAssociationName(associationName);
        associationDto.setAssociationAddress(associationAddress);
        associationDto.setDescription(description);
        associationDto.setStatus(Association.AssociationStatus.valueOf(status.toUpperCase()));
        associationDto.setPartnershipTier(Association.PartnershipTier.BRONZE);
        associationDto.setPartnershipScore(0);

        try {
            if (associationLogo != null) {
                saveFile(associationLogo, associationDto::setAssociationLogoPath);
            }
            if (registrationDocument != null) {
                saveFile(registrationDocument, associationDto::setRegistrationDocumentPath);
            }
            if (legalDocument != null) {
                saveFile(legalDocument, associationDto::setLegalDocumentPath);
            }

            return ResponseEntity.ok(associationServices.createAssociation(associationDto, jwt));

        } catch (RuntimeException e) {
            return handleServiceExceptions(e, status);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("File processing error: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAssociationBySubscriberIDUser(@RequestHeader("Authorization") String token) {
        if (!validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            String jwt = token.substring(7);
            return associationServices.getAssociationByUserId(jwt)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAssociation(
            @PathVariable Long id,
            @RequestBody AssociationDto associationDto,
            @RequestHeader("Authorization") String token) {

        if (!validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            AssociationDto updatedAssociation = associationServices.updateAssociation(id, associationDto, token);
            return ResponseEntity.ok(updatedAssociation);
        } catch (AssociationServices.UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (AssociationServices.ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/verify/{id}")
    public ResponseEntity<?> verifyAssociation(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        if (!validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            AssociationDto verifiedAssociation = associationServices.verifyAssociation(id, token);
            return ResponseEntity.ok(Map.of(
                    "message", "Association verified successfully",
                    "association", verifiedAssociation
            ));
        } catch (AssociationServices.ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAssociation(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        if (!validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            associationServices.deleteAssociation(id, token);
            return ResponseEntity.noContent().build();
        } catch (AssociationServices.UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (AssociationServices.ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // ============== PARTNERSHIP ENDPOINTS ==============
    @PostMapping("/{id}/partners/{partnerId}")
    public ResponseEntity<?> createPartnership(
            @PathVariable Long id,
            @PathVariable Long partnerId,
            @RequestHeader("Authorization") String token) {

        if (!validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            String jwt = token.substring(7);
            AssociationDto updatedAssociation = associationServices.createPartnership(id, partnerId, jwt);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Partnership established successfully",
                    "association", updatedAssociation,
                    "newPartnershipScore", updatedAssociation.getPartnershipScore(),
                    "newTier", updatedAssociation.getPartnershipTier().name(),
                    "tierBenefits", getTierBenefits(updatedAssociation.getPartnershipTier())
            ));
        } catch (AssociationServices.UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (AssociationServices.BusinessException | AssociationServices.ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/{id}/partners")
    public ResponseEntity<?> getPartners(@PathVariable Long id) {
        try {
            Set<AssociationDto> partners = associationServices.getPartners(id);
            return ResponseEntity.ok(partners);
        } catch (AssociationServices.ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/{id}/partner-recommendations")
    public ResponseEntity<?> getPartnerRecommendations(@PathVariable Long id) {
        try {
            List<AssociationDto> recommendations = associationServices.getRecommendedPartners(id);
            return ResponseEntity.ok(recommendations);
        } catch (AssociationServices.ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/{id}/partnership-tier")
    public ResponseEntity<?> getPartnershipTier(@PathVariable Long id) {
        try {
            PartnershipTierDto tierInfo = associationServices.getPartnershipTierInfo(id);
            return ResponseEntity.ok(Map.of(
                    "currentTier", tierInfo.getTier().name(),
                    "score", tierInfo.getScore(),
                    "nextTierThreshold", getNextTierThreshold(tierInfo.getTier()),
                    "maxPartners", getMaxPartnersForTier(tierInfo.getTier()),
                    "benefits", getTierBenefits(tierInfo.getTier())
            ));
        } catch (AssociationServices.ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/{id}/partnership-impact")
    public ResponseEntity<?> getPartnershipImpactReport(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        if (!validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            PartnershipImpactReportDto report = associationServices.generateImpactReport(id, email);
            return ResponseEntity.ok(report);
        } catch (AssociationServices.ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
    @PostMapping("/tier-upgrade/{associationId}")
    public ResponseEntity<Map<String, Association.PartnershipTier>> upgradeTier(
            @PathVariable Long associationId,
            @RequestParam(required = true) String currentTier,
            @RequestParam(required = true) String reason) {

        // Validate current tier
        Association.PartnershipTier currentTierEnum = Association.PartnershipTier.valueOf(currentTier);

        // Perform the upgrade
        Association.PartnershipTier newTier = associationServices.upgradeAssociationTier(
                associationId,
                currentTierEnum,
                reason
        );

        // Return response
        Map<String, Association.PartnershipTier> response = new HashMap<>();
        response.put("newTier", newTier);

        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/{id}/partners/{partnerId}")
    public ResponseEntity<?> removePartnership(
            @PathVariable Long id,
            @PathVariable Long partnerId,
            @RequestHeader("Authorization") String token) {

        if (!validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            String jwt = token.substring(7);
            associationServices.removePartnership(id, partnerId, jwt);

            AssociationDto association = associationServices.getAssociationById(id)
                    .orElseThrow(() -> new AssociationServices.ResourceNotFoundException("Association not found"));

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Partnership removed successfully",
                    "associationId", id,
                    "partnerId", partnerId,
                    "newPartnershipScore", association.getPartnershipScore(),
                    "newTier", association.getPartnershipTier().name()
            ));
        } catch (AssociationServices.UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (AssociationServices.BusinessException | AssociationServices.ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
    @GetMapping("/{id}/user-email")
    public ResponseEntity<Map<String, String>> getUserEmailByAssociationId(@PathVariable("id") Long associationId) {
        try {
            String email = associationServices.getUserEmailByAssociationId(associationId);
            Map<String, String> response = Collections.singletonMap("email", email);
            return ResponseEntity.ok(response);
        } catch (AssociationServices.ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", ex.getMessage()));
        }
    }
    @GetMapping("/paginated")
    public Page<Association> getPaginatedAssociations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "") String location,
            @RequestParam(defaultValue = "") String tier
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return associationServices.findPaginatedAssociations(search, location, tier, pageable);
    }
    // ============== HELPER METHODS ==============
    private Map<String, String> getTierBenefits(Association.PartnershipTier tier) {
        return switch (tier) {
            case BRONZE -> Map.of(
                    "maxPartners", "3",
                    "benefits", "Basic partnership features, Limited resource sharing"
            );
            case SILVER -> Map.of(
                    "maxPartners", "5",
                    "benefits", "Advanced analytics, Partner recommendations, Enhanced resource sharing"
            );
            case GOLD -> Map.of(
                    "maxPartners", "10",
                    "benefits", "Priority support, Joint funding opportunities, Unlimited resource sharing"
            );
        };
    }

    private int getNextTierThreshold(Association.PartnershipTier tier) {
        return switch (tier) {
            case BRONZE -> 31;
            case SILVER -> 61;
            case GOLD -> -1; // Indicates highest tier
        };
    }

    private int getMaxPartnersForTier(Association.PartnershipTier tier) {
        return switch (tier) {
            case BRONZE -> 3;
            case SILVER -> 5;
            case GOLD -> 10;
        };
    }

    private ResponseEntity<?> handleServiceExceptions(RuntimeException e, String status) {
        if (e instanceof AssociationServices.BusinessException) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
        if (e instanceof AssociationServices.ResourceNotFoundException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
        if (e instanceof IllegalArgumentException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid status value: " + status);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }

    private void saveFile(MultipartFile file, PathConsumer pathConsumer) throws IOException {
        if (file != null && !file.isEmpty()) {
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path path = Paths.get(FILE_DIRECTORY, filename);
            Files.createDirectories(path.getParent());
            file.transferTo(path);
            pathConsumer.accept(filename);
        }
    }

    private boolean validateToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return false;
        }
        String jwt = token.substring(7);
        return jwtUtils.validateJwtToken(jwt);
    }


    @FunctionalInterface
    private interface PathConsumer {
        void accept(String path);
    }
}

