package tn.esprit.examen.nomPrenomClasseExamen.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Animal;
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
//Event email
    public void sendReminderEmail(User subscriber, Event event) {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        try {
            helper.setTo(subscriber.getEmail());
            helper.setSubject("Rappel d'événement : " + event.getTitle());
            String eventDateTime = event.getDateTime().toString();
            String htmlContent = "<html><body style='font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, sans-serif; background-color: #f4f7fa; color: #333; margin: 0; padding: 0;'>"
                    + "<div style='max-width: 640px; margin: 40px auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1); border: 1px solid #e0e4e8;'>"
                    + "<div style='background: linear-gradient(135deg, #4a90e2, #50c9c3); padding: 20px; text-align: center;'>"
                    + "<img src='https://cdn.vectorstock.com/i/1000v/26/21/charity-logo-design-template-vector-21462621.jpg' alt='Logo' style='max-width: 120px; height: auto; margin-bottom: 16px; display: block; margin-left: auto; margin-right: auto;'>"
                    + "<h2 style='color: #ffffff; font-size: 24px; margin: 0; font-weight: 600;'>Rappel d'événement</h2>"
                    + "</div>"
                    + "<div style='padding: 24px;'>"
                    + "<p style='font-size: 16px; line-height: 1.6; margin: 0 0 16px;'>Bonjour Mr/Mme " + subscriber.getFirstName() + " " + subscriber.getLastName() + ",</p>"
                    + "<p style='font-size: 16px; line-height: 1.6; margin: 0 0 16px;'>Nous vous rappelons que l'événement <strong>" + event.getTitle() + "</strong> aura lieu dans 2 jours, le <strong>" + eventDateTime + "</strong>.</p>"
                    + "<p style='font-size: 16px; line-height: 1.6; margin: 0 0 16px;'>Voici la description de l'événement :</p>"
                    + "<p style='font-size: 16px; line-height: 1.6; color: #4a5568; background-color: #f7fafc; padding: 16px; border-left: 4px solid #4a90e2; margin: 0 0 24px;'>" + event.getDescription() + "</p>"
                    + "<div style='text-align: center; margin: 24px 0;'>"
                    + "</div>"
                    + "<p style='font-size: 14px; color: #718096; margin: 0;'>À bientôt !</p>"
                    + "</div>"
                    + "<footer style='background-color: #edf2f7; padding: 16px; text-align: center; font-size: 12px; color: #a0aec0;'>"
                    + "<p style='margin: 0;'>© 2025 Solidarity&Refugee</p>"
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
//adoption email
    public void sendAdoptionConfirmationEmail(User user, Animal animal) {
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(user.getEmail());
            helper.setSubject("✅ Adoption acceptée - " + animal.getName());

            String htmlContent = "<html>" +
                    "<body style='font-family: Arial, sans-serif; background-color: #f4f4f9; margin: 0; padding: 0;'>" +
                    "<div style='max-width: 600px; margin: 30px auto; background-color: #ffffff; border-radius: 10px; box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1); overflow: hidden;'>" +
                    "<div style='background-color: #4CAF50; padding: 20px; text-align: center;'>" +
                    "<h2 style='color: #ffffff; margin: 0; font-size: 24px;'>Adoption Confirmée 🎉</h2>" +
                    "</div>" +
                    "<div style='padding: 30px;'>" +
                    "<h3 style='color: #333333; font-size: 20px; margin-top: 0;'>Bonjour " + user.getFirstName() + ",</h3>" +
                    "<p style='color: #555555; font-size: 16px; line-height: 1.6;'>Votre demande d'adoption pour l'animal <strong>" + animal.getName() + "</strong> a été acceptée !</p>" +
                    "<p style='color: #555555; font-size: 16px; line-height: 1.6;'>Merci pour votre engagement et votre amour des animaux 💙</p>" +
                    "<br>" +
                    "<div style='text-align: center; padding: 15px; border-top: 1px solid #e0e0e0;'>" +
                    "<p style='color: #777777; font-size: 14px; margin: 0;'>🐾 L’équipe animaux de Solidarity&Refuge</p>" +
                    "</div>" +
                    "</div>" +
                    "</div>" +
                    "<div style='text-align: center; padding: 10px;'>" +
                    "<p style='color: #999999; font-size: 12px;'>Cet email a été envoyé automatiquement. Merci de ne pas y répondre directement.</p>" +
                    "</div>" +
                    "</body>" +
                    "</html>";

            helper.setText(htmlContent, true);
            javaMailSender.send(message);

        } catch (MessagingException e) {
            System.err.println("Erreur envoi mail : " + e.getMessage());
        }
    }


}
