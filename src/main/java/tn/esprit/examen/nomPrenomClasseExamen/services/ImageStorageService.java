package tn.esprit.examen.nomPrenomClasseExamen.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

@Service
public class ImageStorageService {

    // La méthode qui convertit l'image en Base64
    public String encodeImageToBase64(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("Le fichier est vide");
            }

            // Convertir l'image en tableau d'octets
            byte[] bytes = file.getBytes();

            // Convertir le tableau d'octets en une chaîne Base64
            return Base64.getEncoder().encodeToString(bytes);

        } catch (IOException e) {
            throw new RuntimeException("Impossible de convertir l'image en Base64", e);
        }
    }
}
