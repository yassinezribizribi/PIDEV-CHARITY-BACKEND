package tn.esprit.examen.nomPrenomClasseExamen.Controllers;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.ResponseRepository;
import tn.esprit.examen.nomPrenomClasseExamen.dto.RequestDTO;
import tn.esprit.examen.nomPrenomClasseExamen.dto.ResponseDto;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Request;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Response;
import tn.esprit.examen.nomPrenomClasseExamen.services.RequestService;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/requests")
@AllArgsConstructor
public class RequestController {

    private final RequestService requestService;
    private final ResponseRepository responseRepository;
    private static final Logger logger = LoggerFactory.getLogger(RequestController.class);

    @GetMapping("/all")
    public ResponseEntity<List<Request>> getAllRequests() {
        logger.info("📢 Récupération de toutes les demandes...");
        return ResponseEntity.ok(requestService.getAllRequests());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Request> getRequestById(@PathVariable Long id) {
        logger.info("🔍 Recherche de la demande avec l'ID: {}", id);
        return ResponseEntity.ok(requestService.getRequestById(id));
    }

    @PostMapping("/responses/{requestId}")
    public ResponseEntity<?> addResponseToRequest(
            @PathVariable Long requestId,
            @RequestBody ResponseDto responseDto
    ) {
        try {
            Response response = requestService.addResponseToRequest(requestId, responseDto);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateRequest(@PathVariable Long id, @RequestBody RequestDTO requestDTO) {
        try {
            logger.info("🔄 Requête PUT reçue pour mettre à jour la demande avec ID: {}", id);
            Request updatedRequest = requestService.updateRequest(id, requestDTO);
            return ResponseEntity.ok(updatedRequest);
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la mise à jour de la demande: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Erreur: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteRequest(@PathVariable Long id) {
        logger.info("🗑 Requête DELETE reçue pour supprimer la demande avec ID: {}", id);
        requestService.deleteRequest(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/add")
    public ResponseEntity<Request> createRequest(@RequestBody RequestDTO requestDTO) {
        try {
            logger.info("📝 Requête reçue pour créer une nouvelle demande: {}", requestDTO);
            Request savedRequest = requestService.createRequest(requestDTO);
            return ResponseEntity.ok(savedRequest);
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la création de la demande: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        }
    }
}
