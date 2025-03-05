package tn.esprit.examen.nomPrenomClasseExamen.Controllers;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.RequestRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.ResponseRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.UserRepository;
import tn.esprit.examen.nomPrenomClasseExamen.dto.ResponseDto;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Request;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Response;
import tn.esprit.examen.nomPrenomClasseExamen.entities.User;
import tn.esprit.examen.nomPrenomClasseExamen.services.IRequestServices;
import tn.esprit.examen.nomPrenomClasseExamen.services.IResponseServices;


import java.util.List;
import java.util.NoSuchElementException;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/responses")
@AllArgsConstructor
public class ResponseController {

    private final IResponseServices responseService;
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(ResponseController.class);
    private final ResponseRepository responseRepository;

    @GetMapping("/all")
    public ResponseEntity<List<ResponseDto>> getAllResponses() {
        return ResponseEntity.ok(responseService.getAllResponses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto> getResponseById(@PathVariable Long id) {
        return ResponseEntity.ok(responseService.getResponseById(id));
    }

    // Nouvelle méthode pour récupérer les réponses par requestId
    @GetMapping("/by-request/{requestId}")
    public ResponseEntity<List<ResponseDto>> getResponsesByRequestId(@PathVariable Long requestId) {
        logger.info("📢 Fetching responses for request with ID: {}", requestId);
        List<ResponseDto> responses = responseService.getResponsesByRequestId(requestId);
        return ResponseEntity.ok(responses);
    }


    // Dans ton contrôleur :
    @PostMapping("/add")
    public ResponseEntity<Response> addResponse( @RequestBody Response ResponseDto) {
        Request request = requestRepository.findById(ResponseDto.getRequest().getIdRequest())
                .orElseThrow(() -> new NoSuchElementException("Request not found"));

        ResponseDto.setRequest(request);  // Associer la demande à la réponse

        User sender = userRepository.findById(ResponseDto.getSender().getIdUser())
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        ResponseDto.setSender(sender);  // Associer l'utilisateur qui envoie la réponse

        Response savedResponse = responseRepository.save(ResponseDto);  // Sauvegarder la réponse dans la DB
        return ResponseEntity.ok(savedResponse);
    }



    @PutMapping("/update/{id}")
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

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteResponse(@PathVariable Long id) {
        try {
            logger.info("🗑 Deleting response with ID: {}", id);
            responseService.deleteResponse(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("❌ Error deleting response: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}
