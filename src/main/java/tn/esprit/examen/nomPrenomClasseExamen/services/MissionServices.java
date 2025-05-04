package tn.esprit.examen.nomPrenomClasseExamen.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.security.core.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.*;
import tn.esprit.examen.nomPrenomClasseExamen.dto.MissionDTO;
import tn.esprit.examen.nomPrenomClasseExamen.dto.MissionRoleDTO;
import tn.esprit.examen.nomPrenomClasseExamen.dto.ParticipationMissionDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.*;
import tn.esprit.examen.nomPrenomClasseExamen.jwt.JwtUtils;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static tn.esprit.examen.nomPrenomClasseExamen.entities.VolunteerStatus.*;

@Service
@RequiredArgsConstructor
public class MissionServices {
    private final JwtUtils jwtUtils; // Inject JwtUtils to extract the User ID from JWT
    private final AssociationRepository associationRepository;
    private final CrisisRepository crisisRepository;
    private final MissionRepository missionRepository;
    private final MissionRoleRepository missionRoleRepository;
    private final ParticipationRepository participationRepository;
    private  final MailerSendService mailerSendService;
    @Autowired
    private SubscriberRepository subscriberRepository;

    private static final Logger logger = LoggerFactory.getLogger(MissionServices.class);
    //    public List<MissionDTO> getMissionsByLocation(String location) {
//        List<Mission> missions = missionRepository.findByLocation(location);
//        return missions.stream()
//                .map(this::convertToDTO)
//                .collect(Collectors.toList());
//    }
    public List<MissionDTO> getMissionsByLocation(String location) {
        List<Mission> missions = missionRepository.findByLocation(location);
        return missions.stream()
                .map(MissionDTO::fromMission)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public Set<MissionRole> getMissionRolesByMissionId(Long missionId) {
        logger.info("🔍 Searching for MissionRoles linked to Mission ID: {}", missionId);

        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new RuntimeException("Mission with ID " + missionId + " not found"));

        Set<MissionRole> missionRoles = mission.getMissionRoles();

        if (missionRoles == null || missionRoles.isEmpty()) {
            throw new RuntimeException("No MissionRoles found for Mission ID " + missionId);
        }

        return missionRoles;
    }
    @Transactional
    public Mission createMission(MissionDTO missionDto, MultipartFile missionLogo, String jwtToken) {
        if (jwtToken == null || jwtToken.trim().isEmpty()) {
            throw new RuntimeException("JWT token cannot be null or empty");
        }

        try {
            Long userId = jwtUtils.getUserIdFromJwtToken(jwtToken);
            logger.info("🔍 User ID extracted from JWT: {}", userId);

            Association association = associationRepository.findBySubscriberIdUser(userId)
                    .orElseThrow(() -> new RuntimeException("Association not found for user ID: " + userId));

            // ✅ Save mission logo if provided
            if (missionLogo != null && !missionLogo.isEmpty()) {
                saveFile(missionLogo, missionDto::setMissionLogoPath);
            }

            Mission mission = new Mission();
            mission.setTitle(missionDto.getTitle());
            mission.setDescription(missionDto.getDescription());
            mission.setLocation(missionDto.getLocation());
            mission.setStartDate(missionDto.getStartDate());
            mission.setEndDate(missionDto.getEndDate());
            mission.setStatus(MissionStatus.UPCOMING);
            mission.setMissionLogoPath(missionDto.getMissionLogoPath());

            mission.setAssociations(new HashSet<>());
            mission.getAssociations().add(association);

            Set<MissionRole> roles = new HashSet<>();
            int totalVolunteersNeeded = 0;

            if (missionDto.getMissionRoles() != null) {
                for (MissionRoleDTO roleDto : missionDto.getMissionRoles()) {
                    MissionRole role = new MissionRole();
                    role.setRoleName(roleDto.getRoleName());
                    role.setNumberNeeded(roleDto.getNumberNeeded());
                    role.setRequiresValidation(roleDto.isRequiresValidation());
                    role.setMission(mission);

                    totalVolunteersNeeded += roleDto.getNumberNeeded();
                    roles.add(role);
                }
            }

            mission.setMissionRoles(roles);
            mission.setVolunteerCount(totalVolunteersNeeded);

            Mission savedMission = missionRepository.save(mission);
            logger.info("✅ Mission successfully created with ID: {}", savedMission.getIdMission());
            return savedMission;

        } catch (IOException e) {
            logger.error("❌ File saving error: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save mission logo: " + e.getMessage());
        } catch (Exception e) {
            logger.error("❌ Error occurred while creating mission: {}", e.getMessage(), e);
            throw new RuntimeException("Error while creating mission: " + e.getMessage());
        }
    }
    private void saveFile(MultipartFile file, Consumer<String> setPath) throws IOException {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path uploadPath = Paths.get("C:\\Users\\user\\Desktop\\uploads");

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(fileName);
        Files.write(filePath, file.getBytes());

        setPath.accept(fileName);
    }


