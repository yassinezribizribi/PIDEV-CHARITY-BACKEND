package tn.esprit.examen.nomPrenomClasseExamen.services;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.NotificationRepository;
import tn.esprit.examen.nomPrenomClasseExamen.dto.NotificationDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService implements INotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void sendHealthcareUpdateNotification(Healthcare healthcare) {
        createAndSendNotification(
                "🩺 Votre rendez-vous médical a été mis à jour",
                Notification.NotificationType.HEALTHCARE_UPDATE,
                "/healthcare/" + healthcare.getIdHealthcare(),
                healthcare.getSubscriber(),
                null,
                healthcare,
                null
        );
    }

    @Override
    public void sendVideoCallNotification(Healthcare healthcare) {
        createAndSendNotification(
                "📹 Une consultation vidéo est prévue. Cliquez pour rejoindre",
                Notification.NotificationType.VIDEO_CALL,
                "/video-call/" + healthcare.getIdHealthcare(),
                healthcare.getSubscriber(),
                null,
                healthcare,
                null
        );
    }

    @Override
    public void sendDailyReminderNotification(Healthcare healthcare) {
        createAndSendNotification(
                "📅 Vous avez un rendez-vous médical aujourd'hui. Pensez à vous connecter à l'heure prévue.",
                Notification.NotificationType.APPOINTMENT_REMINDER,
                "/healthcare/" + healthcare.getIdHealthcare(),
                healthcare.getSubscriber(),
                null,
                healthcare,
                null
        );
    }

    @Override
    public void sendTierChangeNotification(Association association, String oldTier, String newTier) {
        createAndSendNotification(
                String.format("🎉 Votre niveau de partenariat a changé de %s à %s", oldTier, newTier),
                Notification.NotificationType.TIER_CHANGE,
                "/associations/" + association.getIdAssociation() + "/partnership",
                association.getSubscriber(),
                null,
                null,
                association
        );
    }

    @Override
    public void sendNewPartnershipNotification(Association association, Association partner) {
        createAndSendNotification(
                String.format("🤝 Partnership request sent to %s", partner.getAssociationName()),
                Notification.NotificationType.PARTNERSHIP_REQUEST,
                "/associations/" + association.getIdAssociation() + "/partners",
                association.getSubscriber(),
                null,
                null,
                association
        );

        createAndSendNotification(
                String.format("🤝 New partnership request from %s", association.getAssociationName()),
                Notification.NotificationType.PARTNERSHIP_REQUEST,
                "/associations/" + partner.getIdAssociation() + "/partners",
                partner.getSubscriber(),
                association.getSubscriber(),
                null,
                partner
        );
    }

    @Override
    public void sendPartnershipRemovedNotification(Association association, Association formerPartner) {
        createAndSendNotification(
                String.format("🚫 Partnership with %s has been removed", formerPartner.getAssociationName()),
                Notification.NotificationType.PARTNERSHIP_REMOVED,
                "/associations/" + association.getIdAssociation() + "/partners",
                association.getSubscriber(),
                null,
                null,
                association
        );
    }

    @Override
    public List<NotificationDTO> getNotificationsForSubscriber(Long userId) {
        return notificationRepository.findBySubscriber_IdUser(userId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificationDTO> getHealthcareNotificationsForSubscriber(Long userId) {
        return getNotificationsForSubscriber(userId).stream()
                .filter(dto -> dto.getHealthcareId() != null)
                .collect(Collectors.toList());
    }

    @Override
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
            Notification saved = notificationRepository.save(notification);
            sendRealTimeNotification(saved);
        });
    }

    // 🔄 CRON: Suppression des anciennes notifications
    @Scheduled(cron = "0 0 0 * * *")
    public void deleteOldNotifications() {
        Date thirtyDaysAgo = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30));
        notificationRepository.deleteByCreatedAtBefore(thirtyDaysAgo);
    }

    // 🔄 CRON: Marquer comme lues automatiquement
    @Scheduled(cron = "0 0 1 * * *")
    public void autoMarkOldNotificationsAsRead() {
        Date sevenDaysAgo = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7));
        List<Notification> unreadOld = notificationRepository.findByCreatedAtBeforeAndReadIsFalse(sevenDaysAgo);
        unreadOld.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unreadOld);
    }

    public Notification createNotification(NotificationDTO dto) {
        if (dto.getBookingDate() == null) {
            throw new IllegalArgumentException("⛔ Date de rendez-vous non spécifiée !");
        }

        LocalDate rdvDate = new java.sql.Date(dto.getBookingDate().getTime()).toLocalDate();

        if (Notification.isHoliday(rdvDate)) {
            throw new IllegalArgumentException("❌ Impossible de planifier un rendez-vous un jour férié !");
        }

        Notification notification = new Notification();
        notification.setMessage(dto.getMessage() != null ? dto.getMessage() : "📬 Nouvelle notification médicale");
        notification.setCreatedAt(new Date());
        notification.setRead(false);

        Subscriber subscriber = new Subscriber();
        subscriber.setIdUser(dto.getSubscriberId());
        notification.setSubscriber(subscriber);

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

    public void createAndSendNotification(String message,
                                          Notification.NotificationType type,
                                          String actionUrl,
                                          Subscriber receiver,
                                          Subscriber sender,
                                          Healthcare healthcare,
                                          Association association) {
        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setType(type);
        notification.setActionUrl(actionUrl);
        notification.setSubscriber(receiver);
        notification.setSender(sender);
        notification.setHealthcare(healthcare);
        notification.setAssociation(association);
        notification.setCreatedAt(new Date());
        notification.setRead(false);

        Notification saved = notificationRepository.save(notification);
        sendRealTimeNotification(saved);
    }

    private void sendRealTimeNotification(Notification notification) {
        messagingTemplate.convertAndSendToUser(
                notification.getSubscriber().getIdUser().toString(),
                "/queue/notifications",
                convertToDto(notification)
        );
    }

    private NotificationDTO convertToDto(Notification notification) {
        return NotificationDTO.builder()
                .idNotification(notification.getId())
                .message(notification.getMessage())
                .createdAt(notification.getCreatedAt())
                .read(notification.isRead())
                .type(notification.getType().name())
                .actionUrl(notification.getActionUrl())
                .subscriberId(notification.getSubscriber().getIdUser())
                .senderId(notification.getSender() != null ? notification.getSender().getIdUser() : null)
                .healthcareId(notification.getHealthcare() != null ? notification.getHealthcare().getIdHealthcare() : null)
                .associationId(notification.getAssociation() != null ? notification.getAssociation().getIdAssociation() : null)
                .build();
    }
}
