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


import java.io.File;


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

            helper.setSubject("Event Reminder: " + event.getTitle());

            String eventDateTime = event.getDateTime().toString();
            String htmlContent = "<html><body style='font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, sans-serif; background-color: #f4f7fa; color: #333; margin: 0; padding: 0;'>"
                    + "<div style='max-width: 640px; margin: 40px auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1); border: 1px solid #e0e4e8;'>"
                    + "<div style='background: linear-gradient(135deg, #4a90e2, #50c9c3); padding: 20px; text-align: center;'>"

                    + "<img src='https://img.freepik.com/premium-vector/logo-hand-hand-charity-with-heart-middle_933533-4.jpg' alt='Logo' style='max-width: 120px; height: auto; margin-bottom: 16px; display: block; margin-left: auto; margin-right: auto;'>"
                    + "<h2 style='color: #ffffff; font-size: 24px; margin: 0; font-weight: 600;'>Event Reminder</h2>"
                    + "</div>"
                    + "<div style='padding: 24px;'>"
                    + "<p style='font-size: 16px; line-height: 1.6; margin: 0 0 16px;'>Hello Mr/Ms " + subscriber.getFirstName() + " " + subscriber.getLastName() + ",</p>"
                    + "<p style='font-size: 16px; line-height: 1.6; margin: 0 0 16px;'>We would like to remind you that the event <strong>" + event.getTitle() + "</strong> will take place in 2 days, on <strong>" + eventDateTime + "</strong>.</p>"
                    + "<p style='font-size: 16px; line-height: 1.6; margin: 0 0 16px;'>Here is the event description:</p>"
                    + "<p style='font-size: 16px; line-height: 1.6; color: #4a5568; background-color: #f7fafc; padding: 16px; border-left: 4px solid #4a90e2; margin: 0 0 24px;'>" + event.getDescription() + "</p>"
                    + "<div style='text-align: center; margin: 24px 0;'>"
                    + "</div>"
                    + "<p style='font-size: 14px; color: #718096; margin: 0;'>See you soon!</p>"

                    + "</div>"
                    + "<footer style='background-color: #edf2f7; padding: 16px; text-align: center; font-size: 12px; color: #a0aec0;'>"
                    + "<p style='margin: 0;'>© 2025 Solidarity&Refugee</p>"
                    + "</footer>"
                    + "</div>"
                    + "</body></html>";
            helper.setText(htmlContent, true);
            javaMailSender.send(message);

            logger.info("Event reminder sent to: " + subscriber.getEmail());

        } catch (MessagingException e) {
            logger.error("Error sending reminder email: " + e.getMessage(), e);
        }
    }

    public void sendAdoptionConfirmationEmail(User user, Animal animal) {
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true); // true for multipart
            helper.setTo(user.getEmail());
            helper.setSubject("✅ Adoption Confirmed - " + animal.getName());

            // Enhanced HTML for a more beautiful design
            String htmlContent = "<html>" +
                    "<body style='font-family: Arial, sans-serif; background-color: #f4f4f9; margin: 0; padding: 0;'>" +
                    "<div style='max-width: 600px; margin: 30px auto; background-color: #ffffff; border-radius: 10px; box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1); overflow: hidden;'>" +
                    "<div style='background-color: #4CAF50; padding: 20px; text-align: center;'>" +
                    "<h2 style='color: #ffffff; margin: 0; font-size: 24px;'>Adoption Confirmed 🎉</h2>" +
                    "</div>" +
                    "<div style='padding: 30px 20px; text-align: center;'>" +
                    "<h3 style='color: #1F2937; font-size: 20px; margin: 0 0 15px;'>Hello " + user.getFirstName() + ",</h3>" +
                    "<p style='color: #4B5563; font-size: 16px; line-height: 1.6; margin: 0 0 20px;'>Your adoption request for <strong>" + animal.getName() + "</strong> has been approved!</p>" +
                    "<p style='color: #4B5563; font-size: 16px; line-height: 1.6; margin: 0 0 20px;'>Thank you for opening your heart and home to this wonderful animal 💙</p>" +
                    "<div style='margin: 20px 0;'>" +
                    "<img src='cid:animalImage' style='max-width: 250px; height: auto; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);' alt='" + animal.getName() + "' />" +
                    "</div>" +
                    "<a href='http://localhost:4200/animals' style='display: inline-block; padding: 12px 24px; background-color: #3B82F6; color: #ffffff; text-decoration: none; font-size: 16px; font-weight: 500; border-radius: 6px; margin: 20px 0;'>Check Your Adoption</a>" +
                    "</div>" +
                    "<div style='background-color: #F9FAFB; padding: 20px; text-align: center; border-top: 1px solid #E5E7EB;'>" +
                    "<p style='color: #6B7280; font-size: 14px; margin: 0;'>🐾 The Solidarity & Refugee Animal Team</p>" +
                    "</div>" +
                    "</div>" +
                    "<div style='text-align: center; padding: 20px 0;'>" +
                    "<p style='color: #9CA3AF; font-size: 12px; margin: 0;'>This email was sent automatically. Please do not reply directly.</p>" +
                    "</div>" +
                    "</body>" +
                    "</html>";

            helper.setText(htmlContent, true);

            // Attach the image if imagePath exists
            if (animal.getImagePath() != null && !animal.getImagePath().isEmpty()) {
                String basePath = "D:\\COURS\\4eme\\PI\\NH\\AMV2\\Photos need\\";

                // Remove the UUID prefix from the image path
                String imageFileName = animal.getImagePath();
                // Assuming the UUID is always 36 characters (32 chars + 4 hyphens) followed by an underscore
                if (imageFileName.contains("_")) {
                    imageFileName = imageFileName.substring(imageFileName.indexOf("_") + 1);
                }
                String fullImagePath = basePath + imageFileName;
                File imageFile = new File(fullImagePath);

                if (imageFile.exists() && imageFile.canRead()) {
                    System.out.println("Attaching image: " + fullImagePath);
                    helper.addInline("animalImage", imageFile);
                } else {
                    System.err.println("Image file not found or not readable: " + fullImagePath);
                }
            } else {
                System.err.println("Image path is null or empty for animal: " + animal.getName());
            }

            javaMailSender.send(message);
            System.out.println("Email sent successfully to: " + user.getEmail());

        } catch (MessagingException e) {
            System.err.println("Error sending email: " + e.getMessage());
            e.printStackTrace();
        }
    }


}
