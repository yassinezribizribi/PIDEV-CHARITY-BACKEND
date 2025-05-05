package tn.esprit.examen.nomPrenomClasseExamen.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Mission;
import tn.esprit.examen.nomPrenomClasseExamen.entities.MissionRole;

import java.util.List;

public interface MissionRoleRepository extends JpaRepository<MissionRole, Long> {
    List<MissionRole> findByMission(Mission mission);

}
