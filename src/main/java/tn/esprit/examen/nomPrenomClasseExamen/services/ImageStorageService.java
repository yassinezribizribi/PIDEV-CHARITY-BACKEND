package tn.esprit.examen.nomPrenomClasseExamen.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class ImageStorageService {

    private final Path rootLocation = Paths.get("src/main/resources/static/images");

    public String storeImage(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("Le fichier est vide");
            }

            // Crée le dossier s’il n’existe pas
            if (!Files.exists(rootLocation)) {
                Files.createDirectories(rootLocation);
            }

            // Génère un nom unique pour l’image
            String originalExtension = getFileExtension(file.getOriginalFilename());
            String uniqueFileName = UUID.randomUUID().toString() + "." + originalExtension;

            Path destinationFile = rootLocation.resolve(uniqueFileName).normalize().toAbsolutePath();

            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);

            // Retourne le chemin relatif de l’image
            return "/images/" + uniqueFileName;
        } catch (IOException e) {
            throw new RuntimeException("Impossible de stocker l'image", e);
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        } else {
            return "jpg"; // par défaut
        }
    }
}
