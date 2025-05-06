package tn.esprit.examen.nomPrenomClasseExamen.Controllers;
import com.itextpdf.text.*;

import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.BarcodeQRCode;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.DonsRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Utlis.ValidationUtils;
import tn.esprit.examen.nomPrenomClasseExamen.dto.DonationDto;
import tn.esprit.examen.nomPrenomClasseExamen.dto.DonsDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Dons;
import tn.esprit.examen.nomPrenomClasseExamen.entities.DonsDeliveryMethod;
import tn.esprit.examen.nomPrenomClasseExamen.services.DonsServices;
import tn.esprit.examen.nomPrenomClasseExamen.services.MailerSendService;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.io.ByteArrayOutputStream;
import java.util.List;


@RestController
@RequestMapping("/api/dons")
@CrossOrigin(origins = "http://localhost:4200")
@AllArgsConstructor
public class DonsController {
    DonsServices donsServices;
    private final DonsRepository donsRepository;
    private final MailerSendService mailerSendService;

    private static final Logger logger = LoggerFactory.getLogger(DonsController.class);


    // ✅ Subscriber contributes to a donation
    @PostMapping("/{idDonation}/contribute")
    public ResponseEntity<?> contributeToDonation(@PathVariable Long idDonation,
                                                  @Valid @RequestBody DonsDTO donsDTO,
                                                  BindingResult bindingResult,
                                                  @RequestHeader(name = "Authorization", required = false) String authHeader) {

        // 🔥 Inject the idDonation into the dto so the validator can use it
        donsDTO.setDonationId(idDonation);

        // ⛔ Don't move this down — we need the id before validating
        ResponseEntity<?> errorResponse = ValidationUtils.handleValidationErrors(bindingResult);
        if (errorResponse != null) return errorResponse;

        String jwtToken = (authHeader != null && authHeader.startsWith("Bearer ")) ? authHeader.substring(7) : null;
        DonsDTO savedDonsDTO = donsServices.contributeToDonation(idDonation, donsDTO, jwtToken);

        return ResponseEntity.ok(savedDonsDTO);
    }
    @GetMapping("/receipt/{idDons}")
    public ResponseEntity<byte[]> getReceiptPdf(@PathVariable Long idDons) {
        try {
            byte[] pdf = mailerSendService.generateReceiptPdf(idDons);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, "application/pdf");
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=receipt-" + idDons + ".pdf");

            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("❌ Receipt not found".getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Error generating receipt PDF".getBytes());
        }
    }


