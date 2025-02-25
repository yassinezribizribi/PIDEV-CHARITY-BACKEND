package tn.esprit.examen.nomPrenomClasseExamen.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Event implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEvent;

    @NotBlank(message = "The event title cannot be blank.")
    @Size(min = 3, max = 100, message = "The event title must be between 3 and 100 characters.")
    private String title;

    @NotBlank(message = "The event description cannot be blank.")
    @Size(min = 5, max = 500, message = "The event description must be between 5 and 500 characters.")
    private String description;

    @NotNull(message = "The event date and time cannot be null.")
    @Future(message = "The event date and time must be in the future.")
    private Date dateTime;

    @NotBlank(message = "The event location cannot be blank.")
    private String location;

    @NotNull(message = "The event type cannot be null.")
    @Enumerated(EnumType.STRING)
    private TypeEvent typeEvent;

    @NotNull(message = "The reservation date cannot be null.")
    @PastOrPresent(message = "The reservation date must be in the past or present.")
    private Date reservationDate;

    @ManyToOne
    @JoinColumn(name = "association_id_association")
    @JsonIgnore
    private Association association;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Notification> notifications;

}
