package tn.esprit.examen.nomPrenomClasseExamen.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Role;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SignupRequestDto {
    private Long idUser;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String telephone;
    private Role role;
    private String skills;
    private String nationality;
    private String expertiseArea;
    private String associationRole;
    private String job;
}