    //    private void addCellToTable(PdfPTable table, String text, Font font, BaseColor backgroundColor) {
//        PdfPCell cell = new PdfPCell(new Phrase(text, font));
//        cell.setBackgroundColor(backgroundColor);
//        cell.setPadding(5);
//        table.addCell(cell);
//    }
//
//    private void addRowToTable(PdfPTable table, String fieldName, String value, Font font, BaseColor backgroundColor) {
//        addCellToTable(table, fieldName, font, backgroundColor);
//        addCellToTable(table, value, font, backgroundColor);
//    }
//
//    // 🧩 Helper method to make the table clean
//    private void addRowToTable(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
//        PdfPCell cell1 = new PdfPCell(new Phrase(label, labelFont));
//        PdfPCell cell2 = new PdfPCell(new Phrase(value, valueFont));
//        cell1.setBackgroundColor(BaseColor.LIGHT_GRAY);
//        cell1.setPadding(5);
//        cell2.setPadding(5);
//        table.addCell(cell1);
//        table.addCell(cell2);
//    }
    @GetMapping("/donation/validate/{idDons}")
    public ResponseEntity<Map<String, String>> validateDonationByQrCode(@PathVariable Long idDons) {
        Map<String, String> response = new HashMap<>();
        try {
            donsServices.validateDonsByAssociation(idDons);
            response.put("message", "✅ Donation successfully validated!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "❌ Validation failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/{idDons}/validate")
    public ResponseEntity<DonsDTO> validateDonsByAssociation(
            @PathVariable Long idDons
    ) {
        DonsDTO validatedDons = donsServices.validateDonsByAssociation(idDons);
        return ResponseEntity.ok(validatedDons);
    }
    @GetMapping("/donations/{donationId}/dons")
    public ResponseEntity<List<DonsDTO>> getDonsByDonation(@PathVariable Long donationId) {
        List<DonsDTO> dons = donsServices.getDonsByDonationId(donationId);
        return ResponseEntity.ok(dons);
    }

    @GetMapping("/{donsId}")
    public ResponseEntity<DonsDTO> getDonsById(@PathVariable Long donsId) {
        return ResponseEntity.ok(donsServices.getDonsById(donsId));
    }

    @GetMapping("/by-association")
    public ResponseEntity<Page<DonsDTO>> getDonsByAssociation(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            String jwtToken = authHeader.replace("Bearer ", "");
            Page<DonsDTO> donsPage = donsServices.getDonsByAssociationFromToken(jwtToken, page, size);
            return ResponseEntity.ok(donsPage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Page.empty());
        }
    }

    @GetMapping("/user-dons")
    public ResponseEntity<Page<DonsDTO>> getDonsByAuthenticatedUser(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            String jwtToken = authHeader.replace("Bearer ", "");
            Page<DonsDTO> donsPage = donsServices.getDonsByAuthenticatedUser(jwtToken, page, size);
            return ResponseEntity.ok(donsPage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Page.empty());
        }
    }
    @PutMapping("/updateDons/{donsId}")
    public ResponseEntity<DonsDTO> updateDons(
            @PathVariable Long donsId,
            @RequestBody @Valid DonsDTO donsDTO,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String jwtToken = authHeader.replace("Bearer ", "");
            DonsDTO updatedDons = donsServices.updateDons(donsId, donsDTO, jwtToken);
            return ResponseEntity.ok(updatedDons);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
    @DeleteMapping("/deleteDons/{donsId}")
    public ResponseEntity<Void> deleteDons(@PathVariable Long donsId) {
        try {
            donsServices.deleteDons(donsId);
            return ResponseEntity.noContent().build(); // 204 No Content
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404 if not found
        }
    }
    @GetMapping("/getDonsForUserById/{donsId}")
    public ResponseEntity<DonsDTO> getDonsForUserById(
            @PathVariable Long donsId,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            String token = authHeader.replace("Bearer ", ""); // 🧼 Clean the Bearer prefix
            DonsDTO donsDTO = donsServices.getDonsForUserById(donsId, token);
            return ResponseEntity.ok(donsDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
    }
    @PostMapping("/donation/{id}/reminder")
    public ResponseEntity<Void> scheduleDonationReminder(@PathVariable Long id) {
        Dons donation = donsRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid donation ID"));
        donsServices.scheduleReminder(donation);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/update-dons/{donationId}")
    public ResponseEntity<String> manageDonation(@PathVariable Long donationId, @RequestParam String token) {
        // Step 1: Validate the token
        if (!donsServices.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("The link has expired or is invalid.");
        }

        // Step 2: Extract the donation ID from the token (optional if you already have donationId from the URL)
        long extractedDonationId = donsServices.extractDonationIdFromToken(token);

        if (extractedDonationId != donationId) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Donation ID mismatch.");
        }

        // Step 3: Retrieve the donation details from the database using the donation ID
        Dons donation = donsRepository.findById(donationId)
                .orElseThrow(() -> new IllegalArgumentException("Donation not found"));

        // Step 4: Allow the user to manage the donation (e.g., redirect to a form or service for updates)
        // Here, we can return a response, but usually you would render a page or redirect to a frontend view for donation management.
        return ResponseEntity.ok("Donation Management Page for Donation ID: " + donation.getIdDons());
    }

    // Optionally, add a POST method for updating the donation after the token validation process:
//    @PostMapping("/update-dons/nonauth/{donationId}")
//    public ResponseEntity<String> updateDonation(@PathVariable Long donationId, @RequestParam String token, @RequestBody Dons updatedDonation) {
//        // Validate token as before
//        if (!donsServices.validateToken(token)) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("The link has expired or is invalid.");
//        }
//
//        // Ensure donation ID from token matches the URL
//        long extractedDonationId = donsServices.extractDonationIdFromToken(token);
//        // Log the extracted donation ID and the donation ID from URL for debugging
//        System.out.println("Token Extracted Donation ID: " + extractedDonationId);
//        System.out.println("URL Donation ID: " + donationId);
//
//        if (extractedDonationId != donationId) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Donation ID mismatch.");
//        }
//
//        // Retrieve and update donation
//        Dons donation = donsRepository.findById(donationId)
//                .orElseThrow(() -> new IllegalArgumentException("Donation not found"));
//
//        // Update the donation with the provided updated information
//        donation.setScheduledDateTime(updatedDonation.getScheduledDateTime());
//        // Update other fields as necessary
//
//        donsRepository.save(donation);
//
//        return ResponseEntity.ok("Donation updated successfully!");
//    }

    @PostMapping("/update-dons/nonauth/{donationId}")
    public ResponseEntity<String> updateDonation(
            @PathVariable Long donationId,
            @RequestParam String token,
            @RequestBody Dons updatedDonation) {

        // Validate token
        if (!donsServices.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("The link has expired or is invalid.");
        }

        // Retrieve the existing donation
        Dons existingDonation = donsRepository.findById(donationId)
                .orElseThrow(() -> new IllegalArgumentException("Donation not found"));

        // Update fields: scheduled date/time, quantity, delivery method
        existingDonation.setScheduledDateTime(updatedDonation.getScheduledDateTime());
        existingDonation.setQuantite(updatedDonation.getQuantite());
        existingDonation.setDeliveryMethod(updatedDonation.getDeliveryMethod());

        // Handle donor address and note based on delivery method
        if (updatedDonation.getDeliveryMethod() == DonsDeliveryMethod.PICK_UP) {
            if (updatedDonation.getDonorAddress() == null || updatedDonation.getDonorAddress().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Address is required for PICK_UP delivery method.");
            }
            existingDonation.setDonorAddress(updatedDonation.getDonorAddress());
            existingDonation.setDonorNote(updatedDonation.getDonorNote()); // optional note
        } else {
            existingDonation.setDonorAddress(null);
            existingDonation.setDonorNote(null);
        }

        // Save changes
        donsRepository.save(existingDonation);

        return ResponseEntity.ok("Donation updated successfully!");
    }

    // Manually trigger the background cleaner
    @PostMapping("/triggerDailyCleanup")
    public ResponseEntity<String> triggerDailyCleanup() {
        try {
            donsServices.dailyBackgroundCleaner();  // Invoke the background cleaner manually
            return ResponseEntity.ok("Daily cleanup process triggered successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error occurred while triggering cleanup: " + e.getMessage());
        }
    }

    // Endpoint to manually trigger the email to a donor
    @PostMapping("/sendEmailToDonor")
    public ResponseEntity<String> sendEmailToDonor(@RequestParam String donationId) {
        try {
            Dons donation = donsServices.findDonationById(Long.parseLong(donationId));
            if (donation == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Donation not found.");
            }

            // Create the email body for cancellation notification
            String emailBody = donsServices.createCancellationEmailBody(donation);
            mailerSendService.sendEmail(donation.getDonorEmail(), "Donation Cancellation Notice", emailBody);

            return ResponseEntity.ok("Email sent to donor successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error sending email: " + e.getMessage());
        }
    }
}
