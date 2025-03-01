package tn.esprit.examen.nomPrenomClasseExamen.entities;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRespons;
    private Long idSender;
    private Long idReceiver;
    private Date dateRespons ;
    private String content;
    private String object;

    @ManyToMany
    @JsonIgnore
    @JoinTable(
            name = "response_request",
            joinColumns = @JoinColumn(name = "response_id"),
            inverseJoinColumns = @JoinColumn(name = "request_id")
    )
    private Set<Request> requests;



}
