package tn.esprit.examen.nomPrenomClasseExamen.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Subscriber extends User {

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = true)
    private String skills;
    @Column(nullable = true)
    private String nationality;
    @Column(nullable = true)
    private String expertiseArea;
    @Column(nullable = true)
    private String associationRole;

    @ManyToOne
    private Healthcare healthcare;

    @OneToOne(mappedBy = "subscriber", cascade = CascadeType.ALL)
    @JsonBackReference
    private Association association;

    @OneToMany(cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Animal> animals;

//    @ManyToMany(mappedBy="subscribers", cascade = CascadeType.ALL)
//    @JsonIgnore

//    private Set<Donation> donations;

    @ManyToMany(mappedBy="subscribers", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Training> trainings;

    @ManyToMany(mappedBy = "subscribers")
    @JsonIgnore
    private Set<Forum> forums;

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<JobOffer> jobOffers;
}
