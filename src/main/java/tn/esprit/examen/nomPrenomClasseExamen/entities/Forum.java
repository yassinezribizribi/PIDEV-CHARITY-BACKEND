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
public class Forum implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_forum")  // Spécifier le nom correct
    private Long idForum;


    private Date dateCreation;

    @OneToMany(mappedBy = "forum", cascade = CascadeType.ALL)
    private Set<Request> requests;

    @ManyToMany
    @JoinTable(
            name = "forum_subscribers",
            joinColumns = @JoinColumn(name = "id_forum"),
            inverseJoinColumns = @JoinColumn(name = "subscriber_id")
    )
    private Set<Subscriber> subscribers;

    @OneToMany(mappedBy = "forum", cascade = CascadeType.ALL)
    private List<JobOffer> jobOffers;

}
