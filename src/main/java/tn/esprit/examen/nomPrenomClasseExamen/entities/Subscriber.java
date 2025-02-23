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
public class Subscriber extends User { // Inherits from User
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = true)
    private String skills; // For VOLUNTEER
    @Column(nullable = true)
    private String nationality; // For REFUGEE
    @Column(nullable = true)
    private String expertiseArea; // For MENTOR
    @Column(nullable = true)
    private String associationRole; // For ASSOCIATION_MEMBER

    @ManyToOne
    private Healthcare healthcare;

    @OneToMany(mappedBy = "subscriber" , cascade = CascadeType.PERSIST) // Cascade only persist and merge
    @JsonIgnore
    private Set<Animal> animals;

    @ManyToMany(mappedBy = "subscribers", cascade = CascadeType.PERSIST)
    private Set<Donation> donations;

    @ManyToMany(mappedBy = "subscribers", cascade = CascadeType.PERSIST)
    private Set<Training> trainings;

    @ManyToMany(mappedBy = "subscribers", cascade = CascadeType.PERSIST)
    private Set<Forum> forums;

    @OneToMany(mappedBy = "subscriber", cascade = CascadeType.PERSIST)
    private List<Crisis> crises;
}
