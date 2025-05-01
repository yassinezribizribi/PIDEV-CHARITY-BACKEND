package tn.esprit.examen.nomPrenomClasseExamen.services;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.AssociationRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.CrisisRepository;
import tn.esprit.examen.nomPrenomClasseExamen.Repositories.MissionRepository;
import tn.esprit.examen.nomPrenomClasseExamen.dto.MissionDTO;
import tn.esprit.examen.nomPrenomClasseExamen.entities.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MissionServices {
    private final MissionRepository missionRepository;
    private final CrisisRepository crisisRepository;
    private final AssociationRepository associationRepository;

    private static final Logger logger = LoggerFactory.getLogger(MissionServices.class);

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

            Mission mission = new Mission();
            mission.setDescription(missionDTO.getDescription());
            mission.setLocation(missionDTO.getLocation());
            mission.setStartDate(missionDTO.getStartDate());
            mission.setEndDate(missionDTO.getEndDate());
            mission.setVolunteerCount(missionDTO.getVolunteerCount());
            mission.setStatus(missionDTO.getStatus());
            mission.setTags(missionDTO.getTags());

            // Handle Crisis relationship
            if (missionDTO.getCrisisId() != null) {
                Crisis crisis = crisisRepository.findById(missionDTO.getCrisisId())
                        .orElseThrow(() -> new RuntimeException("Crisis not found with ID: " + missionDTO.getCrisisId()));
                mission.setCrisis(crisis);
            }

            // Handle Association relationship
            if (missionDTO.getAssociationId() != null) {
                Association association = associationRepository.findById(missionDTO.getAssociationId())
                        .orElseThrow(() -> new RuntimeException("Association not found with ID: " + missionDTO.getAssociationId()));

                if (mission.getAssociations() == null) {
                    mission.setAssociations(new HashSet<>());
                }
                mission.getAssociations().add(association);
            }

            Mission savedMission = missionRepository.save(mission);
            logger.info("✅ Mission created successfully: {}", savedMission);
            return MissionDTO.fromMission(savedMission);
        } catch (Exception e) {
            logger.error("❌ Error while creating mission: {}", e.getMessage(), e);
            throw new RuntimeException("Error while creating mission: " + e.getMessage());
        }
    }

    public MissionDTO updateMission(Long id, MissionDTO updatedMissionDTO) {
        logger.info("🔄 Updating mission with ID: {}", id);
        Mission mission = getMissionById(id);

        mission.setDescription(updatedMissionDTO.getDescription());
        mission.setLocation(updatedMissionDTO.getLocation());
        mission.setStartDate(updatedMissionDTO.getStartDate());
        mission.setEndDate(updatedMissionDTO.getEndDate());
        mission.setVolunteerCount(updatedMissionDTO.getVolunteerCount());
        mission.setStatus(updatedMissionDTO.getStatus());
        mission.setTags(updatedMissionDTO.getTags());

        // Update Crisis relationship
        if (updatedMissionDTO.getCrisisId() != null &&
                (mission.getCrisis() == null || !mission.getCrisis().getIdCrisis().equals(updatedMissionDTO.getCrisisId()))) {
            Crisis crisis = crisisRepository.findById(updatedMissionDTO.getCrisisId())
                    .orElseThrow(() -> new RuntimeException("Crisis not found with ID: " + updatedMissionDTO.getCrisisId()));
            mission.setCrisis(crisis);
        }

        // Update Association relationship
        if (updatedMissionDTO.getAssociationId() != null) {
            Association association = associationRepository.findById(updatedMissionDTO.getAssociationId())
                    .orElseThrow(() -> new RuntimeException("Association not found with ID: " + updatedMissionDTO.getAssociationId()));

            if (mission.getAssociations() == null) {
                mission.setAssociations(new HashSet<>());
            }
            if (!mission.getAssociations().contains(association)) {
                mission.getAssociations().add(association);
            }
        }

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

