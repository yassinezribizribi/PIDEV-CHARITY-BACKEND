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
public class Posts implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Permet l'auto-incrémentation

    private Long idPosts;
    private String content;
    private Date creationDate;
    private int likesCount;
    private int shareCount;

    @OneToMany(cascade = CascadeType.ALL, mappedBy="posts")
    private Set<PostAction> postActions;

    @ManyToOne
    Subscription subscription;






}
