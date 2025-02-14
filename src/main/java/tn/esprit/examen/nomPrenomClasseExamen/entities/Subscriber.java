package tn.esprit.examen.nomPrenomClasseExamen.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level= AccessLevel.PRIVATE)
@Entity
public class Subscriber extends User {
    private String role;

    @ManyToOne
    private Healthcare healthcare; // "healthcare" au lieu de "healthcares"


    @OneToMany(cascade = CascadeType.ALL)
    private Set<Animal> Animals;



    @ManyToMany(mappedBy="subscribers", cascade = CascadeType.ALL)
    private Set<Donation> donations;

    @ManyToMany(mappedBy="subscribers", cascade = CascadeType.ALL)
    private Set<Training> trainings;

    @ManyToMany(mappedBy="subscribers", cascade = CascadeType.ALL)
    private Set<Forum> forums;






}
