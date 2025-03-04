package tn.esprit.examen.nomPrenomClasseExamen.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import java.util.Set;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Training implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTraining;

    @NotBlank(message = "The training name cannot be empty.")
    @Size(min = 3, max = 100, message = "The training name must contain between 3 and 100 characters.")
    private String trainingName;

    @NotBlank(message = "The description cannot be empty.")
    @Size(min = 10, message = "The description must contain at least 10 characters.")
    private String description;

    @NotNull(message = "The duration is mandatory.")
    @Min(value = 1, message = "The duration must be at least 1 day.")
    private Integer duration;

    @NotNull(message = "The capacity is mandatory.")
    @Min(value = 1, message = "The capacity must be at least 1 participant.")
    private Integer capacity;

    @NotNull(message = "The level is mandatory.")
    private Level level;

    @NotNull(message = "The type is mandatory.")
    private Type type;

    @NotNull(message = "The session date is mandatory.")
    @Future(message = "The session date must be a future date.")
    private LocalDate sessionDate;

    @ManyToMany(cascade = CascadeType.ALL)

    private Set<Subscriber> subscribers;
}
