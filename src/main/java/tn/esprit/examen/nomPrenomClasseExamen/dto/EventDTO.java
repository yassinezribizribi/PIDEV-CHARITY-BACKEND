package tn.esprit.examen.nomPrenomClasseExamen.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import tn.esprit.examen.nomPrenomClasseExamen.entities.TypeEvent;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventDTO {

    private Long idEvent;

    @NotBlank(message = "The title cannot be empty.")
    @Size(min = 3, max = 100, message = "The title must be between 3 and 100 characters.")
    private String title;

    @NotBlank(message = "The description cannot be empty.")
    @Size(min = 10, message = "The description must be at least 10 characters long.")
    private String description;

    @NotNull(message = "The event date and time is required.")
    @Future(message = "The event date must be in the future.")
    private Date dateTime;

    @NotBlank(message = "The location cannot be empty.")
    @Size(min = 3, max = 200, message = "The location must be between 3 and 200 characters.")
    private String location;

    @NotNull(message = "The event type is required.")
    private TypeEvent typeEvent;

    @NotNull(message = "The reservation date is required.")
    @FutureOrPresent(message = "The reservation date must be today or in the future.")
    private Date reservationDate;

    @NotNull(message = "The association ID is required.")
    @Min(value = 1, message = "The association ID must be a positive number.")
    private Long associationId;
}
