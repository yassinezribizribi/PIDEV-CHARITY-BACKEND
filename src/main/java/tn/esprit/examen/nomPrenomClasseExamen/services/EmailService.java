package tn.esprit.examen.nomPrenomClasseExamen.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tn.esprit.examen.nomPrenomClasseExamen.entities.User;

@Service
public class EmailService {

    private final JavaMailSender javaMailSender;



    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        try {
            javaMailSender.send(message);
            logger.info("Email envoyé avec succès à : " + to);
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de l'email : " + e.getMessage(), e);
        }
    }

    public void sendReminderEmail(User subscriber, Event event) {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        try {
            helper.setTo(subscriber.getEmail());
            helper.setSubject("Rappel d'événement : " + event.getTitle());
            String eventDateTime = event.getDateTime().toString();
            String htmlContent = "<html><body style='font-family: Arial, sans-serif; background-color: #f9f9f9; color: #333;'>"
                    + "<div style='max-width: 600px; margin: 0 auto; padding: 20px; background-color: #ffffff; border-radius: 8px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);'>"
                    + "<h2 style='color: #4CAF50;'>Rappel d'événement</h2>"
                    + "<p style='font-size: 16px;'>Bonjour Mr/Mme " + subscriber.getFirstName() + " " + subscriber.getLastName() + ",</p>"
                    + "<p style='font-size: 16px;'>Nous vous rappelons que l'événement <strong>" + event.getTitle() + "</strong> aura lieu dans 2 jours, le <strong>" + eventDateTime + "</strong>.</p>"
                    + "<p style='font-size: 16px;'>Voici la description de l'événement :</p>"
                    + "<p style='font-size: 16px; color: #555;'>" + event.getDescription() + "</p>"
                    + "<div style='text-align: center; margin-top: 30px;'>"

                    + "</div>"
                    + "<p style='font-size: 14px; margin-top: 30px; color: #888;'>À bientôt !</p>"
                    + "<footer style='font-size: 12px; color: #aaa; text-align: center; margin-top: 40px;'>"
                    + "<p>&copy; 2025 Votre Site Web</p>"
                    + "</footer>"
                    + "</div>"
                    + "</body></html>";
            helper.setText(htmlContent, true);
            javaMailSender.send(message);
            logger.info("Rappel d'événement envoyé à : " + subscriber.getEmail());

        } catch (MessagingException e) {
            logger.error("Erreur lors de l'envoi du rappel d'email : " + e.getMessage(), e);
        }
    }
}
