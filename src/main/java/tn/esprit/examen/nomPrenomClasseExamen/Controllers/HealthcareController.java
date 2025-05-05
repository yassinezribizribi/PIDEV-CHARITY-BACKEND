package tn.esprit.examen.nomPrenomClasseExamen.Controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.nomPrenomClasseExamen.dto.HealthcareDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Healthcare;
import tn.esprit.examen.nomPrenomClasseExamen.jwt.JwtUtils;
import tn.esprit.examen.nomPrenomClasseExamen.services.IHealthcareServices;
import tn.esprit.examen.nomPrenomClasseExamen.services.PdfService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/healthcare")
@CrossOrigin(origins = {
        "http://localhost:4200",
        "https://datastudio.google.com",
        "https://lookerstudio.googleusercontent.com"
})
public class HealthcareController {

    @Autowired
    private IHealthcareServices healthcareService;

    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private PdfService pdfService;


    // ✅ Ajouter un soin avec génération automatique du lien Jitsi
    @PostMapping("/add")
    public ResponseEntity<Healthcare> createHealthcare(@RequestBody HealthcareDTO healthcareDTO) {
        try {
            Healthcare healthcare = healthcareService.addHealthcare(healthcareDTO);
            log.info("✅ Rendez-vous ajouté avec succès. Lien vidéo : {}", healthcare.getMeetingUrl());
            return ResponseEntity.status(HttpStatus.CREATED).body(healthcare);
        } catch (Exception e) {
            log.error("❌ Erreur lors de l'ajout du soin : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // ✅ Modifier un soin existant
    @PutMapping("/{id}")
    public ResponseEntity<Healthcare> updateHealthcare(@PathVariable Long id,
                                                       @RequestBody HealthcareDTO healthcareDTO) {
        try {
            Healthcare updatedHealthcare = healthcareService.updateHealthcare(id, healthcareDTO);
            log.info("✅ Rendez-vous mis à jour avec succès. ID : {}", id);
            return ResponseEntity.ok(updatedHealthcare);
        } catch (RuntimeException e) {
            log.error("❌ Erreur lors de la mise à jour du soin ID {} : {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadHealthcarePdf(@PathVariable Long id) {
        Optional<Healthcare> optional = healthcareService.getHealthcareById(id);
        if (optional.isEmpty()) return ResponseEntity.notFound().build();

        byte[] pdf = pdfService.generatePdfFromHealthcare(optional.get());


        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=rapport_soin_" + id + ".pdf")
                .header("Content-Type", "application/pdf")
                .body(pdf);
    }

    // ✅ Récupérer mes notifications (ROLE_REFUGEE)
    @GetMapping("/my-notifications")
    @PreAuthorize("hasRole('ROLE_REFUGEE')")
    public ResponseEntity<List<HealthcareDTO>> getMyNotifications(
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "").trim();
            Long userId = jwtUtils.getUserIdFromJwtToken(token);
            List<HealthcareDTO> list = healthcareService.getAllHealthcare().stream()
                    .filter(h -> h.getSubscriberId() != null && h.getSubscriberId().equals(userId))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            log.error("❌ Erreur notifications : {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ✅ Modifier uniquement le statut et la date du rendez-vous
    @PatchMapping("/{id}/status")
    public ResponseEntity<Healthcare> updateHealthcareStatus(@PathVariable Long id,
                                                             @RequestBody HealthcareDTO dto) {
        try {
            Healthcare updated = healthcareService.updateHealthcareStatus(
                    id, dto.getStatus(), dto.getBookingDate());
            log.info("✅ Statut mis à jour. ID : {}", id);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            log.error("❌ Erreur mise à jour statut ID {} : {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // ✅ Proposer une nouvelle date (ROLE_REFUGEE)
    @PatchMapping("/{id}/propose")
    @PreAuthorize("hasRole('ROLE_REFUGEE')")
    public ResponseEntity<Healthcare> proposeDate(@PathVariable Long id,
                                                  @RequestBody HealthcareDTO dto) {
        Healthcare h = healthcareService.proposeAppointment(id, dto.getProposedBookingDate());
        return ResponseEntity.ok(h);
    }

    // ✅ Confirmer la date proposée (ROLE_VOLUNTEER)
    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasRole('ROLE_VOLUNTEER')")
    public ResponseEntity<Healthcare> confirmDate(@PathVariable Long id) {
        Healthcare h = healthcareService.confirmAppointment(id);
        return ResponseEntity.ok(h);
    }

    // ✅ Récupérer un soin par ID
    @GetMapping("/{id}")
    public ResponseEntity<Healthcare> getHealthcareById(@PathVariable Long id) {
        Optional<Healthcare> h = healthcareService.getHealthcareById(id);
        return h.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ✅ Récupérer tous les soins
    @GetMapping("/all")
    public ResponseEntity<List<HealthcareDTO>> getAllHealthcare() {
        return ResponseEntity.ok(healthcareService.getAllHealthcare());
    }

    // ✅ Exporter pour Power BI (JSON)
    @GetMapping("/export")
    public ResponseEntity<List<HealthcareDTO>> exportHealthcareForPowerBI() {
        return ResponseEntity.ok(healthcareService.getAllHealthcare());
    }

    // ✅ Ancien PDF statique (optionnel)
    @GetMapping("/report-url")
    public ResponseEntity<String> getPowerBIReportUrl() {
        return ResponseEntity.ok("http://localhost:8089/report/HealthcareReport.pdf");
    }

    // ✅ Endpoint JSON pour Looker Studio
    @GetMapping("/healthcare")
    public List<HealthcareDTO> getHealthcareForLooker() {
        return healthcareService.getAllHealthcare();
    }

    // ✅ Supprimer un soin
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHealthcare(@PathVariable Long id) {
        try {
            healthcareService.deleteHealthcare(id);
            log.info("✅ Rendez-vous supprimé. ID : {}", id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("❌ Erreur suppression ID {} : {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
