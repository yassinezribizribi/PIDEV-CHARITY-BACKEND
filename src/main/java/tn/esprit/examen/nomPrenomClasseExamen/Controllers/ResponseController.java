package tn.esprit.examen.nomPrenomClasseExamen.Controllers;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.nomPrenomClasseExamen.dto.ResponseDto;
import tn.esprit.examen.nomPrenomClasseExamen.services.IResponseServices;

import java.util.List;

@RestController
@RequestMapping("/api/responses")
@AllArgsConstructor
public class ResponseController {

    private final IResponseServices responseService;
    private static final Logger logger = LoggerFactory.getLogger(ResponseController.class);

    @GetMapping
    public ResponseEntity<List<ResponseDto>> getAllResponses() {
        return ResponseEntity.ok(responseService.getAllResponses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto> getResponseById(@PathVariable Long id) {
        return ResponseEntity.ok(responseService.getResponseById(id));
    }

    @PostMapping
    public ResponseEntity<?> createResponse(@RequestBody ResponseDto responseDto) {
        try {
            logger.info("📝 Creating response: {}", responseDto);
            ResponseDto createdResponse = responseService.createResponse(responseDto);
            return ResponseEntity.ok(createdResponse);
        } catch (Exception e) {
            logger.error("❌ Error creating response: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateResponse(@PathVariable Long id, @RequestBody ResponseDto responseDto) {
        try {
            logger.info("🔄 Updating response with ID: {}", id);
            ResponseDto updatedResponse = responseService.updateResponse(id, responseDto);
            return ResponseEntity.ok(updatedResponse);
        } catch (Exception e) {
            logger.error("❌ Error updating response: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResponse(@PathVariable Long id) {
        logger.info("🗑 Deleting response with ID: {}", id);
        responseService.deleteResponse(id);
        return ResponseEntity.noContent().build();
    }
}
