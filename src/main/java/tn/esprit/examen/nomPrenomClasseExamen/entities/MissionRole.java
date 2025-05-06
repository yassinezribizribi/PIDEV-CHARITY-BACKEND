package tn.esprit.examen.nomPrenomClasseExamen.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MissionRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Mission mission;

    private String roleName; // e.g., Doctor, Nurse, Helper, etc.

    private int numberNeeded;

    private int numberAccepted = 0;

    private boolean requiresValidation; // ✅ Important: requires validated volunteer or not
}

