package tn.esprit.examen.nomPrenomClasseExamen.entities;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Association;

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
    @Enumerated(EnumType.STRING) // au lieu de ORDINAL (valeur par défaut)
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

    @ManyToOne
    @JoinColumn(name = "association_id")
    private Association assignedAssociation;

    public Association getAssociation() {
        return assignedAssociation;  // Retourne la bonne association
    }
    public void setAssociation(Association association) {
        this.assignedAssociation = association;  // Assigne la bonne association
    }
    public void setAssignedAssociation(Association association) {
        this.assignedAssociation = association;}


    @OneToMany(cascade = CascadeType.ALL, mappedBy="crisis")
    private Set<Mission> missions;


    @ManyToOne
    @JoinColumn(name = "subscriber_id")
    private Subscriber subscriber;  // L'utilisateur qui a signalé la crise





}
