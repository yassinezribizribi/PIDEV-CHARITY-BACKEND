package tn.esprit.examen.nomPrenomClasseExamen.services;

// ✅ Assure-toi d’avoir SEULEMENT ça
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Healthcare;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;

@Service
public class PdfService {

    public byte[] generatePdfFromHealthcare(Healthcare healthcare) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Fonts
            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Font labelFont = new Font(Font.HELVETICA, 12, Font.BOLD, Color.DARK_GRAY);
            Font valueFont = new Font(Font.HELVETICA, 12);

            // Logo (facultatif)
            try {
                Image logo = Image.getInstance("src/main/resources/static/logo.png");
                logo.scaleToFit(80, 80);
                logo.setAlignment(Image.ALIGN_RIGHT);
                document.add(logo);
            } catch (Exception e) {
                // Ignorer si pas de logo
            }

            // Titre
            Paragraph title = new Paragraph("📋 Rapport Médical Personnel", titleFont);
            title.setAlignment(Paragraph.ALIGN_CENTER);  // ✅ Utilise Paragraph
            title.setSpacingAfter(20f);
            document.add(title);

            // Table
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(20f);
            table.setSpacingAfter(20f);
            table.setWidths(new float[]{2, 5});

            addRow(table, "📅 Date de rendez-vous :",
                    healthcare.getBookingDate() != null ? sdf.format(healthcare.getBookingDate()) : "Non définie",
                    labelFont, valueFont);

            addRow(table, "🩺 Plan de traitement :", healthcare.getTreatmentPlan(), labelFont, valueFont);
            addRow(table, "⚠️ Maladie terminale :", healthcare.getTerminalDisease(), labelFont, valueFont);
            addRow(table, "🔗 Lien visio :", healthcare.getMeetingUrl(), labelFont, valueFont);
            addRow(table, "📌 Statut :", healthcare.getStatus() != null ? healthcare.getStatus().toString() : "Inconnu",
                    labelFont, valueFont);

            document.add(table);

            Paragraph footer = new Paragraph("🕊️ Merci de prendre soin de votre santé.", valueFont);
            footer.setAlignment(Paragraph.ALIGN_CENTER);  // ✅ Aussi ici
            document.add(footer);

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }

    private void addRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "Non défini", valueFont));

        labelCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setBorder(Rectangle.NO_BORDER);

        labelCell.setPaddingBottom(8f);
        valueCell.setPaddingBottom(8f);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }
}
