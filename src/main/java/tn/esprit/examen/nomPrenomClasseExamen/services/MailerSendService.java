package tn.esprit.examen.nomPrenomClasseExamen.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.*;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BarcodeQRCode;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import okhttp3.RequestBody;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.DonsRepository;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Association;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Dons;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BarcodeQRCode;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MailerSendService {
    private final DonsRepository donsRepository;

    @Value("${mailersend.api.key}")
    private String apiKey;

    private static final String API_URL = "https://api.mailersend.com/v1/email";

    public void sendEmail(String to, String subject, String htmlContent) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        Map<String, Object> emailData = new HashMap<>();
        emailData.put("from", Map.of("email", "no-reply@test-y7zpl988w0545vx6.mlsender.net", "name", "Mohamed Missaoui"));
        emailData.put("to", List.of(Map.of("email", to))); // ✅ Fix here
        emailData.put("subject", subject);
        emailData.put("html", htmlContent != null ? htmlContent : "<p>No content</p>"); // ✅ Safe fallback

        String json = mapper.writeValueAsString(emailData);

        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(json, MediaType.get("application/json"));

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();

        Response response = client.newCall(request).execute();
        System.out.println("📧 MailerSend response: " + response.code());
        System.out.println("📧 MailerSend body: " + response.body().string());
    }

    public byte[] generateReceiptPdf(Long idDons) throws DocumentException, IOException {
        Optional<Dons> optionalDons = donsRepository.findById(idDons);
        if (optionalDons.isEmpty()) {
            throw new NoSuchElementException("Donation not found");
        }

        Dons dons = optionalDons.get();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, byteArrayOutputStream);

        document.addTitle("Donation Receipt #" + dons.getIdDons());
        document.addCreator("Solidarity and Refugee");

        document.open();

        buildPdfContent(document, dons);

        document.close();
        return byteArrayOutputStream.toByteArray();
    }

    private void buildPdfContent(Document document, Dons dons) throws DocumentException, IOException {
        // Colors
        BaseColor primaryColor = new BaseColor(0, 102, 102);
        BaseColor secondaryColor = new BaseColor(245, 245, 245);
        BaseColor accentColor = new BaseColor(255, 153, 51);

        // Fonts
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, primaryColor);
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 14, BaseColor.DARK_GRAY);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 11, BaseColor.DARK_GRAY);
        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, BaseColor.GRAY);

        // Get association info
        Association association = dons.getDonation().getAssociationDonation();
        String associationName = association.getAssociationName();
        String logoPath = association.getAssociationLogoPath();

        // FIX: Fetch email from Subscriber -> User
        String associationEmail = (association.getSubscriber() != null && association.getSubscriber().getEmail() != null)
                ? association.getSubscriber().getEmail()
                : "Email not available";



        // Add Logo
        if (logoPath != null && !logoPath.isEmpty()) {
            try {
                Image logo = Image.getInstance(logoPath);
                logo.scaleToFit(100, 50); // Adjust size
                logo.setAlignment(Image.ALIGN_LEFT);
                document.add(logo);
            } catch (Exception e) {
                System.out.println("Logo loading failed: " + e.getMessage());
            }
        }

        // Header section
        Paragraph header = new Paragraph("DONATION RECEIPT", titleFont);
        header.setAlignment(Element.ALIGN_CENTER);
        header.setSpacingAfter(5f);
        document.add(header);

        Paragraph subHeader = new Paragraph(associationName, subtitleFont);
        subHeader.setAlignment(Element.ALIGN_CENTER);
        subHeader.setSpacingAfter(2f);
        document.add(subHeader);

        Paragraph emailHeader = new Paragraph(associationEmail, subtitleFont);
        emailHeader.setAlignment(Element.ALIGN_CENTER);
        emailHeader.setSpacingAfter(20f);
        document.add(emailHeader);

        // Add receipt number and date
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(90);
        infoTable.setHorizontalAlignment(Element.ALIGN_CENTER);

        addInfoRow(infoTable, "Receipt #", "DN-" + dons.getIdDons(), headerFont, valueFont);
        addInfoRow(infoTable, "Date Issued", new SimpleDateFormat("MMMM dd, yyyy").format(new Date()), headerFont, valueFont);

        document.add(infoTable);
        document.add(Chunk.NEWLINE);

        // Donation Details
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(90);
        table.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(20f);
        table.setWidths(new float[]{30, 70});

        addStyledRow(table, "Donor Name", dons.getNomDoneur() + " " + dons.getPrenomDoneur(), headerFont, valueFont, secondaryColor);
        addStyledRow(table, "Email", dons.getDonorEmail(), headerFont, valueFont, BaseColor.WHITE);
        addStyledRow(table, "Donation ID", "DN-" + dons.getIdDons(), headerFont, valueFont, secondaryColor);
        addStyledRow(table, "Quantity", String.valueOf(dons.getQuantite()), headerFont, valueFont, BaseColor.WHITE);
        addStyledRow(table, "Scheduled Date", formatDate(dons.getScheduledDateTime()), headerFont, valueFont, secondaryColor);
        addStyledRow(table, "Delivery Method", capitalize(dons.getDeliveryMethod().toString()), headerFont, valueFont, BaseColor.WHITE);
        addStyledRow(table, "Notes", (dons.getDonorNote() != null ? dons.getDonorNote() : "N/A"), headerFont, valueFont, secondaryColor);

        document.add(table);

        // QR Code section
        Paragraph qrTitle = new Paragraph("Scan for donation verification",
                FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.GRAY));
        qrTitle.setAlignment(Element.ALIGN_CENTER);
        document.add(qrTitle);

        String qrContent = "{\"donationId\":" + dons.getIdDons() + ",\"org\":\"" + associationName + "\"}";
        BarcodeQRCode qrCode = new BarcodeQRCode(qrContent, 300, 300, null);
        Image qrImage = qrCode.getImage();
        qrImage.scaleAbsolute(150, 150);
        qrImage.setAlignment(Element.ALIGN_CENTER);
        document.add(qrImage);

        document.add(Chunk.NEWLINE);

        // Thank you message
        Paragraph thankYou = new Paragraph("Thank You For Your Generosity!",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, primaryColor));
        thankYou.setAlignment(Element.ALIGN_CENTER);
        thankYou.setSpacingAfter(10f);
        document.add(thankYou);

        Paragraph appreciation = new Paragraph(
                "Your donation makes a meaningful difference in our mission. " +
                        "We greatly appreciate your support and generosity.",
                FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.DARK_GRAY));
        appreciation.setAlignment(Element.ALIGN_CENTER);
        document.add(appreciation);

        // Footer
        Paragraph footer = new Paragraph(
                associationName + " | " + associationEmail,
                footerFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(20f);
        document.add(footer);
    }


    private void addStyledRow(PdfPTable table, String label, String value,
                              Font labelFont, Font valueFont, BaseColor bgColor) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBackgroundColor(bgColor);
        labelCell.setPadding(8);
        labelCell.setBorderColor(BaseColor.WHITE);
        labelCell.setBorderWidth(1.5f);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBackgroundColor(bgColor);
        valueCell.setPadding(8);
        valueCell.setBorderColor(BaseColor.WHITE);
        valueCell.setBorderWidth(1.5f);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addInfoRow(PdfPTable table, String label, String value,
                            Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    // Helper method to format date
    private String formatDate(Temporal dateTime) {
        if (dateTime instanceof LocalDateTime) {
            return DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a").format((LocalDateTime)dateTime);
        } else if (dateTime instanceof LocalDate) {
            return DateTimeFormatter.ofPattern("MMMM dd, yyyy").format((LocalDate)dateTime);
        }
        return dateTime.toString();
    }

    // Helper method to capitalize strings
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