    @Transactional
    public void reviewParticipation(Long participationId, Boolean approve, String rejectionReason) {
        Participation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new IllegalArgumentException("Participation not found"));

        Subscriber subscriber = participation.getSubscriber();
        MissionRole missionRole = participation.getMissionRole();

        if (approve != null && approve) {
            subscriber.setValidationStatus(VALIDATED);
            subscriberRepository.save(subscriber);

            participation.setParticipationStatus(ParticipationStatus.ACCEPTED);
            missionRole.setNumberAccepted(missionRole.getNumberAccepted() + 1);
            missionRoleRepository.save(missionRole);
        } else {
            subscriber.setValidationStatus(NOT_VALIDATED);
            participation.setParticipationStatus(ParticipationStatus.REJECTED);
            participation.setRejectionReason(rejectionReason); // ✅ SET THE REJECTION REASON
        }

        participationRepository.save(participation);
    }

    private void acceptParticipation(Subscriber subscriber, MissionRole missionRole) {
        // Check again spots
        if (missionRole.getNumberAccepted() >= missionRole.getNumberNeeded()) {
            throw new IllegalStateException("Role is already full");
        }

        missionRole.setNumberAccepted(missionRole.getNumberAccepted() + 1);
        missionRoleRepository.save(missionRole);

        Participation participation = Participation.builder()
                .subscriber(subscriber)
                .missionRole(missionRole)
                .participationStatus(ParticipationStatus.ACCEPTED)
                .build();
        participationRepository.save(participation);

        logger.info("Volunteer {} accepted for role {}", subscriber.getEmail(), missionRole.getRoleName());
    }
