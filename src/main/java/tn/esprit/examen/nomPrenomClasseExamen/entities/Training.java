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
public class Training implements Serializable {
    @Id
    private Long idTraining;
    private String trainingName;
    private String description;
    private Integer duration;
    private Integer capacity;
    private Level level;
    private Type type;
    private Date sessionDate;

    @ManyToMany(cascade = CascadeType.ALL)
    private Set<Subscriber> subscribers;





}
