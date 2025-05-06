package tn.esprit.examen.nomPrenomClasseExamen.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.examen.nomPrenomClasseExamen.entities.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ParticipationRepository extends JpaRepository<Participation,Long> {

    boolean existsBySubscriberAndMission(Subscriber subscriber, Mission mission);
    boolean existsBySubscriberAndMissionRoleAndParticipationStatus(
            Subscriber subscriber,
            MissionRole missionRole,
            ParticipationStatus participationStatus
    );
//    boolean existsBySubscriberAndMissionRoleAndParticipationStatusIn(
//            Subscriber subscriber,
//            MissionRole missionRole,
//            List<ParticipationStatus> statuses
//    );

    boolean existsBySubscriberAndMissionRoleAndParticipationStatusIn(
            Subscriber subscriber,
            MissionRole missionRole,
            List<ParticipationStatus> statuses
    );
    List<Participation> findBySubscriberAndParticipationStatus(Subscriber subscriber, ParticipationStatus status);
    Optional<Participation> findBySubscriberAndMissionAndParticipationStatusIn(
            Subscriber subscriber,
            Mission mission,
            List<ParticipationStatus> statuses
    );
    List<Participation> findBySubscriberIdUser(Long userId);
    @Query("SELECT mp FROM Participation mp WHERE mp.mission.startDate > :after")
    List<Participation> findAllByMissionStartDateAfter(@Param("after") LocalDateTime after);
}
