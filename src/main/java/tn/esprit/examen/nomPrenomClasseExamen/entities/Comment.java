package tn.esprit.examen.nomPrenomClasseExamen.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class Comment {
    @Id
    @GeneratedValue
    private int idComment;
    private String DescriptionComment;
    private int rating;
    private LocalDateTime date=LocalDateTime.now();
    @ManyToOne
    User user;

    @ManyToOne
    @JsonIgnore
    Posts post;



}
