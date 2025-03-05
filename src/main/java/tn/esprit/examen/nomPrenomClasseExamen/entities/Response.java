package tn.esprit.examen.nomPrenomClasseExamen.entities;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level= AccessLevel.PRIVATE)
@Entity
public class Response implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRespons;
    private LocalDateTime dateRespons;
    private String content;
    private String object;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "request_id")
    private Request request;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User sender;



}
