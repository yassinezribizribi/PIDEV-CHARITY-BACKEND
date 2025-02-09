package tn.esprit.examen.nomPrenomClasseExamen.entities;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED) // Using JOINED strategy
@FieldDefaults(level= AccessLevel.PRIVATE)
@Entity
public class User implements Serializable{
    @Id
    private Long idUser;
    private String FirstName;
    private String LastName;
    private String Email;
    private String Password;
    private int Telephone ;
    private Boolean isBanned;
    private String Banreason;
    private String Job ;



}
