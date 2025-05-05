package tn.esprit.examen.nomPrenomClasseExamen.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "subscriber")
public class Subscriber extends User {

    @Enumerated(EnumType.STRING)
    private Role role;

    private String skills;
    private String nationality;
    private String expertiseArea;
    private String associationRole;
    private String symptoms;

    @Column(nullable = false)
    private boolean healthcareRequest = false;

    @OneToMany(mappedBy = "subscriber", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Healthcare> healthcareRequests;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Healthcare> treatedPatients;

    @OneToMany(mappedBy = "subscriber", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Notification> notifications;

    @OneToMany(cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Animal> animals;

    @ManyToMany(mappedBy = "subscribers", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Donation> donations;

    @ManyToMany(mappedBy = "subscribers", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Training> trainings;

    @ManyToMany(mappedBy = "subscribers", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Forum> forums;

    @OneToMany(mappedBy = "subscriber", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Crisis> crises;

    @OneToOne(mappedBy = "subscriber", cascade = CascadeType.ALL)
    @JsonBackReference
    private Association association;

    // Ce champ est optionnel si déjà défini dans User (hérité)
    // @Column(name = "id_user")
    // private Long idUser;
}
