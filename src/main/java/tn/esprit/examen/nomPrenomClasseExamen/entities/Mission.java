package tn.esprit.examen.nomPrenomClasseExamen.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
//@Table(name = "mission") // Avoid reserved keyword conflict

public class Mission implements Serializable{
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Id

    private Long idMission;
    private String title; // ✅ RECOMMENDED (you forgot Title!)

    private String description;
    private String location;
    private Date startDate;
    private Date endDate;
    private int volunteerCount;
    private MissionStatus status;
    private String missionLogoPath;

    @ManyToMany
    @JoinTable(
            name = "association_missions",
            joinColumns = @JoinColumn(name = "missions_id_mission"),
            inverseJoinColumns = @JoinColumn(name = "association_id_association")
    )
    @JsonIgnore
    private Set<Association> associations = new HashSet<>();



    @ManyToOne
    Crisis crisis;


    @OneToMany(mappedBy = "mission", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<MissionRole> missionRoles = new HashSet<>(); // ✅ also useful
//    @OneToMany(mappedBy = "mission", cascade = CascadeType.ALL)
//    private Set<MissionRole> missionRoles; // ✅ NEW: Link with roles


    @ElementCollection
    @CollectionTable(name = "mission_tags", joinColumns = @JoinColumn(name = "mission_id"))
    @Column(name = "tag")
    List<String> tags;  // You can use Set<String> if you prefer unique tags

    // ➡️ NEW RELATION: PARTICIPATIONS
    @OneToMany(mappedBy = "mission", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Participation> participations;


//    @OneToMany(cascade = CascadeType.ALL, mappedBy="missionsubscriber")
//    private Set<Subscriber> subscribersMission;

}