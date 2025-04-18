package tn.esprit.examen.nomPrenomClasseExamen.entities;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level= AccessLevel.PRIVATE)
@Entity
public class Crisis implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long idCrisis;
    private Categorie categorie;
    private String location;
    private String updates;
    private String description;
    @Temporal(TemporalType.TIMESTAMP)
    private Date crisisDate;

    private Double latitude;
    private Double longitude;
    private String image;


    @Enumerated(EnumType.STRING)
    private CrisisSeverity severity;

    @Enumerated(EnumType.STRING)
    private CrisisStatus status;

    @OneToMany(cascade = CascadeType.ALL, mappedBy="crisis")
    private Set<Mission> missions;


    @ManyToOne
    @JoinColumn(name = "subscriber_id")
    private Subscriber subscriber;  // L'utilisateur qui a signalé la crise


}
