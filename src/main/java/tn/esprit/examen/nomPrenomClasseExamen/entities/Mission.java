package tn.esprit.examen.nomPrenomClasseExamen.entities;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level= AccessLevel.PRIVATE)
@Entity

public class Mission implements Serializable{
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Id
    private Long idMission;
    private String description;
    private String location;
    private Date startDate;
    private Date endDate;
    private int volunteerCount;
    private MissionStatus status;

    @ManyToMany(mappedBy="missions", cascade = CascadeType.ALL)
    private Set<Association> associations;

    @ManyToOne
    Crisis crisis;

//    @OneToMany(cascade = CascadeType.ALL, mappedBy="missionsubscriber")
//    private Set<Subscriber> subscribersMission;

}
