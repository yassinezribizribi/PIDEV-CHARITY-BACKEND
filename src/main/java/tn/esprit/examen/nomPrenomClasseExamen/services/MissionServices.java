package tn.esprit.examen.nomPrenomClasseExamen.services;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.MissionRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.SubscriberRepository;
import tn.esprit.examen.nomPrenomClasseExamen.dto.MissionDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Mission;
import tn.esprit.examen.nomPrenomClasseExamen.entities.Subscriber;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MissionServices {
    private SubscriberRepository subscriberRepository;

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

    public MissionDTO createMission(MissionDTO missionDTO) {
        try {
            logger.info("📝 Creating a new mission: {}", missionDTO);

            // Create new Mission entity
            Mission mission = new Mission();

            // Set each attribute from MissionDTO to Mission entity using setters
            mission.setDescription(missionDTO.getDescription());
            mission.setLocation(missionDTO.getLocation());
            mission.setStartDate(missionDTO.getStartDate());
            mission.setEndDate(missionDTO.getEndDate());
            mission.setVolunteerCount(missionDTO.getVolunteerCount());
            mission.setStatus(missionDTO.getStatus());

//            // Handle Crisis and Association relations
//            if (missionDTO.getCrisisId() != null) {
//                Crisis crisis = crisisRepository.findById(missionDTO.getCrisisId())
//                        .orElseThrow(() -> new RuntimeException("Crisis not found with ID: " + missionDTO.getCrisisId()));
//                mission.setCrisis(crisis);
//            }
//
//            if (missionDTO.getAssociationId() != null) {
//                Association association = associationRepository.findById(missionDTO.getAssociationId())
//                        .orElseThrow(() -> new RuntimeException("Association not found with ID: " + missionDTO.getAssociationId()));
//                mission.setAssociations(Collections.singleton(association));
//            }

            // Save the mission
            Mission savedMission = missionRepository.save(mission);

            logger.info("✅ Mission created successfully: {}", savedMission);

            // Convert the saved Mission entity back to DTO and return
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
