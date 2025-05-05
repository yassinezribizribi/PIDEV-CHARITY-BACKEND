package tn.esprit.examen.nomPrenomClasseExamen.services;

import tn.esprit.examen.nomPrenomClasseExamen.dto.NotificationDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Association;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Healthcare;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Notification;

import java.util.List;

public interface INotificationService {

    // 🔔 Notifications médicales
    void sendHealthcareUpdateNotification(Healthcare healthcare);
    void sendVideoCallNotification(Healthcare healthcare);
    void sendDailyReminderNotification(Healthcare healthcare);

    // 🤝 Notifications de partenariat
    void sendTierChangeNotification(Association association, String oldTier, String newTier);
    void sendNewPartnershipNotification(Association association, Association partner);
    void sendPartnershipRemovedNotification(Association association, Association formerPartner);

    // 📩 CRUD de notifications
    Notification createNotification(NotificationDTO dto);
    List<NotificationDTO> getNotificationsForSubscriber(Long userId);
    List<NotificationDTO> getHealthcareNotificationsForSubscriber(Long userId);

    // ✅ Fonction pour marquer comme lu
    void markAsRead(Long notificationId);
}
