package tn.esprit.examen.nomPrenomClasseExamen.services;

import tn.esprit.examen.nomPrenomClasseExamen.dto.NotificationDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Healthcare;

import java.util.List;

public interface INotificationService {
    void sendHealthcareUpdateNotification(Healthcare healthcare);
    void sendVideoCallNotification(Healthcare healthcare);
    List<NotificationDTO> getNotificationsForSubscriber(Long userId);
}
