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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRequest;
    private Long idSender;
    private Long idReceiver;
    private Date dateRequest;
    private String object;
    private String content;
    private Boolean isUrgent;

    @ManyToMany(cascade = CascadeType.ALL)
    private Set<Response> responses;

    @ManyToOne
    Forum forum;
    public Request(Long idSender, Long idReceiver, Date dateRequest, String object, String content, Boolean isUrgent, Forum forum) {
        this.idSender = idSender;
        this.idReceiver = idReceiver;
        this.dateRequest = dateRequest;
        this.object = object;
        this.content = content;
        this.isUrgent = isUrgent;
        this.forum = forum;
    }


}
