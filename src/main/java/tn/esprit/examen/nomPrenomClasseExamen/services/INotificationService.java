package tn.esprit.examen.nomPrenomClasseExamen.services;

import tn.esprit.examen.nomPrenomClasseExamen.dto.NotificationDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Healthcare;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Notification;

import java.util.List;

public interface INotificationService {
    void sendHealthcareUpdateNotification(Healthcare healthcare);
    void sendVideoCallNotification(Healthcare healthcare);
    void sendDailyReminderNotification(Healthcare healthcare);

    List<NotificationDTO> getNotificationsForSubscriber(Long userId);
    List<NotificationDTO> getHealthcareNotificationsForSubscriber(Long userId);

    // 👉 Nouvelle méthode à ajouter
    Notification createNotification(NotificationDTO dto);
}
