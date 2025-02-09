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
    private Long idPosts;
    private String Content;
    private Date CreationDate;
    private int LikesCount;
    private int ShareCount;

    @OneToMany(cascade = CascadeType.ALL, mappedBy="posts")
    private Set<PostAction> PostActions;

    @ManyToOne
    Subscription subscription;






}
