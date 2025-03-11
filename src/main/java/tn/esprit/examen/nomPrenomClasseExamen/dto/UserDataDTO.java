package tn.esprit.examen.nomPrenomClasseExamen.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDataDTO {
  private Long idUser;
  @NotNull(message = "Le nom ne peut pas être null")
  @Size(min = 3, max = 50, message = "Le nom doit contenir entre 3 et 50 caractères")
  private String firstName;
  private String lastName;
  @NotEmpty(message = "L'email est obligatoire")
  @Email(message = "L'email doit être valide")
  private String email;
  private String password;
  private String telephone;
  private Boolean isBanned;
  private String banreason;
  private String job;



}
