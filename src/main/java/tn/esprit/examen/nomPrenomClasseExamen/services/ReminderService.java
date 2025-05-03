package tn.esprit.examen.nomPrenomClasseExamen.services;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.AnimalRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.EventRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.EventSubscriberRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.NotificationRepository;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Animal;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Event;
import tn.esprit.examen.nomPrenomClasseExamen.entities.EventSubscriber;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Notification;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReminderService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventSubscriberRepository eventSubscriberRepository;
    @Autowired
    EmailService emailService;
    @Autowired
    private AnimalRepository animalRepository;
    @Autowired
    private NotificationRepository notificationRepository;


   //@Scheduled(cron = "0 * * * * ?")
    public void sendEventReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twoDaysFromNow = now.plusDays(2);

        List<Event> eventsInTwoDays = eventRepository.findEventByDateTimeBetween(twoDaysFromNow.minusDays(1), twoDaysFromNow.plusDays(1));

        for (Event event : eventsInTwoDays) {
            List<EventSubscriber> subscribers = eventSubscriberRepository.findByEvent(event);
            for (EventSubscriber subscriber : subscribers) {
                emailService.sendReminderEmail(subscriber.getSubscriber(), event);
            }
        }
    }


    //calculating animal dispo
    @Scheduled(cron = "*/10 * * * * ?")
    public void updateUnadoptedAnimalsCount() {
        // Compter les animaux non adoptés
        List<Animal> unadoptedAnimals = animalRepository.findByIsAdoptedFalse();

        // Créer ou mettre à jour une notification
        Notification notification = notificationRepository.findByType("UNADOPTED_ANIMALS")
                .orElse(new Notification());
        notification.setType("UNADOPTED_ANIMALS");
        notification.setMessage(String.format("Il y a actuellement %d animaux qui attendent une adoption.", unadoptedAnimals.size()));
        notification.setUpdatedAt(LocalDateTime.now());

        notificationRepository.save(notification);
    }


}
