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
public class Posts implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPosts;
    private String content;
    private Date creationDate;


    @OneToMany(cascade = CascadeType.ALL, mappedBy="posts")
    @JsonIgnore
    private Set<PostAction> postActions;

    @ManyToOne
    @JsonIgnore
    Subscription subscription;


}
