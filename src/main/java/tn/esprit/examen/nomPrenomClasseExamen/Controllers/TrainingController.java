package tn.esprit.examen.nomPrenomClasseExamen.Controllers;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.AffineTransform;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.canvas.draw.ILineDrawer;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.UserRepository;
import tn.esprit.examen.nomPrenomClasseExamen.dto.TrainingDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Training;
import tn.esprit.examen.nomPrenomClasseExamen.entities.User;
import tn.esprit.examen.nomPrenomClasseExamen.services.ITrainingServices;
import tn.esprit.examen.nomPrenomClasseExamen.services.UserServices;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/trainings")
public class
TrainingController {

    @Autowired
    private ITrainingServices trainingServices;
    @Autowired
    private UserRepository userServices;
    // 🟠 Ajouter une formation
    @PostMapping("/add")
    public ResponseEntity<Training> createTraining(@Valid @RequestBody TrainingDTO trainingDTO) {
        try {
            Training training = trainingServices.addTraining(trainingDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(training);
        } catch (IllegalArgumentException e) {
            log.error("Erreur lors de l'ajout de la formation : {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // 🟠 Mettre à jour une formation
    @PutMapping("/{id}")
    public ResponseEntity<Training> updateTraining(@Valid @PathVariable Long id, @RequestBody Training updatedTraining) {
        try {
            Training training = trainingServices.updateTraining(id, updatedTraining);
            return ResponseEntity.ok(training);
        } catch (RuntimeException e) {
            log.error("Erreur lors de la mise à jour de la formation : {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // 🟠 Supprimer une formation
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTraining(@Valid @PathVariable Long id) {
        try {
            trainingServices.deleteTraining(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Erreur lors de la suppression de la formation : {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // 🟠 Obtenir une formation par son ID
    @GetMapping("/{id}")
    public ResponseEntity<Training> getTrainingById(@Valid @PathVariable Long id) {
        Optional<Training> training = trainingServices.getTrainingById(id);
        return training.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 🟠 Obtenir toutes les formations
    @GetMapping
    public ResponseEntity<List<Training>> getAllTrainings() {
        List<Training> trainings = trainingServices.getAllTrainings();
        return ResponseEntity.ok(trainings);
    }

    // 🟠 Obtenir les formations par abonné
    @GetMapping("/subscriber/{subscriberId}")
    public ResponseEntity<List<Training>> getTrainingsBySubscriber(@Valid @PathVariable Long subscriberId) {
        List<Training> trainings = trainingServices.getTrainingsBySubscriber(subscriberId);
        return ResponseEntity.ok(trainings);
    }
    @GetMapping("/generate/{trainingId}/{userId}")
    public ResponseEntity<byte[]> generateCertificate(@PathVariable Long trainingId, @PathVariable Long userId) throws IOException {
        Optional<Training> trainingOpt = trainingServices.getTrainingById(trainingId);
        User user = userServices.findById(userId).orElse(null);

        if (trainingOpt.isEmpty() || user == null) {
            return ResponseEntity.notFound().build();
        }

        Training training = trainingOpt.get();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        document.setMargins(50, 50, 50, 50);
        pdf.addNewPage();

        // Cadre double (bleu marine et doré)
        PdfCanvas canvas = new PdfCanvas(pdf.getFirstPage());
        canvas.setLineWidth(2)
                .setStrokeColor(new DeviceRgb(0, 51, 102)) // Bleu marine
                .rectangle(36, 36, pdf.getDefaultPageSize().getWidth() - 72, pdf.getDefaultPageSize().getHeight() - 72)
                .stroke();
        canvas.setLineWidth(1)
                .setStrokeColor(new DeviceRgb(191, 144, 0)) // Doré
                .rectangle(41, 41, pdf.getDefaultPageSize().getWidth() - 82, pdf.getDefaultPageSize().getHeight() - 82)
                .stroke();

        // Bandeau supérieur (bleu marine)
        canvas.setFillColor(new DeviceRgb(0, 51, 102))
                .rectangle(36, pdf.getDefaultPageSize().getHeight() - 86, pdf.getDefaultPageSize().getWidth() - 72, 50)
                .fill();

        // Symbole élégant (icône de diplôme) avec marge supérieure pour éviter le décalage
        document.add(new Paragraph("🎓")
                .setFontSize(40)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(new DeviceRgb(191, 144, 0))
                .setMarginTop(20) // Ajout d'une marge supérieure pour descendre le symbole
                .setMarginBottom(10));

        // Titre doré avec une police serif
        PdfFont titleFont = PdfFontFactory.createFont(StandardFonts.TIMES_BOLD);
        document.add(new Paragraph("CERTIFICAT DE PARTICIPATION")
                .setFont(titleFont)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(26)
                .setBold()
                .setFontColor(new DeviceRgb(191, 144, 0))
                .setMarginTop(10)); // Marge supplémentaire pour espacer du symbole

        // Ligne décorative sous le titre
        canvas.setLineWidth(0.5f)
                .setStrokeColor(new DeviceRgb(191, 144, 0))
                .moveTo(150, pdf.getDefaultPageSize().getHeight() - 180) // Ajustement de la position pour suivre le titre
                .lineTo(pdf.getDefaultPageSize().getWidth() - 150, pdf.getDefaultPageSize().getHeight() - 180)
                .stroke();

        // Texte d'intro
        document.add(new Paragraph("\nCe certificat est remis à")
                .setFont(PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN))
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(14)
                .setFontColor(ColorConstants.BLACK));

        // Nom du récipiendaire
        document.add(new Paragraph(user.getFirstName().toUpperCase() + " " + user.getLastName().toUpperCase())
                .setFont(PdfFontFactory.createFont(StandardFonts.TIMES_BOLD))
                .setTextAlignment(TextAlignment.CENTER)
                .setBold()
                .setFontSize(28)
                .setFontColor(new DeviceRgb(0, 51, 102))
                .setMarginBottom(10));

        // Description de participation
        document.add(new Paragraph("pour ses contributions à la formation :\n")
                .setFont(PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN))
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(14)
                .setFontColor(ColorConstants.BLACK));

        // Nom de la formation
        document.add(new Paragraph(training.getTrainingName())
                .setFont(PdfFontFactory.createFont(StandardFonts.TIMES_ITALIC))
                .setTextAlignment(TextAlignment.CENTER)
                .setItalic()
                .setFontSize(16)
                .setFontColor(new DeviceRgb(0, 51, 102))
                .setMarginBottom(30));

        // Ligne décorative après la formation
        canvas.setLineWidth(0.5f)
                .setStrokeColor(new DeviceRgb(191, 144, 0))
                .moveTo(150, pdf.getDefaultPageSize().getHeight() - 300)
                .lineTo(pdf.getDefaultPageSize().getWidth() - 150, pdf.getDefaultPageSize().getHeight() - 300)
                .stroke();

        // Détails de la formation (encadré fond gris clair avec bordure dorée)
        Paragraph trainingDetails = new Paragraph()
                .add("📅 Date de la session : " + training.getSessionDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "\n")
                .add("📚 Durée : " + training.getDuration() + " jours\n")
                .add("🎓 Niveau : " + training.getLevel() + "\n")
                .add("🔖 Type : " + training.getType() + "\n\n")
                .add("📄 Description de la formation :\n" + training.getDescription())
                .setFont(PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN))
                .setFontSize(11)
                .setTextAlignment(TextAlignment.LEFT)
                .setBackgroundColor(new DeviceRgb(245, 245, 245)) // Gris très clair
                .setBorder(new SolidBorder(new DeviceRgb(191, 144, 0), 1))
                .setPadding(10)
                .setMarginLeft(50)
                .setMarginRight(50)
                .setMarginBottom(30);
        document.add(trainingDetails);

        // Date d’émission
        String dateNow = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        document.add(new Paragraph("Fait le : " + dateNow)
                .setFont(PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN))
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(12)
                .setFontColor(ColorConstants.BLACK)
                .setMarginBottom(50));

        // Signatures
        Table signTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .useAllAvailableWidth();
        signTable.addCell(new Cell()
                .add(new Paragraph("Signature").setFont(PdfFontFactory.createFont(StandardFonts.TIMES_BOLD)).setBold())
                .add(new Paragraph("_________________________"))
                .setTextAlignment(TextAlignment.CENTER)
                .setBorder(Border.NO_BORDER));
        signTable.addCell(new Cell()
                .add(new Paragraph("Responsable de formation").setFont(PdfFontFactory.createFont(StandardFonts.TIMES_BOLD)).setBold())
                .add(new Paragraph("_________________________"))
                .setTextAlignment(TextAlignment.CENTER)
                .setBorder(Border.NO_BORDER));
        document.add(signTable);

        // QR Code (agrandi et centré)
        String certId = "CERT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String qrText =
                "Formation : " + training.getTrainingName() + "\n" +
                        "Date : " + dateNow + "\n" +
                        "Nom : " + user.getFirstName() + " " + user.getLastName() + "\n" +
                        "Email : " + user.getEmail() + "\n" +
                        "ID : " + certId;

        ByteArrayOutputStream qrOutput = new ByteArrayOutputStream();
        try {
            BitMatrix matrix = new MultiFormatWriter().encode(qrText, BarcodeFormat.QR_CODE, 120, 120); // Taille augmentée à 120x120
            MatrixToImageWriter.writeToStream(matrix, "PNG", qrOutput);
        } catch (WriterException e) {
            throw new RuntimeException("Erreur lors de la génération du QR Code", e);
        }

        Image qrCode = new Image(ImageDataFactory.create(qrOutput.toByteArray()));
        float pageWidth = pdf.getDefaultPageSize().getWidth(); // 595 pour A4
        float qrWidth = 120; // Taille du QR code en points
        float xPosition = (pageWidth - qrWidth) / 2; // Calcul pour centrer horizontalement
        qrCode.setFixedPosition(xPosition, 80); // Position centrée horizontalement, 80 points du bas
        document.add(qrCode);

        // Filigrane
        PdfFont watermarkFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        canvas.saveState();
        canvas.setFillColor(new DeviceRgb(200, 200, 200));
        canvas.beginText()
                .setFontAndSize(watermarkFont, 50)
                .setTextMatrix(AffineTransform.getRotateInstance(Math.toRadians(45), 300, 400))
                .moveText(300, 400)
                .showText("SoliRefuge")
                .endText();
        canvas.restoreState();

        document.close();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "certificat_" + user.getIdUser() + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(baos.toByteArray());
    }

    // 🟠 Ajouter un abonné à une formation
    @PostMapping("/{trainingId}/add-subscriber/{userId}")
    public ResponseEntity<Training> addSubscriberToTraining(@PathVariable Long trainingId, @PathVariable Long userId) {
        // Appel du service pour ajouter un abonné (réfugié) à la formation
        Training training = trainingServices.addSubscriberToTraining(trainingId, userId);
        return ResponseEntity.ok(training); // Retourne le training mis à jour
    }
}

