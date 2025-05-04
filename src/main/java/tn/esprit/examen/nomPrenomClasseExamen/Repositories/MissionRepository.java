package tn.esprit.examen.nomPrenomClasseExamen.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Mission;
import tn.esprit.examen.nomPrenomClasseExamen.entities.MissionRole;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Participation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MissionRepository extends JpaRepository<Mission, Long> {
    List<Mission> findByLocation(String location);

    @Query("SELECT m FROM Mission m WHERE FUNCTION('DATE', m.startDate) = :startDate")
    List<Mission> findByStartDate(@Param("startDate") String startDate);
    List<Mission> findByAssociationsIdAssociation(Long idAssociation);
    @Query("SELECT DISTINCT m FROM Mission m LEFT JOIN FETCH m.missionRoles")
    List<Mission> findAllWithRoles();

    @Query("SELECT m FROM Mission m LEFT JOIN FETCH m.missionRoles WHERE m.id = :id")
    Optional<Mission> findByIdWithRoles(@Param("id") Long id);

    @Query("SELECT mp FROM Participation mp WHERE mp.mission.startDate > :after")
    List<Participation> findAllByMissionStartDateAfter(@Param("after") LocalDateTime after);

}