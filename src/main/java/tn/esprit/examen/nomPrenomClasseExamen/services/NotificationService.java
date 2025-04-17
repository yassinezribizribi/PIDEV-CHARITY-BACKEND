package tn.esprit.examen.nomPrenomClasseExamen.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.NotificationRepository;
import tn.esprit.examen.nomPrenomClasseExamen.dto.NotificationDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Healthcare;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Notification;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Subscriber;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService implements INotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public void sendHealthcareUpdateNotification(Healthcare healthcare) {
        Notification notif = new Notification();
        notif.setMessage("🩺 Votre rendez-vous médical a été mis à jour.");
        notif.setSubscriber(healthcare.getSubscriber());
        notif.setCreatedAt(new Date());

        notificationRepository.save(notif);
    }

    @Override
    public void sendVideoCallNotification(Healthcare healthcare) {
        Notification notif = new Notification();
        notif.setMessage("📹 Une consultation vidéo est prévue. Cliquez pour rejoindre.");
        notif.setSubscriber(healthcare.getSubscriber());
        notif.setCreatedAt(new Date());

        notificationRepository.save(notif);
    }

    @Override
    public List<NotificationDTO> getNotificationsForSubscriber(Long userId) {
        return notificationRepository.findBySubscriber_IdUser(userId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private NotificationDTO toDto(Notification notif) {
        NotificationDTO dto = new NotificationDTO();
        dto.setIdNotification(notif.getId());
        dto.setMessage(notif.getMessage());
        dto.setCreatedAt(notif.getCreatedAt());
        dto.setSubscriberId(notif.getSubscriber().getIdUser());
        return dto;
    }
}
