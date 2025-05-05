package tn.esprit.examen.nomPrenomClasseExamen.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity

public class Mission implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long idMission;

    String description;
    String location;
    Date startDate;
    Date endDate;
    int volunteerCount;
    MissionStatus status;

    @ManyToMany(mappedBy = "missions", cascade = CascadeType.ALL)
    Set<Association> associations;

    @ManyToOne
    @JoinColumn(name = "crisis_id") // Explicitly define the foreign key column
    Crisis crisis;

    @ElementCollection
    @CollectionTable(name = "mission_tags", joinColumns = @JoinColumn(name = "mission_id"))
    @Column(name = "tag")
    List<String> tags;
}