//    @Transactional
//    public void applyToMissionRole(Long missionRoleId) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String email = authentication.getName();
//
//        Subscriber subscriber = subscriberRepository.findByEmail(email)
//                .orElseThrow(() -> new IllegalArgumentException("Subscriber not found"));
//
//        MissionRole missionRole = missionRoleRepository.findById(missionRoleId)
//                .orElseThrow(() -> new IllegalArgumentException("MissionRole not found"));
//
//        if (missionRole.getNumberAccepted() >= missionRole.getNumberNeeded()) {
//            throw new IllegalStateException("Role is already full");
//        }
//
//        if (missionRole.isRequiresValidation()) {
//            switch (subscriber.getValidationStatus()) {
//                case VALIDATED -> acceptParticipation(subscriber, missionRole);
//                case NOT_VALIDATED -> {
//                    subscriber.setValidationStatus(VolunteerStatus.PENDING_VALIDATION);
//                    subscriberRepository.save(subscriber);
//
//                    Participation participation = new Participation();
//                    participation.setSubscriber(subscriber);
//                    participation.setMissionRole(missionRole);
//                    participation.setParticipationStatus(ParticipationStatus.PENDING);
//                    participationRepository.save(participation);
//                }
//                case PENDING_VALIDATION -> throw new IllegalStateException("Validation is already pending. Please wait.");
//            }
//        } else {
//            acceptParticipation(subscriber, missionRole);
//        }
//    }
//@Transactional
//public void applyToMissionRole(Long missionRoleId) {
//    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//    String email = authentication.getName();
//
//    Subscriber subscriber = subscriberRepository.findByEmail(email)
//            .orElseThrow(() -> new IllegalArgumentException("Subscriber not found"));
//
//    MissionRole missionRole = missionRoleRepository.findById(missionRoleId)
//            .orElseThrow(() -> new IllegalArgumentException("MissionRole not found"));
//
//    boolean alreadyApplied = participationRepository.existsBySubscriberAndMissionRoleAndParticipationStatusIn(
//            subscriber, missionRole,
//            List.of(ParticipationStatus.PENDING, ParticipationStatus.ACCEPTED)
//    );
//
//    if (alreadyApplied) {
//        throw new IllegalStateException("You have already applied or been accepted to this role.");
//    }
//
//    Participation participation = new Participation();
//    participation.setSubscriber(subscriber);
//    participation.setMissionRole(missionRole);
//    participation.setParticipationDate(LocalDateTime.now());
//
//    if (missionRole.isRequiresValidation()) {
//        if (subscriber.getValidationStatus() == VolunteerStatus.VALIDATED) {
//            acceptParticipation(participation, missionRole);
//        } else {
//            participation.setParticipationStatus(ParticipationStatus.PENDING);
//            participationRepository.save(participation);
//            // Optional: Notify user
//            throw new IllegalStateException("Your application is registered but pending admin validation.");
//        }
//    } else {
//        acceptParticipation(participation, missionRole);
//    }
//}

    @Transactional
    public String applyToMissionRole(Long missionRoleId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Subscriber subscriber = subscriberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("❌ Subscriber not found."));

        MissionRole newRole = missionRoleRepository.findById(missionRoleId)
                .orElseThrow(() -> new IllegalArgumentException("❌ MissionRole not found."));

        Mission mission = newRole.getMission();

        // ❌ Case A: Mission has already started or ended
        // Convert Date to LocalDate
        LocalDate startDate = mission.getStartDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

