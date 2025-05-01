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

    // ✅ Ajouter un soin
    @Transactional
    @Override
    public Healthcare addHealthcare(HealthcareDTO dto) {
        Subscriber patient = subscriberRepository.findById(dto.getSubscriberId())
                .orElseThrow(() -> new RuntimeException("❌ Patient introuvable !"));

        // 🔍 Recherche automatique d’un médecin (par rôle ou métier)
        Subscriber doctor = subscriberRepository.findAll().stream()
                .filter(u -> "Médecin".equalsIgnoreCase(u.getJob()))
                .findFirst()
                .orElse(null); // peut être null s’il n’y a aucun médecin

        Healthcare healthcare = new Healthcare();
        healthcare.setSubscriber(patient);
        healthcare.setDoctor(doctor); // ✅ peut être null
        healthcare.setHistory(dto.getHistory());
        healthcare.setTreatmentPlan(dto.getTreatmentPlan());
        healthcare.setTerminalDisease(dto.getTerminalDisease());
        healthcare.setBookingDate(dto.getBookingDate());
        healthcare.setStatus(HealthcareStatus.PENDING);
        // ⬇️ Notify le patient
        notificationService.sendHealthcareUpdateNotification(healthcare);

        // ✅ Génération automatique du lien Jitsi
        healthcare.setMeetingUrl("https://meet.jit.si/" + UUID.randomUUID());
        notificationService.sendVideoCallNotification(healthcare);

        return healthcareRepository.save(healthcare);
    }


    // ✅ Modifier un soin existant
    @Transactional
    @Override
    public Healthcare updateHealthcare(Long id, HealthcareDTO dto) {
        Healthcare healthcare = healthcareRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("❌ Rendez-vous introuvable !"));

        healthcare.setHistory(dto.getHistory());
        healthcare.setTreatmentPlan(dto.getTreatmentPlan());
        healthcare.setTerminalDisease(dto.getTerminalDisease());
        healthcare.setBookingDate(dto.getBookingDate());

        return healthcareRepository.save(healthcare);
    }

    // ✅ Modifier uniquement le statut et la date
    @Transactional
    @Override
    public Healthcare updateHealthcareStatus(Long id, String status, Date bookingDate) {
        Healthcare healthcare = healthcareRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("❌ Rendez-vous introuvable !"));

        healthcare.setStatus(HealthcareStatus.valueOf(status));
        healthcare.setBookingDate(bookingDate);

        return healthcareRepository.save(healthcare);
    }

    // ✅ Supprimer un soin
    @Transactional
    @Override
    public void deleteHealthcare(Long id) {
        if (!healthcareRepository.existsById(id)) {
            throw new RuntimeException("❌ Soin introuvable !");
        }
        healthcareRepository.deleteById(id);
    }

    // ✅ Récupérer un soin par ID
    @Override
    public Optional<Healthcare> getHealthcareById(Long id) {
        return healthcareRepository.findById(id);
    }

    // ✅ Récupérer tous les soins avec conversion DTO
    @Override
    public List<HealthcareDTO> getAllHealthcare() {
        return healthcareRepository.findAll().stream().map(h -> {
            HealthcareDTO dto = new HealthcareDTO();
            dto.setHistory(h.getHistory());
            dto.setTreatmentPlan(h.getTreatmentPlan());
            dto.setTerminalDisease(h.getTerminalDisease());
            dto.setBookingDate(h.getBookingDate());
            dto.setStatus(h.getStatus().name());
            dto.setMeetingUrl(h.getMeetingUrl());

            // ✅ Utilisation correcte des ID
            if (h.getSubscriber() != null) {
                dto.setSubscriberId(h.getSubscriber().getIdUser());
            }

            if (h.getDoctor() != null) {
                dto.setDoctorId(h.getDoctor().getIdUser());
            }

            return dto;
        }).collect(Collectors.toList());
    }
}
