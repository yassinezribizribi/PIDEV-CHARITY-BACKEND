package tn.esprit.examen.nomPrenomClasseExamen.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import java.util.Set;

public class Subscriber extends User {
    private Role role;

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
