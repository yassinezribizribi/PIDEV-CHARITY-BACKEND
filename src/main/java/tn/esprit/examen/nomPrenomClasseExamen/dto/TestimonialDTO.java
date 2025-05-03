package tn.esprit.examen.nomPrenomClasseExamen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestimonialDTO {

    private Long id;
    private String content;
    private String description;
    private Long userId;
    private Long likeCount;

    private boolean likedByCurrentUser;

    // Champs images existants (avant/après)
    private String beforePhotoBase64;
    private String afterPhotoBase64;
    private String beforePhotoPath;
    private String afterPhotoPath;
    private String dominantEmotion;
    private String summary;
    private String suggestedResponse;
    private List<String> recommendedResources;

    // ⚡ Nouveaux champs pour audio/vidéo
    private String mediaType;            // "audio" ou "video"
    private String mediaPath;            // Chemin du fichier audio/video stocké
    private String TranscriptionText;    // Texte transcrit automatiquement par IA
}
