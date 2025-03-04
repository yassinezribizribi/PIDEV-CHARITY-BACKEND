package tn.esprit.examen.nomPrenomClasseExamen.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.Set;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level= AccessLevel.PRIVATE)
@Entity
public class Testimonial implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTestimonial;
    private String content;
    private String beforePhoto ;
    private String afterPhoto;
    private String description;

    @OneToMany(cascade = CascadeType.ALL, mappedBy="testimonial")
    private Set<PostAction> postActions;


}
