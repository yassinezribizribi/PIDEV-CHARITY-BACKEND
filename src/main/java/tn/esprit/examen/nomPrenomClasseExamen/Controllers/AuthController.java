package tn.esprit.examen.nomPrenomClasseExamen.Controllers;
import jakarta.validation.Valid;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.SubscriberRepository;
import tn.esprit.examen.nomPrenomClasseExamen.dto.JwtResponseDto;
import tn.esprit.examen.nomPrenomClasseExamen.dto.LoginRequest;
import tn.esprit.examen.nomPrenomClasseExamen.dto.MessageResponseDto;
import tn.esprit.examen.nomPrenomClasseExamen.dto.SignupRequestDto;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Role;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Subscriber;
import tn.esprit.examen.nomPrenomClasseExamen.jwt.JwtUtils;
import tn.esprit.examen.nomPrenomClasseExamen.services.SubDetailsImpl;
import tn.esprit.examen.nomPrenomClasseExamen.services.EmailService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.validation.annotation.Validated;


import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private SubscriberRepository subscriberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private EmailService emailService;

    @PostMapping("/admin/login")
    public ResponseEntity<?> adminLogin(@RequestBody LoginRequest loginRequest) {
        return authenticateUser(loginRequest);
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        System.out.println("🔍 Tentative de connexion...");

        if (loginRequest.getEmail()== null||loginRequest.getEmail().isEmpty() ) {
            System.out.println("❌ loginRequest est NULL !");
            return ResponseEntity.badRequest().body("INVALID REQUEST");
        }

        System.out.println("📩 Email reçu : " + loginRequest.getEmail());
        System.out.println("🔑 Mot de passe reçu : " + loginRequest.getPassword());

        if (loginRequest.getEmail() == null || loginRequest.getEmail().isEmpty()) {
            System.out.println("❌ Email est NULL ou vide !");
            return ResponseEntity.badRequest().body("EMAIL IS NULL OR EMPTY");
        }

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );
        } catch (Exception e) {
            System.out.println("❌ ERREUR : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("INVALID CREDENTIALS");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        SubDetailsImpl userDetails = (SubDetailsImpl) authentication.getPrincipal();

        // 🔹 Récupérer les rôles sous forme de List<String>
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponseDto(jwt, userDetails.getId(), roles));
    }


    // 🔹 REGISTER (SIGNUP)
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody @Valid SignupRequestDto signUpRequest) {
        log.info("📩 Tentative d'inscription avec l'email : {}", signUpRequest.getEmail());

        if (subscriberRepository.existsByEmail(signUpRequest.getEmail())) {
            log.warn("❌ Email déjà utilisé !");
            return ResponseEntity.badRequest().body(new MessageResponseDto("Error: Email is already in use!"));
        }

        try {
            Role role = Role.fromString(signUpRequest.getRole().name());
            Subscriber subscriber = new Subscriber();
            subscriber.setEmail(signUpRequest.getEmail());
            subscriber.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
            subscriber.setFirstName(signUpRequest.getFirstName());
            subscriber.setLastName(signUpRequest.getLastName());
            subscriber.setRole(role);
            subscriber.setJob(signUpRequest.getJob());
            subscriber.setTelephone(signUpRequest.getTelephone());
            subscriber.setLocation(signUpRequest.getLocation());

            subscriberRepository.save(subscriber);
            sendWelcomeEmail(subscriber, signUpRequest.getPassword());

            log.info("✅ Inscription réussie pour : {}", signUpRequest.getEmail());
            return ResponseEntity.ok(new MessageResponseDto("User registered successfully!"));

        } catch (Exception e) {
            log.error("❌ Erreur lors de l'inscription : {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponseDto("Error: " + e.getMessage()));
        }
    }

    // 🔹 DELETE USER
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        log.info("🗑 Tentative de suppression du compte ID: {}", id);

        Optional<Subscriber> subscriber = subscriberRepository.findById(id);
        if (subscriber.isEmpty()) {
            log.warn("❌ Utilisateur non trouvé !");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponseDto("User not found!"));
        }

        subscriberRepository.deleteById(id);
        log.info("✅ Utilisateur supprimé avec succès !");
        return ResponseEntity.ok(new MessageResponseDto("User deleted successfully!"));
    }
    // 🔹 GET USER BY ID
    @GetMapping("/user/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        log.info("🔍 Fetching user details for ID: {}", id);

        Optional<Subscriber> subscriberOpt = subscriberRepository.findById(id);
        if (subscriberOpt.isEmpty()) {
            log.warn("❌ User not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponseDto("User not found!"));
        }

        Subscriber subscriber = subscriberOpt.get();
        return ResponseEntity.ok(subscriber);
    }


    // 🔹 UPDATE USER
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody SignupRequestDto updateRequest) {
        log.info("✏ Tentative de mise à jour du compte ID: {}", id);

        Optional<Subscriber> subscriberOpt = subscriberRepository.findById(id);
        if (subscriberOpt.isEmpty()) {
            log.warn("❌ Utilisateur non trouvé !");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponseDto("User not found!"));
        }

        try {
            Subscriber subscriber = subscriberOpt.get();
            subscriber.setFirstName(updateRequest.getFirstName());
            subscriber.setLastName(updateRequest.getLastName());
            subscriber.setJob(updateRequest.getJob());
            subscriber.setTelephone(updateRequest.getTelephone());

            // Si l'utilisateur met à jour son mot de passe
            if (updateRequest.getPassword() != null && !updateRequest.getPassword().isEmpty()) {
                subscriber.setPassword(passwordEncoder.encode(updateRequest.getPassword()));
            }

            subscriberRepository.save(subscriber);
            log.info("✅ Mise à jour réussie pour l'utilisateur ID: {}", id);
            return ResponseEntity.ok(new MessageResponseDto("User updated successfully!"));

        } catch (Exception e) {
            log.error("❌ Erreur lors de la mise à jour : {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponseDto("Error: " + e.getMessage()));
        }
    }

    // 🔹 ENVOYER UN EMAIL DE BIENVENUE
    private void sendWelcomeEmail(Subscriber subscriber, String plainPassword) {
        String subject = "Welcome to Our Platform";
        String body = String.format(
                "Dear %s,\n\nYour account has been created successfully.\n\n" +
                        "Login credentials:\nEmail: %s\nPassword: %s\n\nBest regards,\nThe Team",
                subscriber.getFirstName(),
                subscriber.getEmail(),
                plainPassword
        );

        try {
            emailService.sendEmail(subscriber.getEmail(), subject, body);
        } catch (Exception e) {
            log.warn("⚠️ Email non envoyé : {}", e.getMessage());
        }
    }


    // 🔹 GET USER BY EMAIL
    @GetMapping("/user/email/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        log.info("🔍 Fetching user details for email: {}", email);

        Optional<Subscriber> subscriberOpt = subscriberRepository.findByEmail(email);
        if (subscriberOpt.isEmpty()) {
            log.warn("❌ User not found with email: {}", email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponseDto("User not found!"));
        }

        // Remove these misplaced lines
        // emailService.sendEmail(subscriber.getEmail(), subject, body);

        return ResponseEntity.ok(subscriberOpt.get());
    }


    // 🔹 GET ALL USERS
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        log.info("🔍 Fetching all users");

        try {
            List<Subscriber> users = subscriberRepository.findAll();
            log.info("✅ Successfully retrieved {} users", users.size());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("❌ Error fetching users: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponseDto("Error fetching users: " + e.getMessage()));
        }
    }

    @PostMapping("/upload-profile-image")
    public ResponseEntity<?> uploadProfileImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId) {
        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Please select a file to upload");
            }

            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body("Only image files are allowed");
            }

            // Validate file size (2MB max)
            if (file.getSize() > 2 * 1024 * 1024) {
                return ResponseEntity.badRequest().body("File size should not exceed 2MB");
            }

            // Get user
            Subscriber subscriber = subscriberRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ?
                    originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String filename = "profile_" + userId + "_" + System.currentTimeMillis() + extension;

            // Create uploads directory if it doesn't exist
            // Use absolute path and create directories recursively
            String uploadDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "profile-images";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (!created) {
                    throw new RuntimeException("Failed to create upload directory");
                }
            }

            // Save file
            String filePath = uploadDir + File.separator + filename;
            File dest = new File(filePath);
            file.transferTo(dest);

            // Update user's profile image path - use relative path for storage
            String relativePath = "/uploads/profile-images/" + filename;
            subscriber.setProfileImage(relativePath);
            subscriberRepository.save(subscriber);

            // Return the new profile image URL
            Map<String, String> response = new HashMap<>();
            response.put("profileImage", relativePath);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error uploading profile image: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload profile image: " + e.getMessage());
        }
    }

    @GetMapping("/profile-image/{filename:.+}")
    public ResponseEntity<Resource> getProfileImage(@PathVariable String filename) {
        try {
            String uploadDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "profile-images";
            Path filePath = Paths.get(uploadDir, filename);
            Resource resource = new FileSystemResource(filePath.toFile());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG) // or MediaType.IMAGE_PNG
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

}

