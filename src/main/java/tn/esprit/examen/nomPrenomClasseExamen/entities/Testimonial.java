package tn.esprit.examen.nomPrenomClasseExamen.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;


import java.io.Serializable;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Testimonial implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTestimonial;

    @NotBlank(message = "Le contenu ne peut pas être vide")
    @Size(min = 10, max = 500, message = "Le contenu doit être entre 10 et 500 caractères")
    private String content;

    @NotBlank(message = "L'URL de la photo avant est requise")
    private String beforePhoto;

    @NotBlank(message = "L'URL de la photo après est requise")
    private String afterPhoto;

    @Size(max = 1000, message = "La description ne doit pas dépasser 1000 caractères")
    private String description;

    // ✅ Relation avec User (un testimonial appartient à un utilisateur)
    @NotNull(message = "L'utilisateur est obligatoire")
    @ManyToOne
    @JoinColumn(name = "id_user", nullable = false)
    private User user;
}
