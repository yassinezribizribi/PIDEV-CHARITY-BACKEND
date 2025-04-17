package tn.esprit.examen.nomPrenomClasseExamen.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.HealthcareRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.SubscriberRepository;
import tn.esprit.examen.nomPrenomClasseExamen.dto.HealthcareDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Healthcare;
import tn.esprit.examen.nomPrenomClasseExamen.entities.HealthcareStatus;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Subscriber;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HealthcareServices implements IHealthcareServices {

    private final HealthcareRepository healthcareRepository;
    private final SubscriberRepository subscriberRepository;
    private final NotificationService notificationService;

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
        return healthcareRepository.save(healthcare);
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

    @Transactional
    @Override
    public void deleteHealthcare(Long id) {
        if (!healthcareRepository.existsById(id))
            throw new RuntimeException("❌ Soin introuvable !");
        healthcareRepository.deleteById(id);
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
