package tn.esprit.examen.nomPrenomClasseExamen.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.HealthcareRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.SubscriberRepository;
import tn.esprit.examen.nomPrenomClasseExamen.dto.HealthcareDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Healthcare;
import tn.esprit.examen.nomPrenomClasseExamen.entities.HealthcareStatus;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Subscriber;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HealthcareServices implements IHealthcareServices {

    private final HealthcareRepository healthcareRepository;
    private final SubscriberRepository subscriberRepository;
    private final NotificationService notificationService;
    private final PdfService pdfService;
    private final EmailService emailService;
    @Transactional
    @Override
    public Healthcare addHealthcare(HealthcareDTO dto) {
        Subscriber patient = subscriberRepository.findById(dto.getSubscriberId())
                .orElseThrow(() -> new RuntimeException("❌ Patient introuvable !"));

        Subscriber doctor = subscriberRepository.findAll().stream()
                .filter(u -> "Médecin".equalsIgnoreCase(u.getJob()))
                .findFirst()
                .orElse(null);

        Healthcare healthcare = new Healthcare();
        healthcare.setSubscriber(patient);
        healthcare.setDoctor(doctor);
        healthcare.setHistory(dto.getHistory());
        healthcare.setTreatmentPlan(dto.getTreatmentPlan());
        healthcare.setTerminalDisease(dto.getTerminalDisease());
        healthcare.setBookingDate(dto.getBookingDate());
        healthcare.setStatus(HealthcareStatus.PENDING);
        healthcare.setMeetingUrl("https://meet.jit.si/" + UUID.randomUUID());

        notificationService.sendHealthcareUpdateNotification(healthcare);
        notificationService.sendVideoCallNotification(healthcare);

        return healthcareRepository.save(healthcare);
    }

    @Transactional
    @Override
    public Healthcare updateHealthcare(Long id, HealthcareDTO dto) {
        Healthcare healthcare = healthcareRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("❌ Soin non trouvé avec l'ID : " + id));

        boolean shouldSendReport =
                (healthcare.getTreatmentPlan() == null && dto.getTreatmentPlan() != null) ||
                        (healthcare.getTerminalDisease() == null && dto.getTerminalDisease() != null);

        healthcare.setTerminalDisease(dto.getTerminalDisease());
        healthcare.setTreatmentPlan(dto.getTreatmentPlan());
        healthcare.setBookingDate(dto.getBookingDate());
        healthcare.setStatus(HealthcareStatus.valueOf(dto.getStatus()));

        if (dto.getDoctorId() != null) {
            Subscriber doctor = subscriberRepository.findById(dto.getDoctorId())
                    .orElseThrow(() -> new RuntimeException("❌ Médecin introuvable !"));
            healthcare.setDoctor(doctor);
        }

        notificationService.sendHealthcareUpdateNotification(healthcare);
        Healthcare saved = healthcareRepository.save(healthcare);

        // Générer et envoyer le PDF si nécessaire
        if (shouldSendReport && healthcare.getSubscriber() != null) {
            byte[] pdf = pdfService.generatePdfFromHealthcare(saved);
            String refugeeEmail = healthcare.getSubscriber().getEmail();

            emailService.sendEmailWithAttachment(
                    refugeeEmail,
                    pdf,
                    "rapport_soin_" + saved.getIdHealthcare() + ".pdf"

            );
        }

        return saved;
    }

    @Transactional
    @Override
    public Healthcare updateHealthcareStatus(Long id, String status, Date bookingDate) {
        Healthcare healthcare = healthcareRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("❌ Rendez-vous introuvable !"));

        healthcare.setStatus(HealthcareStatus.valueOf(status));
        healthcare.setBookingDate(bookingDate);

        notificationService.sendHealthcareUpdateNotification(healthcare);
        return healthcareRepository.save(healthcare);
    }

    /**
     * 🔔 Cette méthode planifiée tourne tous les jours à 8h du matin
     * et envoie des notifications pour les rendez-vous ACCEPTÉS de la journée
     */
    @Scheduled(cron = "0 0 8 * * ?")
    @Transactional
    public void notifyTodayAppointments() {
        Date today = new Date();
        List<Healthcare> appointments = healthcareRepository.findAll().stream()
                .filter(h -> h.getBookingDate() != null
                        && isSameDay(h.getBookingDate(), today)
                        && h.getStatus().name().equals("ACCEPTED"))
                .collect(Collectors.toList());

        for (Healthcare h : appointments) {
            notificationService.sendDailyReminderNotification(h); // nouveau message du jour
            notificationService.sendVideoCallNotification(h);     // lien visio
        }

        System.out.println("📅 Notifications envoyées pour " + appointments.size() + " rendez-vous aujourd'hui.");
    }


    private boolean isSameDay(Date d1, Date d2) {
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTime(d1);
        c2.setTime(d2);
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }
    @Override
    public Healthcare proposeAppointment(Long healthcareId, Date proposedDate) {
        Healthcare healthcare = healthcareRepository.findById(healthcareId)
                .orElseThrow(() -> new RuntimeException("Soin introuvable"));

        healthcare.setProposedBookingDate(proposedDate);
        healthcare.setStatus(HealthcareStatus.PROPOSED);
        return healthcareRepository.save(healthcare);
    }

    @Override
    public Healthcare confirmAppointment(Long healthcareId) {
        Healthcare healthcare = healthcareRepository.findById(healthcareId)
                .orElseThrow(() -> new RuntimeException("Soin introuvable"));

        healthcare.setBookingDate(healthcare.getProposedBookingDate());
        healthcare.setProposedBookingDate(null);
        healthcare.setStatus(HealthcareStatus.CONFIRMED);
        return healthcareRepository.save(healthcare);
    }


    @Transactional
    @Override
    public void deleteHealthcare(Long id) {
        if (!healthcareRepository.existsById(id))
            throw new RuntimeException("❌ Soin introuvable !");
        healthcareRepository.deleteById(id);
    }
    public Healthcare confirmBookingDate(Long healthcareId) {
        Healthcare healthcare = healthcareRepository.findById(healthcareId)
                .orElseThrow(() -> new RuntimeException("Soin introuvable"));

        healthcare.setBookingDate(healthcare.getProposedBookingDate());
        healthcare.setProposedBookingDate(null);
        healthcare.setStatus(HealthcareStatus.CONFIRMED); // 💡 Ici l'enum
        return healthcareRepository.save(healthcare);
    }


    @Override
    public Optional<Healthcare> getHealthcareById(Long id) {
        return healthcareRepository.findById(id);
    }

    @Override
    public List<HealthcareDTO> getAllHealthcare() {
        return healthcareRepository.findAll().stream().map(h -> {
            HealthcareDTO dto = new HealthcareDTO();
            dto.setId(h.getIdHealthcare());
            dto.setHistory(h.getHistory());
            dto.setTreatmentPlan(h.getTreatmentPlan());
            dto.setTerminalDisease(h.getTerminalDisease());
            dto.setBookingDate(h.getBookingDate());
            dto.setStatus(h.getStatus().name());
            dto.setMeetingUrl(h.getMeetingUrl());
            if (h.getSubscriber() != null)
                dto.setSubscriberId(h.getSubscriber().getIdUser());
            if (h.getDoctor() != null)
                dto.setDoctorId(h.getDoctor().getIdUser());
            return dto;
        }).collect(Collectors.toList());
    }
}
