package tn.esprit.examen.nomPrenomClasseExamen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
@Getter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestimonialDTO {

    private Long id;
    private String content;
    private String description;
    private Long userId;
    private int likeCount;
    private int unlikeCount;

    private boolean likedByCurrentUser;

    // 📸 Champs pour les photos
    private String beforePhotoBase64;
    private String afterPhotoBase64;
    private String beforePhotoPath;
    private String afterPhotoPath;

    // 🎭 Résultats émotionnels
    // "tristesse", "joie", etc.
    private int distressScore;                  // Score de détresse émotionnelle
    private String suggestedResponse;           // Message IA
    private List<String> resources;             // URLs de soutien

    // 🔊 Champs média audio/vidéo
    private String mediaType;                   // "audio", "video"
    private String mediaPath;                   // URL vers le fichier media


}
