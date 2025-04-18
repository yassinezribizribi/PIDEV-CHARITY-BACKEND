package tn.esprit.examen.nomPrenomClasseExamen.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Categorie;
import tn.esprit.examen.nomPrenomClasseExamen.entities.CrisisSeverity;
import tn.esprit.examen.nomPrenomClasseExamen.entities.CrisisStatus;

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
    private Double latitude;
    private Double longitude;
   private CrisisSeverity severity;
 private MultipartFile  image;







}