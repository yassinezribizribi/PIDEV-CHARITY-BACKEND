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
public class Request implements Serializable {
    @Id
    private Long idRequest;
    private Long idSender;
    private Long idReceiver;
    private Date DateRequest;
    private String Object;
    private String Content;
    private Boolean isUrgent;

    @ManyToMany(cascade = CascadeType.ALL)
    private Set<Response> responses;

    @ManyToOne
    Forum forum;



}
