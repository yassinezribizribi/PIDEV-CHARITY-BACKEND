package tn.esprit.examen.nomPrenomClasseExamen.services;

import org.springframework.web.multipart.MultipartFile;
import tn.esprit.examen.nomPrenomClasseExamen.dto.CrisisDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Crisis;
import tn.esprit.examen.nomPrenomClasseExamen.entities.CrisisSeverity;
import tn.esprit.examen.nomPrenomClasseExamen.entities.CrisisStatus;

import java.util.List;
import java.util.Optional;

public interface ICrisisServices {


    public Crisis addCrisis(CrisisDTO crisisDTO);


    Crisis updateCrisis(Long id, Crisis crisis);
    void deleteCrisis(Long id);
    Optional<Crisis> getCrisisById(Long id);
    List<Crisis> getAllCrises();
    List<Crisis> getCrisesBySubscriber(Long idUser);
    List<Crisis> getCrisesByStatus(CrisisStatus status);
    List<Crisis> getCrisesBySeverity(CrisisSeverity severity);
    Crisis addCrisisWithImage(CrisisDTO crisisDTO, MultipartFile file);


}
