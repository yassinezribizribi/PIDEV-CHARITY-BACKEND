package tn.esprit.examen.nomPrenomClasseExamen.entities;

import jakarta.persistence.*;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

@Entity
public class DemandeAnimal {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Animal animal;

    private boolean accepted = false;

    private LocalDateTime dateDemande = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public DemandeAnimal setId(Long id) {
        this.id = id;
        return this;
    }

    public User getUser() {
        return user;
    }

    public DemandeAnimal setUser(User user) {
        this.user = user;
        return this;
    }

    public Animal getAnimal() {
        return animal;
    }

    public DemandeAnimal setAnimal(Animal animal) {
        this.animal = animal;
        return this;
    }

    public boolean getAccepted() {
        return accepted;
    }

    public DemandeAnimal setAccepted(boolean accepted) {
        this.accepted = accepted;
        return this;
    }

    public LocalDateTime getDateDemande() {
        return dateDemande;
    }

    public DemandeAnimal setDateDemande(LocalDateTime dateDemande) {
        this.dateDemande = dateDemande;
        return this;
    }
}
