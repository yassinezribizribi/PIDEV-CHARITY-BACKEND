package tn.esprit.examen.nomPrenomClasseExamen.entities;

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
    private Association association;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<Animal> animals;

    @ManyToMany(mappedBy="subscribers", cascade = CascadeType.ALL)
    private Set<Donation> donations;

    @ManyToMany(mappedBy="subscribers", cascade = CascadeType.ALL)
    private Set<Training> trainings;

    @ManyToMany(mappedBy = "subscribers")
    private Set<Forum> forums;

    // New field to track job offers created by the subscriber
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL)
    private Set<JobOffer> jobOffers; // This field will track the job offers created by this subscriber
}
