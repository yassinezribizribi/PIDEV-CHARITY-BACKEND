package tn.esprit.examen.nomPrenomClasseExamen.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Categorie;

import java.util.Date;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CrisisDTO {
    private Long idCrisis;
    private Categorie categorie;
    private String location;
    private String updates;
    private String description;
    private Date crisisDate;
    private Long idUser;



}