package tn.esprit.examen.nomPrenomClasseExamen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestimonialDTO {
    private Long id;
    private String content;
    private String description;
    private Long userId;

    private String beforePhotoBase64;
    private String afterPhotoBase64;  // 🔥 Assure-toi que ce champ existe

    private String beforePhotoPath;
    private String afterPhotoPath;
}
