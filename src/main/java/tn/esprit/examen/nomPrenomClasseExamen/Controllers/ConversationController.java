package tn.esprit.examen.nomPrenomClasseExamen.Controllers;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.SubscriberRepository;
import tn.esprit.examen.nomPrenomClasseExamen.dto.SendMessageRequest;
import tn.esprit.examen.nomPrenomClasseExamen.dto.StartConversationRequest;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Conversation;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Message;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Subscriber;
import tn.esprit.examen.nomPrenomClasseExamen.jwt.JwtUtils;
import tn.esprit.examen.nomPrenomClasseExamen.services.ConversationService;
import tn.esprit.examen.nomPrenomClasseExamen.services.SubDetailsImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {
    private ConversationService conversationService;
    private SubscriberRepository subscriberRepository;
    private static final String FILE_DIRECTORY = "C:\\Users\\hp\\Desktop\\uploads";
    private JwtUtils jwtUtils;  // Add to constructor injection
    public ConversationController(ConversationService conversationService,
                                  SubscriberRepository subscriberRepository,
                                  JwtUtils jwtUtils) {
        this.conversationService = conversationService;
        this.subscriberRepository = subscriberRepository;
        this.jwtUtils = jwtUtils;
    }
    @PostMapping("/upload")
    public ResponseEntity<?> uploadAttachment(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String token) {

        if (!validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            String filename = saveConversationFile(file);
            return ResponseEntity.ok(Map.of(
                    "filename", filename,
                    "message", "File uploaded successfully"
            ));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("File upload failed: " + e.getMessage());
        }
    }

    @GetMapping("/files/{filename}")
    public ResponseEntity<Resource> getFile(
            @PathVariable String filename,
            @RequestHeader("Authorization") String token) throws IOException {

        if (!validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Path filePath = Paths.get(FILE_DIRECTORY, filename);
        Resource resource = new UrlResource(filePath.toUri());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    // Add these helper methods
    private String saveConversationFile(MultipartFile file) throws IOException {
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path path = Paths.get(FILE_DIRECTORY, filename);
        Files.createDirectories(path.getParent());
        file.transferTo(path);
        return filename;
    }

    private boolean validateToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) return false;
        String jwt = token.substring(7);
        return jwtUtils.validateJwtToken(jwt);
    }


    private Subscriber getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        SubDetailsImpl userDetails = (SubDetailsImpl) authentication.getPrincipal();
        return subscriberRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @PostMapping("/start")
    public ResponseEntity<Conversation> startConversation(
            @RequestBody StartConversationRequest request
    ) {
        Subscriber currentUser = getCurrentUser();
        return ResponseEntity.ok(
                conversationService.getOrCreateConversation(
                        currentUser.getIdUser(),
                        request.getParticipant2Id()
                )
        );
    }
    @GetMapping
    public ResponseEntity<List<Conversation>> getUserConversations() {
        Subscriber currentUser = getCurrentUser();
        return ResponseEntity.ok(
                conversationService.getUserConversations(currentUser.getIdUser())
        );
    }

    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<List<Message>> getMessages(
            @PathVariable Long conversationId
    ) {
        return ResponseEntity.ok(
                conversationService.getConversationMessages(conversationId)
        );
    }

    @PostMapping("/{conversationId}/messages")
    public ResponseEntity<Message> sendMessage(
            @PathVariable Long conversationId,
            @RequestBody SendMessageRequest request
    ) {
        Subscriber currentUser = getCurrentUser();
        return ResponseEntity.ok(
                conversationService.sendMessage(
                        conversationId,
                        currentUser.getIdUser(),
                        request.getContent()
                )
        );
    }

    @PutMapping("/{conversationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long conversationId) {
        Subscriber currentUser = getCurrentUser();
        conversationService.markMessagesAsRead(conversationId, currentUser.getIdUser());
        return ResponseEntity.ok().build();
    }
}
