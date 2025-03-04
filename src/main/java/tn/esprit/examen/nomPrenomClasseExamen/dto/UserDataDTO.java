package tn.esprit.examen.nomPrenomClasseExamen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDataDTO {
  private Long idUser;
  private String firstName;
  private String lastName;
  private String email;
  private String password;
  private String telephone;
  private Boolean isBanned;
  private String banreason;
  private String job;
  private String resetToken; // ✅ Ajout du champ pour gérer le token de réinitialisation
}
