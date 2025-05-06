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
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Subscriber extends User {

    @Enumerated(EnumType.STRING)
    Role role;

    @Column(nullable = true)
    String skills;

    @Column(nullable = true)
    String nationality;

    @Column(nullable = true)
    String expertiseArea;

    @Column(nullable = true)
    String associationRole;

    // ✅ Pour le domaine médical
    String symptoms;

    // ✅ CV upload
    String cvFilePath;

    @Column(length = 500)
    String rejectionReason;

    @Enumerated(EnumType.STRING)
    VolunteerStatus validationStatus = VolunteerStatus.NOT_VALIDATED;

    @Column(nullable = false)
    boolean healthcareRequest = false;

    // ✅ Relations

    @OneToMany(mappedBy = "subscriber", cascade = CascadeType.ALL)
    @JsonIgnore
    List<Healthcare> healthcareRequests;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL)
    @JsonIgnore
    List<Healthcare> treatedPatients;

    @ManyToOne
    Healthcare healthcare;

    @OneToMany(mappedBy = "subscriber", cascade = CascadeType.ALL)
    @JsonIgnore
    List<Notification> notifications;

    @OneToMany(cascade = CascadeType.ALL)
    @JsonIgnore
    Set<Animal> animals;

    @ManyToMany(mappedBy = "subscribers", cascade = CascadeType.ALL)
    @JsonIgnore
    Set<Donation> donations;

    @ManyToMany(mappedBy = "subscribers", cascade = CascadeType.ALL)
    @JsonIgnore
    Set<Training> trainings;

    @ManyToMany(mappedBy = "subscribers", cascade = CascadeType.ALL)
    @JsonIgnore
    Set<Forum> forums;

    @OneToMany(mappedBy = "subscriber", cascade = CascadeType.ALL)
    @JsonIgnore
    List<Crisis> crises;

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL)
    @JsonIgnore
    Set<JobOffer> jobOffers;

    @OneToMany(mappedBy = "subscriber", cascade = CascadeType.ALL)
    @JsonIgnore
    Set<Participation> participations;

    @OneToOne(mappedBy = "subscriber", cascade = CascadeType.ALL)
    @JsonBackReference
    Association association;
}
