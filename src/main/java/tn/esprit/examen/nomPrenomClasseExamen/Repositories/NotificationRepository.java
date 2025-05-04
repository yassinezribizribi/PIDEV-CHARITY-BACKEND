package tn.esprit.examen.nomPrenomClasseExamen.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Notification;

import java.util.Date;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findBySubscriber_IdUser(Long userId);

    void deleteByCreatedAtBefore(Date cutoffDate);

    List<Notification> findByCreatedAtBeforeAndReadIsFalse(Date cutoffDate);
}

