package tn.esprit.examen.nomPrenomClasseExamen.Controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
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


import java.util.List;
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

    // 🔹 LOGIN (AUTHENTICATION)
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        System.out.println("🔍 Tentative de connexion...");

        if (loginRequest == null) {
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
    public ResponseEntity<?> registerUser(@RequestBody SignupRequestDto signUpRequest) {
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
                plainPassword);

        if (subscriber.getEmail() == null || subscriber.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email is null or empty");
        }

        emailService.sendEmail(subscriber.getEmail(), subject, body);
    }
}