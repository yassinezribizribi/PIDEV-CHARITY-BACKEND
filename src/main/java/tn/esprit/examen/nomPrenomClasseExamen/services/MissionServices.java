package tn.esprit.examen.nomPrenomClasseExamen.services;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.AssociationRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.CrisisRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.MissionRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.SubscriberRepository;
import tn.esprit.examen.nomPrenomClasseExamen.dto.MissionDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Association;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Crisis;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Mission;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Subscriber;
import tn.esprit.examen.nomPrenomClasseExamen.jwt.JwtUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MissionServices {
    private SubscriberRepository subscriberRepository;
    private final JwtUtils jwtUtils; // Inject JwtUtils to extract the User ID from JWT
    private final AssociationRepository associationRepository;
    private final CrisisRepository crisisRepository;
    private final MissionRepository missionRepository;
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

    public List<Mission> getMissionsByAssociationIdFromToken(String jwtToken) {
        try {
            if (jwtToken == null || jwtToken.trim().isEmpty()) {
                throw new RuntimeException("JWT token cannot be null or empty");
            }

            Long userId = jwtUtils.getUserIdFromJwtToken(jwtToken);
            logger.info("🔍 User ID extracted from JWT: {}", userId);

            Association association = associationRepository.findBySubscriberIdUser(userId)
                    .orElseThrow(() -> new RuntimeException("Association not found for user ID: " + userId));

            return missionRepository.findByAssociationMissionIdAssociation(association.getIdAssociation());

        } catch (Exception e) {
            logger.error("❌ Error occurred while fetching missions: {}", e.getMessage(), e);
            throw new RuntimeException("Error while fetching missions: " + e.getMessage());
        }
    }

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
        logger.info("🔍 Searching for mission with ID: {}", id);
        return missionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mission with ID " + id + " not found"));
    }

    public MissionDTO createMission(MissionDTO missionDTO, String jwtToken) {
        try {
            if (jwtToken == null || jwtToken.trim().isEmpty()) {
                throw new RuntimeException("JWT token cannot be null or empty");
            }

            // 1️⃣ Extract user ID from JWT
            Long userId = jwtUtils.getUserIdFromJwtToken(jwtToken);
            logger.info("🔐 User ID extracted from JWT: {}", userId);

            // 2️⃣ Retrieve the association linked to this user
            Association association = associationRepository.findBySubscriberIdUser(userId)
                    .orElseThrow(() -> new RuntimeException("Association not found for user ID: " + userId));

            // 3️⃣ Map DTO → Entity
            Mission mission = new Mission();
            mission.setDescription(missionDTO.getDescription());
            mission.setLocation(missionDTO.getLocation());
            mission.setStartDate(missionDTO.getStartDate());
            mission.setEndDate(missionDTO.getEndDate());
            mission.setVolunteerCount(missionDTO.getVolunteerCount());
            mission.setStatus(missionDTO.getStatus());
            mission.setAssociationMission(association); // 🔗 Link the association

            // Optional: handle crisis if included in DTO
            if (missionDTO.getCrisisId() != null) {
                Crisis crisis = crisisRepository.findById(missionDTO.getCrisisId())
                        .orElseThrow(() -> new RuntimeException("Crisis not found with ID: " + missionDTO.getCrisisId()));
                mission.setCrisis(crisis);
            }

            // 4️⃣ Save
            Mission savedMission = missionRepository.save(mission);
            logger.info("✅ Mission created successfully with ID: {}", savedMission.getIdMission());

            // 5️⃣ Convert back to DTO and return
            return MissionDTO.fromMission(savedMission);

        } catch (Exception e) {
            logger.error("❌ Error while creating mission: {}", e.getMessage(), e);
            throw new RuntimeException("Error while creating mission: " + e.getMessage());
        }
    }


    // Method to enroll a subscriber in a mission
//    public String enrollSubscriberToMission(Long subscriberId, Long missionId) {
//        // Find the mission by its ID
//        Mission mission = missionRepository.findById(missionId).orElse(null);
//        if (mission == null) {
//            return "Mission not found!";
//        }
//
//        // Find the subscriber by their ID
//        Subscriber subscriber = subscriberRepository.findById(subscriberId).orElse(null);
//        if (subscriber == null) {
//            return "Subscriber not found!";
//        }
//
//        // Check if the subscriber is already enrolled in the mission
//        if (subscriber.getMissionsubscriber() != null && subscriber.getMissionsubscriber().getIdMission().equals(missionId)) {
//            return "Subscriber is already enrolled in this mission!";
//        }
//
//        // Enroll the subscriber by adding the mission to their list
//        subscriber.setMissionsubscriber(mission); // Set the mission for the subscriber
//
//        // Add the subscriber to the mission
//        mission.getSubscribersMission().add(subscriber);
//
//        // Save both entities with the updated relationships
//        missionRepository.save(mission);
//        subscriberRepository.save(subscriber);
//
//        return "Subscriber successfully enrolled in the mission!";
//    }
    public MissionDTO updateMission(Long id, MissionDTO updatedMissionDTO) {
        logger.info("🔄 Updating mission with ID: {}", id);
        Mission mission = getMissionById(id);

        mission.setDescription(updatedMissionDTO.getDescription());
        mission.setLocation(updatedMissionDTO.getLocation());
        mission.setStartDate(updatedMissionDTO.getStartDate());
        mission.setEndDate(updatedMissionDTO.getEndDate());
        mission.setVolunteerCount(updatedMissionDTO.getVolunteerCount());
        mission.setStatus(updatedMissionDTO.getStatus());

        Mission updated = missionRepository.save(mission);
        logger.info("✅ Mission updated successfully: {}", updated);
        return MissionDTO.fromMission(updated);
    }

    public void deleteMission(Long id) {
        logger.info("🗑 Deleting mission with ID: {}", id);
        missionRepository.deleteById(id);
        logger.info("✅ Mission deleted successfully!");
    }
}
