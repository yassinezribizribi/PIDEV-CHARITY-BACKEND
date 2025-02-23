package tn.esprit.examen.nomPrenomClasseExamen.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TestimonialDTO {

    @NotBlank(message = "Le contenu ne peut pas être vide")
    @Size(min = 10, max = 500, message = "Le contenu doit être entre 10 et 500 caractères")
    private String content;

    @NotBlank(message = "L'URL de la photo avant est requise")
    private String beforePhoto;

    @NotBlank(message = "L'URL de la photo après est requise")
    private String afterPhoto;

    @Size(max = 1000, message = "La description ne doit pas dépasser 1000 caractères")
    private String description;

    @NotNull(message = "L'utilisateur est obligatoire")
    private Long userId;
}
