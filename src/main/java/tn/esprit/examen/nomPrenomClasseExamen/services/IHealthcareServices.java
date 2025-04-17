package tn.esprit.examen.nomPrenomClasseExamen.services;

import tn.esprit.examen.nomPrenomClasseExamen.dto.HealthcareDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Healthcare;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface IHealthcareServices {
    Healthcare addHealthcare(HealthcareDTO healthcareDTO);
    Healthcare updateHealthcare(Long id, HealthcareDTO healthcareDTO);
    void deleteHealthcare(Long id);
    Optional<Healthcare> getHealthcareById(Long id);
    List<HealthcareDTO> getAllHealthcare();

    Healthcare updateHealthcareStatus(Long healthcareId, String status, Date bookingDate);
}
