package tn.esprit.examen.nomPrenomClasseExamen.services;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.esprit.examen.nomPrenomClasseExamen.entities.User;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.UserRepository;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {  // 🔥 Implémentation ajoutée

    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, JavaMailSender mailSender, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
    }



    public String forgotPassword(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return "User not found";
        }

        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        userRepository.save(user);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Password Reset Request");
        message.setText("Cliquez sur le lien suivant pour réinitialiser votre mot de passe :\n"
                + "http://localhost:4200/reset-password?token=" + resetToken);

        mailSender.send(message);

        // ✅ Retourner le resetToken dans la réponse pour être utilisé par le front
        return resetToken;
    }


    public String resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetToken(token);
        if (user == null) {
            return "Invalid token";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        userRepository.save(user);

        return "Password successfully reset";
    }
}
