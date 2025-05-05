package tn.esprit.examen.nomPrenomClasseExamen.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.NotificationRepository;
import tn.esprit.examen.nomPrenomClasseExamen.dto.NotificationDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Healthcare;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Notification;
import tn.esprit.examen.nomPrenomClasseExamen.entities.NotificationType;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Subscriber;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService implements INotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public void sendHealthcareUpdateNotification(Healthcare healthcare) {
        saveNotification(healthcare.getSubscriber(),
                "🩺 Votre rendez-vous médical a été mis à jour.",
                NotificationType.UPDATE_HEALTHCARE,
                healthcare);
    }

    @Override
    public void sendVideoCallNotification(Healthcare healthcare) {
        saveNotification(healthcare.getSubscriber(),
                "📹 Une consultation vidéo est prévue. Cliquez pour rejoindre.",
                NotificationType.VIDEO_CALL,
                healthcare);
    }

    @Override
    public void sendDailyReminderNotification(Healthcare healthcare) {
        saveNotification(healthcare.getSubscriber(),
                "📅 Vous avez un rendez-vous médical aujourd'hui. Pensez à vous connecter à l'heure prévue.",
                NotificationType.DAILY_REMINDER,
                healthcare);
    }

    private void saveNotification(Subscriber subscriber, String message, NotificationType type, Healthcare healthcare) {
        Notification notif = new Notification();
        notif.setSubscriber(subscriber);
        notif.setMessage(message);
        notif.setType(type);
        notif.setCreatedAt(new Date());
        notif.setHealthcare(healthcare); // Lien avec l'objet Healthcare
        notificationRepository.save(notif);
    }

    @Override
    public List<NotificationDTO> getNotificationsForSubscriber(Long userId) {
        return notificationRepository.findBySubscriber_IdUser(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificationDTO> getHealthcareNotificationsForSubscriber(Long userId) {
        return notificationRepository.findBySubscriber_IdUser(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private NotificationDTO mapToDto(Notification notif) {
        NotificationDTO dto = new NotificationDTO();
        dto.setIdNotification(notif.getId());
        dto.setMessage(notif.getMessage());
        dto.setCreatedAt(notif.getCreatedAt());
        dto.setSubscriberId(notif.getSubscriber().getIdUser());

        if (notif.getHealthcare() != null) {
            dto.setTerminalDisease(notif.getHealthcare().getTerminalDisease());
            dto.setTreatmentPlan(notif.getHealthcare().getTreatmentPlan());
            dto.setBookingDate(notif.getHealthcare().getBookingDate());
            dto.setMeetingUrl(notif.getHealthcare().getMeetingUrl());
        }

        return dto;
    }

    public Notification createNotification(NotificationDTO dto) {
        if (dto.getBookingDate() == null) {
            throw new IllegalArgumentException("⛔ Date de rendez-vous non spécifiée !");
        }

        LocalDate rdvDate = new java.sql.Date(dto.getBookingDate().getTime()).toLocalDate();

        // Vérifie les jours fériés via l'entité Notification
        if (Notification.isHoliday(rdvDate)) {
            throw new IllegalArgumentException("❌ Impossible de planifier un rendez-vous un jour férié !");
        }

        Notification notification = new Notification();
        notification.setMessage(dto.getMessage() != null ? dto.getMessage() : "📬 Nouvelle notification médicale");
        notification.setCreatedAt(new Date());

        // Affecter le subscriber
        Subscriber subscriber = new Subscriber();
        subscriber.setIdUser(dto.getSubscriberId());
        notification.setSubscriber(subscriber);

        // Si booking lié à un soin médical
        if (dto.getBookingDate() != null) {
            Healthcare healthcare = new Healthcare();
            healthcare.setBookingDate(dto.getBookingDate());
            healthcare.setTreatmentPlan(dto.getTreatmentPlan());
            healthcare.setTerminalDisease(dto.getTerminalDisease());
            healthcare.setMeetingUrl(dto.getMeetingUrl());
            notification.setHealthcare(healthcare);
        }

        return notificationRepository.save(notification);
    }
}
