package tn.esprit.examen.nomPrenomClasseExamen.entities;
import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @ManyToOne
    @JsonIgnore // Prevent recursion here
    Association associationMission;

    @ManyToOne
    Crisis crisis;

//    @OneToMany(cascade = CascadeType.ALL, mappedBy="missionsubscriber")
//    private Set<Subscriber> subscribersMission;

}
