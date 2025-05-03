package tn.esprit.examen.nomPrenomClasseExamen.entities;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.Set;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
@Entity
public class User implements Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUser;
    private String firstName;
    private String lastName;
    @NotNull
    protected String email;
    private String password;
    private String telephone;
    private Boolean isBanned;
    private String banreason;
    private String job;


}
