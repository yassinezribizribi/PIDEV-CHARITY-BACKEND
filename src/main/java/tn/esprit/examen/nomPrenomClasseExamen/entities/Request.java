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
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level= AccessLevel.PRIVATE)
@Entity
public class Request implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRequest;

    private Date dateRequest;
    private String object;
    private String content;
    private Boolean isUrgent;

    @OneToMany(mappedBy = "request",cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Response> responses;

    @ManyToOne
            @JsonIgnore
    Forum forum;
    public Request(Date dateRequest, String object, String content, Boolean isUrgent, Forum forum) {

        this.dateRequest = dateRequest;
        this.object = object;
        this.content = content;
        this.isUrgent = isUrgent;
        this.forum = forum;
    }


}
