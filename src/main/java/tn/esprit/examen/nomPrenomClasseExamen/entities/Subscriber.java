package tn.esprit.examen.nomPrenomClasseExamen.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Set;
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level= AccessLevel.PRIVATE)
@Entity
public class Subscriber extends User {

    @Enumerated(EnumType.STRING) // Stocke le rôle en tant que texte dans la base de données
    private Role role; // Change String -> Role (Enum)

    // Role-specific fields (nullable for roles that don't use them)
    @Column(nullable = true)
    private String skills;          // For VOLUNTEER
    @Column(nullable = true)
    private String nationality;     // For REFUGEE
    @Column(nullable = true)
    private String expertiseArea;   // For MENTOR
    @Column(nullable = true)
    private String associationRole; // For ASSOCIATION_MEMBER

    @ManyToOne
    private Healthcare healthcare; // "healthcare" au lieu de "healthcares"


    @OneToMany(cascade = CascadeType.ALL)
    private Set<Animal> animals;



    @ManyToMany(mappedBy="subscribers", cascade = CascadeType.ALL)
    private Set<Donation> donations;

    @ManyToMany(mappedBy="subscribers", cascade = CascadeType.ALL)
    private Set<Training> trainings;

    @ManyToMany(mappedBy="subscribers", cascade = CascadeType.ALL)
    private Set<Forum> forums;

    @OneToMany(mappedBy = "subscriber")
    @JsonIgnore
    private List<Crisis> crises; // Liste des crises signalées par ce subscriber






}
