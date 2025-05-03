package tn.esprit.examen.nomPrenomClasseExamen.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Mission;

import java.util.List;

public interface MissionRepository extends JpaRepository<Mission, Long> {
    List<Mission> findByLocation(String location);
    @Query("SELECT m FROM Mission m WHERE FUNCTION('DATE', m.startDate) = :startDate")
    List<Mission> findByStartDate(@Param("startDate") String startDate);
    List<Mission> findByAssociationMissionIdAssociation(Long idAssociation);


}