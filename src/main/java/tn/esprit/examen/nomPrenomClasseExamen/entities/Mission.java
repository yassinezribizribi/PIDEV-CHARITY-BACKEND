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
    @Id
    private Long idMission;
    private String Description;
    private String Location;
    private Date startDate;
    private Date endDate;
    private int VolunteerCount;
    private MissionStatus Status;

    @ManyToMany(mappedBy="missions", cascade = CascadeType.ALL)
    private Set<Association> associations;

    @ManyToOne
    Crisis crisis;
}
