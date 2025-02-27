package tn.esprit.examen.nomPrenomClasseExamen.services;

import tn.esprit.examen.nomPrenomClasseExamen.dto.CrisisDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Crisis;
import java.util.List;
import java.util.Optional;

public interface ICrisisServices {


    public Crisis addCrisis(CrisisDTO crisisDTO);


    Crisis updateCrisis(Long id, Crisis crisis);
    void deleteCrisis(Long id);
    Optional<Crisis> getCrisisById(Long id);
    List<Crisis> getAllCrises();
    List<Crisis> getCrisesBySubscriber(Long idUser);
}