// Now use isBefore() on LocalDate
        if (startDate.isBefore(LocalDate.now())) {
            return "⛔ This mission has already started. You can no longer apply.";
        }

        // ❌ Case B: Role is full
        if (newRole.getNumberAccepted() >= newRole.getNumberNeeded()) {
            return "⛔ This role is already full. Please choose another role.";
        }

        // ❌ Case C: Already applied to the same role
        boolean alreadyAppliedToSameRole = participationRepository.existsBySubscriberAndMissionRoleAndParticipationStatusIn(
                subscriber, newRole, List.of(ParticipationStatus.PENDING, ParticipationStatus.ACCEPTED)
        );
        if (alreadyAppliedToSameRole) {
            return "⚠️ You have already applied to this mission role.";
        }

        // ❌ Case D: Already participating in a different role in the same mission
        Optional<Participation> existingParticipationOpt = participationRepository
                .findBySubscriberAndMissionAndParticipationStatusIn(
                        subscriber, mission, List.of(ParticipationStatus.PENDING, ParticipationStatus.ACCEPTED)
                );
        if (existingParticipationOpt.isPresent()) {
            return "⚠️ You are already participating in another role in this mission. Please cancel it first if you want to change roles.";
        }

        // ✅ Valid case: Create new participation
        Participation newParticipation = new Participation();
        newParticipation.setSubscriber(subscriber);
        newParticipation.setMissionRole(newRole);
        newParticipation.setParticipationDate(LocalDateTime.now());

        boolean roleRequiresValidation = newRole.isRequiresValidation();
        boolean isValidated = subscriber.getValidationStatus() == VolunteerStatus.VALIDATED;
        boolean isPendingValidation = subscriber.getValidationStatus() == VolunteerStatus.PENDING_VALIDATION;

        // 🎯 Case 1: Role requires validation
        if (roleRequiresValidation) {
            if (!isValidated && !isPendingValidation) {
                // Subscriber not validated yet → ask for CV
                subscriber.setValidationStatus(VolunteerStatus.PENDING_VALIDATION);
                subscriberRepository.save(subscriber);

                newParticipation.setParticipationStatus(ParticipationStatus.PENDING);
                participationRepository.save(newParticipation);
                return "📄 You must upload your CV. Your participation is saved and will be reviewed once your profile is validated.";
            } else if (isPendingValidation) {
                // CV already uploaded, just mark participation as pending
                newParticipation.setParticipationStatus(ParticipationStatus.PENDING);
                participationRepository.save(newParticipation);
                return "🕒 Your participation is saved and pending approval while your validation is in progress.";
            } else if (isValidated) {
                // Already validated → accept immediately
                return acceptParticipation(newParticipation, newRole);
            }
        }

        // 🎯 Case 2: Role does not require validation → accept immediately
        return acceptParticipation(newParticipation, newRole);
    }


    @Transactional
    public void promotePendingParticipations(Subscriber subscriber) {
        List<Participation> pendingParticipations =
                participationRepository.findBySubscriberAndParticipationStatus(subscriber, ParticipationStatus.PENDING);

        for (Participation participation : pendingParticipations) {
            MissionRole role = participation.getMissionRole();
            if (role.isRequiresValidation()
                    && role.getNumberAccepted() < role.getNumberNeeded()) {

                participation.setParticipationStatus(ParticipationStatus.ACCEPTED);
                participationRepository.save(participation);

                role.setNumberAccepted(role.getNumberAccepted() + 1);
                missionRoleRepository.save(role);
            }
        }
    }

    private String acceptParticipation(Participation participation, MissionRole role) {
        participation.setParticipationStatus(ParticipationStatus.ACCEPTED);
        participationRepository.save(participation);

        role.setNumberAccepted(role.getNumberAccepted() + 1);
        missionRoleRepository.save(role);

        return "✅ You have successfully applied to the mission role. Welcome aboard!";
    }



    private void handleValidationRequirement(Subscriber subscriber, MissionRole missionRole) {
        switch (subscriber.getValidationStatus()) {
            case VALIDATED -> {
                acceptParticipation(subscriber, missionRole);
            }
            case NOT_VALIDATED -> {
                subscriber.setValidationStatus(PENDING_VALIDATION);
                subscriberRepository.save(subscriber);

                Participation participation = new Participation();
                participation.setSubscriber(subscriber);
                participation.setMissionRole(missionRole);
                participation.setParticipationStatus(ParticipationStatus.PENDING);
                participationRepository.save(participation);

                // ✨ Ask for CV upload
                throw new IllegalStateException("You must upload your CV to complete your application.");
            }
            case PENDING_VALIDATION -> {
                throw new IllegalStateException("Validation is already pending. Please wait.");
            }
        }
    }


    @Transactional
    public void validateParticipationRequest(Long requestId, boolean accepted, String rejectionReason) {
        Participation request = participationRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("ParticipationRequest not found"));

        Subscriber subscriber = request.getSubscriber();
        MissionRole missionRole = request.getMissionRole();

        if (accepted) {
            subscriber.setValidationStatus(VALIDATED);
            subscriberRepository.save(subscriber);

            acceptParticipation(subscriber, missionRole);

            request.setParticipationStatus(ParticipationStatus.ACCEPTED);
        } else {
            request.setParticipationStatus(ParticipationStatus.REJECTED);
            // Subscriber stays NOT_VALIDATED
        }

        participationRepository.save(request);
    }
    @Transactional(readOnly = true)
    public List<Mission> getMissionsByAssociationIdFromToken(String jwtToken) {
        try {
            if (jwtToken == null || jwtToken.trim().isEmpty()) {
                throw new RuntimeException("JWT token cannot be null or empty");
            }

            Long userId = jwtUtils.getUserIdFromJwtToken(jwtToken);
            logger.info("🔍 User ID extracted from JWT: {}", userId);

            Association association = associationRepository.findBySubscriberIdUser(userId)
                    .orElseThrow(() -> new RuntimeException("Association not found for user ID: " + userId));

            List<Mission> allMissions = missionRepository.findByAssociationsIdAssociation(association.getIdAssociation())
                    .stream()
                    .filter(mission -> mission.getStatus() == MissionStatus.UPCOMING || mission.getStatus() == MissionStatus.UPGOING)
                    .sorted(Comparator.comparing(
                            Mission::getStartDate,
                            Comparator.nullsLast(Comparator.naturalOrder())
                    ))
                    .collect(Collectors.toList());

            String baseImageUrl = "http://localhost:8089/images/";

            allMissions.forEach(mission -> {
                if (mission.getMissionRoles() != null) {
                    mission.getMissionRoles().size(); // force lazy loading
                }


            });

            return allMissions;

        } catch (Exception e) {
            logger.error("❌ Error occurred while fetching missions: {}", e.getMessage(), e);
            throw new RuntimeException("Error while fetching missions: " + e.getMessage());
        }
    }


