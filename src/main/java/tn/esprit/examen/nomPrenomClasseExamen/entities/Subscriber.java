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

    @JsonIgnore
    @OneToMany(mappedBy = "subscriber", cascade = CascadeType.ALL)
    private List<Healthcare> healthcareRequests;

    @JsonIgnore
    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL)
    private List<Healthcare> treatedPatients;

    @OneToMany(mappedBy = "subscriber", cascade = CascadeType.ALL)
    private List<Notification> notifications;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<Animal> animals;

    @ManyToMany(mappedBy="subscribers", cascade = CascadeType.ALL)
    private Set<Donation> donations;

    @ManyToMany(mappedBy="subscribers", cascade = CascadeType.ALL)
    private Set<Training> trainings;

    @ManyToMany(mappedBy="subscribers", cascade = CascadeType.ALL)
    private Set<Forum> forums;

    @OneToMany(mappedBy = "subscriber")
    private List<Crisis> crises;

    @Column(name = "id_user")
    private Long idUser;
}


