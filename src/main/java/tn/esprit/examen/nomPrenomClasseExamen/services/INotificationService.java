package tn.esprit.examen.nomPrenomClasseExamen.services;

import tn.esprit.examen.nomPrenomClasseExamen.dto.NotificationDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Notification;

import java.util.List;

public interface INotificationService {
    Notification addNotification(NotificationDTO notificationDTO);
    List<Notification> getNotificationsBySubscriber(Long subscriberId);
}
