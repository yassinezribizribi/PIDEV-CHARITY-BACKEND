package tn.esprit.examen.nomPrenomClasseExamen.entities;

import java.io.Serializable;

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

public class Forum implements Serializable {
    @Id
    private Long idForum;
    private Date dateCreation;

    @OneToMany(cascade = CascadeType.ALL, mappedBy="forum")
    @JsonIgnore
    private Set<Request> requests;

    @ManyToMany(cascade = CascadeType.ALL)
    private Set<Subscriber> subscribers;

    @ManyToMany(cascade = CascadeType.ALL)
    private Set<JobApplication> jobapplications;
}
