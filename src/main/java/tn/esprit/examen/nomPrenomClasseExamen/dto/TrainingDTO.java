package tn.esprit.examen.nomPrenomClasseExamen.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Level;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Type;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TrainingDTO {

    private Long idTraining;

    @NotBlank(message = "The training name cannot be empty.")
    @Size(min = 3, max = 100, message = "The training name must be between 3 and 100 characters.")
    private String trainingName;

    @NotBlank(message = "The description cannot be empty.")
    @Size(min = 10, message = "The description must be at least 10 characters long.")
    private String description;

    @NotNull(message = "The duration is required.")
    @Min(value = 1, message = "The duration must be at least 1 day.")
    private Integer duration;

    @NotNull(message = "The capacity is required.")
    @Min(value = 1, message = "The capacity must be at least 1 participant.")
    private Integer capacity;

    @NotNull(message = "The level is required.")
    private Level level;

    @NotNull(message = "The type is required.")
    private Type type;

    @NotNull(message = "The session date is required.")
    @Future(message = "The session date must be in the future.")
    private Date sessionDate;
}