//    public List<MissionRole> getMissionRolesByMissionId(Long missionId) {
//        logger.info("🔍 Searching for MissionRoles linked to Mission ID: {}", missionId);
//
//        Mission mission = missionRepository.findById(missionId)
//                .orElseThrow(() -> new RuntimeException("Mission with ID " + missionId + " not found"));
//
//        List<MissionRole> roles = missionRoleRepository.findByMission(mission);
//
//        if (roles == null || roles.isEmpty()) {
//            throw new RuntimeException("No MissionRoles found for Mission ID " + missionId);
//        }
//
//        return roles;
//    }

    public List<MissionDTO> findMissionByStartDate(String startDate) {
        logger.info("🔍 Searching for missions with start date: {}", startDate);
        List<Mission> missions = missionRepository.findByStartDate(startDate);
        logger.info("✅ Found {} missions", missions.size());

        return missions.stream()
                .map(MissionDTO::fromMission)
                .collect(Collectors.toList());
    }




    public List<Mission> getAllMissions() {
        logger.info("📢 Retrieving all missions...");
        return missionRepository.findAll();
    }

    public Mission getMissionById(Long id) {
        return missionRepository.findByIdWithRoles(id)
                .orElseThrow(() -> new AssociationServices.ResourceNotFoundException("Mission not found"));
    }




    @Transactional
    public Mission updateMission(Long id, MissionDTO updatedMissionDTO, String jwtToken) {
        try {
            logger.info("🔄 Updating mission with ID: {}", id);

            if (jwtToken == null || jwtToken.trim().isEmpty()) {
                throw new RuntimeException("JWT token cannot be null or empty");
            }

            Long userId = jwtUtils.getUserIdFromJwtToken(jwtToken);
            logger.info("🔐 User ID extracted from JWT: {}", userId);

            Mission mission = missionRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Mission not found with ID: " + id));

            mission.setTitle(updatedMissionDTO.getTitle());
            mission.setDescription(updatedMissionDTO.getDescription());
            mission.setLocation(updatedMissionDTO.getLocation());
            mission.setStartDate(updatedMissionDTO.getStartDate());
            mission.setEndDate(updatedMissionDTO.getEndDate());
            mission.setVolunteerCount(updatedMissionDTO.getVolunteerCount());
            mission.setStatus(updatedMissionDTO.getStatus());

            if (updatedMissionDTO.getCrisisId() != null) {
                Crisis crisis = crisisRepository.findById(updatedMissionDTO.getCrisisId())
                        .orElseThrow(() -> new RuntimeException("Crisis not found with ID: " + updatedMissionDTO.getCrisisId()));
                mission.setCrisis(crisis);
            } else {
                mission.setCrisis(null);
            }

            Association association = associationRepository.findBySubscriberIdUser(userId)
                    .orElseThrow(() -> new RuntimeException("Association not found for user ID: " + userId));

            // ✅ Fix ManyToMany association update
            // Clear mission's current associations
            mission.getAssociations().clear();

// Link the association (from DB) to the mission
            mission.getAssociations().add(association);

// Optional but safe: clear old links from association and re-add
            association.getMissions().removeIf(m -> m.getIdMission().equals(mission.getIdMission()));
            association.getMissions().add(mission);

// 🔥 Save BOTH sides explicitly (important for ManyToMany)
            associationRepository.save(association);
            missionRepository.save(mission); // ✅ Save owning side

            // 🔄 Clear and recreate MissionRoles
            if (mission.getMissionRoles() != null && !mission.getMissionRoles().isEmpty()) {
                missionRoleRepository.deleteAll(mission.getMissionRoles());
                mission.getMissionRoles().clear();
            }

            if (updatedMissionDTO.getMissionRoles() != null) {
                for (MissionRoleDTO roleDTO : updatedMissionDTO.getMissionRoles()) {
                    MissionRole missionRole = new MissionRole();
                    missionRole.setRoleName(roleDTO.getRoleName());
                    missionRole.setNumberNeeded(roleDTO.getNumberNeeded());
                    missionRole.setRequiresValidation(roleDTO.isRequiresValidation());
                    missionRole.setMission(mission);
                    mission.getMissionRoles().add(missionRole);
                }
            }

            Mission updated = missionRepository.save(mission); // ✅ Save owning side
            logger.info("✅ Mission successfully updated and reassociated");
            return updated;

        } catch (Exception e) {
            logger.error("❌ Error occurred while updating mission: {}", e.getMessage(), e);
            throw new RuntimeException("Error while updating mission: " + e.getMessage());
        }
    }


    public void deleteMission(Long id) {
        logger.info("🗑 Deleting mission with ID: {}", id);
        missionRepository.deleteById(id);
        logger.info("✅ Mission deleted successfully!");
    }


    /**
     * Get all participations for the current subscriber
     */
    public List<ParticipationMissionDTO> getMyMissionParticipations(String jwtToken) {
        if (jwtToken == null || jwtToken.trim().isEmpty()) {
            throw new RuntimeException("JWT token is missing");
        }

        Long userId = jwtUtils.getUserIdFromJwtToken(jwtToken);
        logger.info("🔍 Fetching participations for subscriber ID: {}", userId);

        List<Participation> participations = participationRepository.findBySubscriberIdUser(userId);

        return participations.stream().map(p -> {
            MissionRole role = p.getMissionRole();
            Mission m = (role != null) ? role.getMission() : null;

            return ParticipationMissionDTO.builder()
                    .participationId(p.getIdParticipation())
                    .missionTitle(m != null ? m.getTitle() : null)
                    .missionDescription(m != null ? m.getDescription() : null)
                    .location(m != null ? m.getLocation() : null)
                    .startDate(m != null ? m.getStartDate() : null)
                    .endDate(m != null ? m.getEndDate() : null)
                    .status(m != null ? m.getStatus() : null)
                    .roleName(role != null ? role.getRoleName() : null)
                    .numberNeeded(role != null ? role.getNumberNeeded() : 0)
                    .numberAccepted(role != null ? role.getNumberAccepted() : 0)
                    .requiresValidation(role != null && role.isRequiresValidation())
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * Cancel a participation and update the role count
     */
    @Transactional
    public void cancelParticipation(Long participationId, String jwtToken) {
        Long subscriberId = jwtUtils.getUserIdFromJwtToken(jwtToken);
        Participation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new RuntimeException("Participation not found"));

        if (!participation.getSubscriber().getIdUser().equals(subscriberId)) {
            throw new RuntimeException("You cannot cancel this participation");
        }

        // Decrease accepted count in MissionRole
        MissionRole role = participation.getMissionRole();
        role.setNumberAccepted(Math.max(0, role.getNumberAccepted() - 1));
        missionRoleRepository.save(role);

        // Delete participation
        participationRepository.delete(participation);

        logger.info("✅ Participation canceled for subscriber ID {} and role updated", subscriberId);
    }

    public String createMissionReminderEmailBody(Subscriber subscriber, Mission mission, String specialLink) {
        String firstName = subscriber.getFirstName() != null ? subscriber.getFirstName() : "Friend";

        LocalDateTime startDateTime = mission.getStartDate()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        String formattedDate = startDateTime.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' HH:mm"));

        return String.format("""
        <div style="font-family: 'Helvetica Neue', Arial, sans-serif; color: #333333; max-width: 600px; margin: 0 auto;">
            <div style="background-color: #006666; padding: 20px; text-align: center;">
                <h1 style="color: white; margin: 0;">Upcoming Mission Reminder</h1>
            </div>
            <div style="padding: 20px;">
                <h2 style="color: #006666;">Hello %s,</h2>
                <p style="font-size: 16px;">
                    This is a friendly reminder: Your mission <strong>"%s"</strong> will begin on <strong>%s</strong>.
                </p>
                <p style="font-size: 16px;">
                    Please arrive on time, bring any necessary materials, and most importantly, bring your positive spirit!
                </p>
                <p style="font-size: 16px;">
                    Be respectful, follow the guidelines, and don't hesitate to reach out to the organizing team if you have questions.
                </p>
                <div style="text-align: center; margin: 30px 0;">
                    <a href="%s" style="background-color: #006666; color: white; padding: 12px 25px;
                                       text-decoration: none; border-radius: 5px; font-weight: bold;">
                        View Mission Details
                    </a>
                </div>
                <p style="font-size: 16px;">Thank you for being a valued volunteer!</p>
                <p style="font-size: 16px;">— The Solidarity & Refugee Team</p>
                <div style="margin-top: 40px; font-size: 12px; color: #999; text-align: center;">
                    © %d Solidarity & Refugee | <a href="https://yourorganization.org" style="color: #006666;">Website</a>
                </div>
            </div>
        </div>
    """, firstName, mission.getTitle(), formattedDate, specialLink, java.util.Calendar.getInstance().get(Calendar.YEAR));
    }
    private final TaskScheduler scheduler = new ConcurrentTaskScheduler();
    @Scheduled(cron = "0 0 2 * * *") // Every day at 2 AM
    public void dailyReminderScheduler() {
        scheduleAllUpcomingMissionReminders();
    }
    public void scheduleAllUpcomingMissionReminders() {
        List<Participation> upcomingParticipations = participationRepository
                .findAllByMissionStartDateAfter(LocalDateTime.now().plusHours(24));

        for (Participation participation : upcomingParticipations) {
            scheduleMissionReminder(participation);
        }
    }

    public void scheduleMissionReminder(Participation participation) {
        Mission mission = participation.getMission();
        Subscriber subscriber = participation.getSubscriber();

        if (mission.getStartDate() == null || subscriber == null) return;
        LocalDateTime startDateTime = mission.getStartDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        Duration untilStart = Duration.between(LocalDateTime.now(), startDateTime);

        if (untilStart.toHours() >= 24) {
            LocalDateTime reminderTime = startDateTime.minusHours(24);
            scheduler.schedule(() -> sendMissionReminder(participation), Timestamp.valueOf(reminderTime));
        } else {
            sendMissionReminder(participation);
        }
    }
    private void sendMissionReminder(Participation participation) {
        Subscriber subscriber = participation.getSubscriber();
        Mission mission = participation.getMission();

        if (subscriber == null || mission == null) return;

        String email = subscriber.getEmail();
        String link = generateMissionLink(mission); // Implement as needed

        String subject = "Reminder: Your Mission Starts Soon!";
        String body = createMissionReminderEmailBody(subscriber, mission, link);

        try {
            mailerSendService.sendEmail(email, subject, body);
        } catch (IOException e) {
            throw new RuntimeException("Failed to send mission reminder email: " + e.getMessage(), e);
        }
    }
    private String generateMissionLink(Mission mission) {
        return "https://yourplatform.org/missions/" + mission.getIdMission();
    }

}
