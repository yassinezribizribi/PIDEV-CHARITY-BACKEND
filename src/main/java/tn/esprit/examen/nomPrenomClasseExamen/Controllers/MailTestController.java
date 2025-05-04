package tn.esprit.examen.nomPrenomClasseExamen.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.examen.nomPrenomClasseExamen.services.MailerSendService;

@RestController
@RequiredArgsConstructor
public class MailTestController {

    private final MailerSendService mailerSendService;

    @GetMapping("/test-email")
    public String sendTestEmail(
            @RequestParam String to,
            @RequestParam String firstName, // Donor's first name
            @RequestParam String donationId // Donation ID

    ) {
        // Creating the HTML email body using String.format
        String htmlBody = """
            <h2>Dear %s,</h2>
            <p>Thank you so much for your generous contribution to our donation campaign! 🙏</p>
            
            <h3>📌 Your Donation Summary</h3>
            <ul>
                <li><b>🆔 Donation ID:</b> %s</li>
           
            </ul>

            <p>⚠️ <b>Reminder:</b> Be available on your scheduled date! If changes are needed, contact us in advance.</p>
            
                <p>🧾 <a href="http://localhost:8089/receipt/%s">Download Your Receipt</a></p>

            <p>For any questions, reply to this email or contact us at support@example.org.</p>

            <p>— The Solidarity & Refugee Team 💙</p>
        """.formatted(
                firstName,
                donationId,

                donationId // Assuming the URL is based on the donation ID
        );

        try {
            mailerSendService.sendEmail(to, "Your Donation Has Been Confirmed – Don’t Miss Your Scheduled Date!", htmlBody);
            return "✅ Email sent successfully to " + to;
        } catch (Exception e) {
            return "❌ Failed to send email: " + e.getMessage();
        }
    }
}
