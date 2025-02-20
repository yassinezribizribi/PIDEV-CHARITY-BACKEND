package tn.esprit.examen.nomPrenomClasseExamen.entities;
import jakarta.persistence.*;
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
@FieldDefaults(level= AccessLevel.PRIVATE)
@Entity
public class Response implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Permet l'auto-incrémentation

    private Long idRespons;
    private Long idSender;
    private Long idReceiver;
    private Date dateRespons ;
    private String content;
    private String object;

    @ManyToMany(mappedBy="responses", cascade = CascadeType.ALL)
    private Set<Request> requests;



}
