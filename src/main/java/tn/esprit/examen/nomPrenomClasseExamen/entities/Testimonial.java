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
    private Long idTestimonial;
    private String Content;
    private String BeforePhoto ;
    private String AfterPhoto;
    private String Description;

    @OneToMany(cascade = CascadeType.ALL, mappedBy="testimonial")
    private Set<PostAction> PostActions;


}
